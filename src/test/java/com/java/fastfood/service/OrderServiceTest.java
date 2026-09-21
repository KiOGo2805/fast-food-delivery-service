package com.java.fastfood.service;

import com.java.fastfood.domain.dto.OrderItemRequest;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.dto.PlaceOrderRequest;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.Product;
import com.java.fastfood.exception.InsufficientStockException;
import com.java.fastfood.exception.OrderNotFoundException;
import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.repository.OrderRepository;
import com.java.fastfood.repository.ProductRepository;
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
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_standardQuantity_createsOrderAndDeductsStock() {
        // Arrange
        String username = "bob";
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("Street 1");
        request.setNeedsCutlery(true);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(2); // Менше порогу знижки (5)
        request.setItems(List.of(itemRequest));

        Product product = new Product();
        product.setId(1);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());

        // Act
        OrderResponse result = orderService.placeOrder(username, request);

        // Assert
        assertNotNull(result);

        // Перевірка збереження продукту та зменшення стоку
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(8, productCaptor.getValue().getStockQuantity()); // 10 - 2 = 8

        // Перевірка створення замовлення та суми
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(username, savedOrder.getCustomerName());
        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());
        assertEquals(new BigDecimal("200.00"), savedOrder.getTotalAmount()); // 2 * 100
    }

    @Test
    void placeOrder_bulkQuantity_appliesDiscount() {
        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(5); // Поріг знижки 10%
        request.setItems(List.of(itemRequest));

        Product product = new Product();
        product.setId(1);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());

        // Act
        orderService.placeOrder("bob", request);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // 5 * 100 = 500; знижка 10% = 50; Разом = 450.00
        assertEquals(new BigDecimal("450.0000"), orderCaptor.getValue().getTotalAmount());
    }

    @Test
    void placeOrder_insufficientStock_throwsException() {
        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(15); // Запит більший за наявність
        request.setItems(List.of(itemRequest));

        Product product = new Product();
        product.setId(1);
        product.setStockQuantity(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder("bob", request));
        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_productNotFound_throwsException() {
        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(99);
        request.setItems(List.of(itemRequest));

        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> orderService.placeOrder("bob", request));
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