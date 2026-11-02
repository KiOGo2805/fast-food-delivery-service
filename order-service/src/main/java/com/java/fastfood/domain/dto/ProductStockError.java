package com.java.fastfood.domain.dto;

public record ProductStockError(Integer productId, int requested, int available, String message) {
}
