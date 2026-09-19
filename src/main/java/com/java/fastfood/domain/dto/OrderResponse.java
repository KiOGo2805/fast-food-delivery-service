package com.java.fastfood.domain.dto;

import com.java.fastfood.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String customerName;
    private String customerEmail;
    private String deliveryAddress;
    private Boolean needsCutlery;
    private OffsetDateTime createdAt;
}
