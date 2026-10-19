package com.java.fastfood.service;

import com.java.fastfood.domain.dto.OrderItemRequest;
import com.java.fastfood.domain.dto.PlaceOrderRequest;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.Product;
import com.java.fastfood.repository.OrderRepository;
import com.java.fastfood.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceSideEffectTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_isBlockedBySideEffects() {
        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        Product product = new Product();
        product.setId(1);
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        long start = System.currentTimeMillis();
        orderService.placeOrder("bob", request);
        long duration = System.currentTimeMillis() - start;

        // Assert
        // Очікуємо затримку принаймні 1000мс (email) + 1200мс (loyalty) = 2200мс
        System.out.println("[DEBUG_LOG] placeOrder duration: " + duration + "ms");
        assertTrue(duration >= 2200, "Execution should be blocked for at least 2200ms");
    }

    @Test
    void updateOrderStatus_isBlockedByPdfGeneration() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        long start = System.currentTimeMillis();
        orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
        long duration = System.currentTimeMillis() - start;

        // Assert
        // Очікуємо затримку принаймні 1500мс (PDF generation)
        System.out.println("[DEBUG_LOG] updateOrderStatus duration: " + duration + "ms");
        assertTrue(duration >= 1500, "Execution should be blocked for at least 1500ms");
    }
}
