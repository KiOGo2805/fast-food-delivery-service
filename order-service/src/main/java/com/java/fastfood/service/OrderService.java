package com.java.fastfood.service;

import com.java.fastfood.domain.dto.OrderItemRequest;
import com.java.fastfood.domain.dto.ProductInfo;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    private static final int BULK_DISCOUNT_THRESHOLD = 5;
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.10");

    private final ProductClient productClient;
    private final OrderCreationService orderCreationService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ExternalServiceSim externalServiceSim;

    public OrderResponse placeOrder(String customerUsername, PlaceOrderRequest request) {
        List<ReservedProduct> reservations = new ArrayList<>();
        try {
            Order order = new Order();
            order.setCustomerName(customerUsername);
            order.setCustomerEmail(customerUsername + "@example.com");
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setNeedsCutlery(request.getNeedsCutlery());
            order.setCreatedAt(OffsetDateTime.now());
            order.setStatus(OrderStatus.PLACED);

            BigDecimal total = BigDecimal.ZERO;
            for (OrderItemRequest itemRequest : request.getItems()) {
                ProductInfo product = productClient.reserveProduct(
                        itemRequest.getProductId(), itemRequest.getQuantity());
                reservations.add(new ReservedProduct(itemRequest.getProductId(), itemRequest.getQuantity()));

                BigDecimal unitPrice = product.price();
                total = total.add(calculateLineTotal(unitPrice, itemRequest.getQuantity()));

                OrderItem item = new OrderItem();
                item.setProductId(product.id());
                item.setProductName(product.name());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(unitPrice);
                order.addItem(item);
            }

            order.setTotalAmount(total);
            return orderCreationService.createOrder(order);
        } catch (RuntimeException exception) {
            releaseReservations(reservations, exception);
            throw exception;
        }
    }

    private void releaseReservations(List<ReservedProduct> reservations, RuntimeException cause) {
        for (int i = reservations.size() - 1; i >= 0; i--) {
            ReservedProduct reservation = reservations.get(i);
            try {
                productClient.releaseProduct(reservation.productId(), reservation.quantity());
            } catch (RuntimeException compensationException) {
                log.error(
                        "Failed to release {} units of product {} after order creation failed",
                        reservation.quantity(),
                        reservation.productId(),
                        compensationException);
                if (compensationException != cause) {
                    cause.addSuppressed(compensationException);
                }
            }
        }
    }

    private record ReservedProduct(Integer productId, int quantity) {
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

    private BigDecimal calculateLineTotal(BigDecimal unitPrice, int quantity) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (quantity >= BULK_DISCOUNT_THRESHOLD) {
            return subtotal.subtract(subtotal.multiply(BULK_DISCOUNT_RATE));
        }
        return subtotal;
    }

    @Transactional
    public void deleteOrder(Integer orderID) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new OrderNotFoundException(orderID));

        orderRepository.delete(order);
    }

    @Transactional
    public void updateOrderStatus(Integer orderID, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new OrderNotFoundException(orderID));

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
