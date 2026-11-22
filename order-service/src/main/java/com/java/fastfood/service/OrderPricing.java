package com.java.fastfood.service;

import java.math.BigDecimal;

public final class OrderPricing {

    private static final int BULK_DISCOUNT_THRESHOLD = 5;
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.10");

    private OrderPricing() {
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (quantity >= BULK_DISCOUNT_THRESHOLD) {
            return total.subtract(total.multiply(BULK_DISCOUNT_RATE));
        }
        return total;
    }
}
