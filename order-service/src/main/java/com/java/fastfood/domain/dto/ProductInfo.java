package com.java.fastfood.domain.dto;

import java.math.BigDecimal;

public record ProductInfo(
        Integer id,
        String name,
        String description,
        String category,
        BigDecimal price,
        Integer stockQuantity) {
}
