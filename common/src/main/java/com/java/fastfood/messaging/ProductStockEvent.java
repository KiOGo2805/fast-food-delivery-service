package com.java.fastfood.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductStockEvent(
        String messageId,
        Integer orderId,
        Integer productId,
        int quantity,
        StockAction action,
        BigDecimal unitPrice,
        String productName,
        String customerName,
        OffsetDateTime occurredAt
) {
}
