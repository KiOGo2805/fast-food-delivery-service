package com.java.fastfood.service;

import com.java.fastfood.domain.dto.OrderItemRequest;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.dto.PlaceOrderRequest;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.OrderItem;
import com.java.fastfood.exception.OrderNotFoundException;
import com.java.fastfood.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCreationService orderCreationService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ExternalServiceSim externalServiceSim;

    public OrderResponse placeOrder(String customerUsername, PlaceOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(customerUsername);
        order.setCustomerEmail(customerUsername + "@example.com");
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setNeedsCutlery(request.getNeedsCutlery());
        order.setCreatedAt(OffsetDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        for (int lineNumber = 0; lineNumber < request.getItems().size(); lineNumber++) {
            OrderItemRequest itemRequest = request.getItems().get(lineNumber);
            OrderItem item = new OrderItem();
            item.setLineNumber(lineNumber);
            item.setProductId(itemRequest.getProductId());
            item.setProductName("Pending product details");
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(BigDecimal.ZERO);
            order.addItem(item);
        }

        return orderCreationService.createOrder(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMyOrders(String customerUsername) {
        return orderRepository.findByCustomerName(customerUsername).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Integer orderId, String requestingUsername, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!isAdmin && !order.getCustomerName().equals(requestingUsername)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional
    public void deleteOrder(Integer orderID) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new OrderNotFoundException(orderID));

        if (order.getStatus() == OrderStatus.PENDING
                || order.getStatus() == OrderStatus.PLACED
                || order.getStatus() == OrderStatus.CONFIRMED) {
            orderCreationService.recordCancellation(order);
        }
        orderRepository.delete(order);
    }

    @Transactional
    public void updateOrderStatus(Integer orderID, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new OrderNotFoundException(orderID));
        if (order.getStatus() == OrderStatus.PENDING && newStatus != OrderStatus.DECLINED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order status cannot change before stock reservation completes");
        }

        orderCreationService.recordStatusChange(order, newStatus);
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }


    public String processOrderAsyncDetails(Integer orderId) {
        CompletableFuture<String> invoiceFuture = externalServiceSim.generateOrderInvoice(orderId)
                .exceptionally(ex -> {
                    log.error("Failed to generate invoice for order {}: {}", orderId, ex.getMessage());
                    return "Invoice-Failed";
                });

        CompletableFuture<String> logisticsFuture = externalServiceSim.callExternalLogistics(orderId)
                .handle((res, ex) -> {
                    if (ex != null) {
                        log.error("Logistics call failed for order {}: {}", orderId, ex.getMessage());
                        return "Logistics-Unavailable";
                    }
                    return res;
                });

        try {
            return invoiceFuture.thenCombine(logisticsFuture, (invoice, logistics) ->
                    "Combined Result: " + invoice + " | " + logistics
            ).get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Async operations timed out for order {}", orderId);
            return "Processing-Timeout";
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error combining async results for order {}", orderId, e);
            return "Processing-Error";
        }
    }

    public String triggerRejectedExecution(String name) {
        try {
            return externalServiceSim.slowSmallTask(name).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Task rejected or failed for {}: {}", name, e.getMessage());
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof java.util.concurrent.RejectedExecutionException) {
                    return "System-Overloaded-Try-Later";
                }
                cause = cause.getCause();
            }
            return "Error-" + e.getMessage();
        }
    }

    public String testTimeout() {
        try {
            return externalServiceSim.verySlowTask().get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Caught expected timeout");
            return "Timeout-Caught";
        } catch (Exception e) {
            log.error("Unexpected error in testTimeout", e);
            return "Error";
        }
    }


    @Transactional
    public void processOrdersInBulk(OrderStatus currentStatus) {
        List<Order> orders = orderRepository.findByStatus(currentStatus);
        if (orders.isEmpty()) {
            return;
        }
        for (Order order : orders) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderCreationService.recordStatusChange(order, OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order ID: {} status updated from {} to CONFIRMED", order.getId(), currentStatus);
        }
    }

    @Transactional
    public void cleanupOrdersInBulk(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        if (!orders.isEmpty()) {
            orderRepository.deleteAll(orders);
            log.info("Deleted {} orders with status {}", orders.size(), status);
        }
    }
}
