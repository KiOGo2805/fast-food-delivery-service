package com.java.fastfood.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank
    private String deliveryAddress;

    private Boolean needsCutlery;
}
