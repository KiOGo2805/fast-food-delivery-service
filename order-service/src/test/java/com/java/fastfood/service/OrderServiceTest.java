package com.java.fastfood.service;

import com.java.fastfood.domain.dto.OrderItemRequest;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.dto.PlaceOrderRequest;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.exception.OrderNotFoundException;
import com.java.fastfood.exception.InsufficientStockException;
import com.java.fastfood.exception.ProductUnavailableException;
import com.java.fastfood.product.port.ProductAvailabilityLookup;
import com.java.fastfood.product.port.ProductPriceLookup;
import com.java.fastfood.product.port.ProductStockLookup;
import com.java.fastfood.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderCreationService orderCreationService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ExternalServiceSim externalServiceSim;
    @Mock
    private ProductAvailabilityLookup productAvailabilityLookup;
    @Mock
    private ProductStockLookup productStockLookup;
    @Mock
    private ProductPriceLookup productPriceLookup;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_createsPendingOrderAndPublishesThroughCreationService() {
        String username = "bob";
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("Street 1");
        request.setNeedsCutlery(true);
        request.setItems(List.of(new OrderItemRequest(1, 2), new OrderItemRequest(2, 1)));
        when(productAvailabilityLookup.isAvailable(1)).thenReturn(true);
        when(productAvailabilityLookup.isAvailable(2)).thenReturn(true);
        when(productStockLookup.findAvailableStock(1)).thenReturn(10);
        when(productStockLookup.findAvailableStock(2)).thenReturn(10);
        when(productPriceLookup.findPrice(1)).thenReturn(new BigDecimal("10.00"));
        when(productPriceLookup.findPrice(2)).thenReturn(new BigDecimal("5.00"));
        when(orderCreationService.createOrder(any(Order.class))).thenReturn(new OrderResponse());

        OrderResponse result = orderService.placeOrder(username, request);

        assertNotNull(result);
        verify(productAvailabilityLookup).isAvailable(1);
        verify(productAvailabilityLookup).isAvailable(2);
        verify(productStockLookup).findAvailableStock(1);
        verify(productStockLookup).findAvailableStock(2);
        verify(productPriceLookup).findPrice(1);
        verify(productPriceLookup).findPrice(2);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderCreationService).createOrder(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(username, savedOrder.getCustomerName());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(new BigDecimal("25.00"), savedOrder.getTotalAmount());
        assertEquals(0, savedOrder.getItems().getFirst().getLineNumber());
        assertEquals(1, savedOrder.getItems().get(1).getLineNumber());
        assertEquals("Pending product details", savedOrder.getItems().getFirst().getProductName());
        assertEquals(new BigDecimal("10.00"), savedOrder.getItems().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("5.00"), savedOrder.getItems().get(1).getUnitPrice());
    }

    @Test
    void placeOrder_aggregatesDuplicateProductQuantitiesBeforeStockCheck() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("Street 1");
        request.setItems(List.of(new OrderItemRequest(1, 3), new OrderItemRequest(1, 3)));
        when(productAvailabilityLookup.isAvailable(1)).thenReturn(true);
        when(productStockLookup.findAvailableStock(1)).thenReturn(5);

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder("bob", request));

        verify(productStockLookup).findAvailableStock(1);
        verifyNoInteractions(productPriceLookup);
        verify(orderCreationService, never()).createOrder(any(Order.class));
    }

    @Test
    void placeOrder_unavailableProductIsRejectedBeforeStockOrPricingLookups() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("Street 1");
        request.setItems(List.of(new OrderItemRequest(1, 1)));
        when(productAvailabilityLookup.isAvailable(1)).thenReturn(false);

        assertThrows(ProductUnavailableException.class, () -> orderService.placeOrder("bob", request));

        verifyNoInteractions(productStockLookup, productPriceLookup);
        verify(orderCreationService, never()).createOrder(any(Order.class));
    }

    @Test
    void placeOrder_calculatesBulkDiscountInPreliminaryTotal() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("Street 1");
        request.setItems(List.of(new OrderItemRequest(1, 5)));
        when(productAvailabilityLookup.isAvailable(1)).thenReturn(true);
        when(productStockLookup.findAvailableStock(1)).thenReturn(5);
        when(productPriceLookup.findPrice(1)).thenReturn(new BigDecimal("10.00"));
        when(orderCreationService.createOrder(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertEquals(new BigDecimal("45.0000"), order.getTotalAmount());
            return new OrderResponse();
        });

        orderService.placeOrder("bob", request);

        verify(orderCreationService).createOrder(any(Order.class));
    }

    @Test
    void listMyOrders_returnsUserOrders() {
        // Arrange
        String username = "bob";
        Order order = new Order();
        when(orderRepository.findByCustomerName(username)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(new OrderResponse());

        // Act
        List<OrderResponse> result = orderService.listMyOrders(username);

        // Assert
        assertEquals(1, result.size());
        verify(orderRepository).findByCustomerName(username);
    }

    @Test
    void getOrder_ownerRequests_returnsOrder() {
        // Arrange
        Integer orderId = 1;
        String username = "bob";
        Order order = new Order();
        order.setCustomerName(username);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(new OrderResponse());

        // Act
        OrderResponse result = orderService.getOrder(orderId, username, false);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getOrder_adminRequestsOtherUserOrder_returnsOrder() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setCustomerName("bob");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(new OrderResponse());

        // Act
        OrderResponse result = orderService.getOrder(orderId, "adminUser", true);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getOrder_otherUserRequests_throwsAccessDeniedException() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setCustomerName("bob");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> orderService.getOrder(orderId, "alice", false));
    }

    @Test
    void getOrder_notFound_throwsException() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(99, "bob", false));
    }

    @Test
    void deleteOrder_existingOrder_deletesSuccessfully() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_notFound_throwsException() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(99));
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void updateOrderStatus_existingOrder_updatesAndSaves() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.COMPLETED, orderCaptor.getValue().getStatus());
    }

    @Test
    void getAllOrders_returnsAllOrders() {
        // Arrange
        Order order = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(new OrderResponse());

        // Act
        List<OrderResponse> result = orderService.getAllOrders();

        // Assert
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }
}