package com.java.fastfood.messaging;

import java.math.BigDecimal;

public record OrderLineSnapshot(
        int lineNumber,
        Integer productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
