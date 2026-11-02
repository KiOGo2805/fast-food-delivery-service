package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.OrderItemResponse;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderMapperImpl.class, OrderItemMapperImpl.class})
class OrderMapperImplTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void orderItemMapper_mapsProductFieldsAndComputesLineTotal() {
        OrderItem item = new OrderItem();
        item.setProductId(1);
        item.setProductName("Meal");
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("10.00"));

        OrderItemResponse response = orderItemMapper.toResponse(item);

        assertThat(response.getProductId()).isEqualTo(1);
        assertThat(response.getProductName()).isEqualTo("Meal");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getUnitPrice()).isEqualByComparingTo("10.00");
        assertThat(response.getLineTotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void orderMapper_mapsOrderWithNestedItemsList() {
        Order order = new Order();
        order.setId(5);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerName("alice");
        order.setCustomerEmail("alice@example.com");
        order.setDeliveryAddress("Kyiv, Main Str 1");
        order.setNeedsCutlery(true);
        order.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        OrderItem item = new OrderItem();
        item.setProductId(2);
        item.setProductName("Meal");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));
        order.addItem(item);

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(5);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("50.00");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getCustomerName()).isEqualTo("alice");
        assertThat(response.getCustomerEmail()).isEqualTo("alice@example.com");
        assertThat(response.getDeliveryAddress()).isEqualTo("Kyiv, Main Str 1");
        assertThat(response.getNeedsCutlery()).isTrue();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getProductName()).isEqualTo("Meal");
        assertThat(response.getItems().getFirst().getLineTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void orderMapper_emptyItemsList_mapsToEmptyList() {
        Order order = new Order();
        order.setId(6);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(OffsetDateTime.now());

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.getItems()).isEmpty();
    }
}
