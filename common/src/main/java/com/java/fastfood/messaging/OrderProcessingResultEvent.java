package com.java.fastfood.messaging;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderProcessingResultEvent(
        String messageId,
        Integer orderId,
        boolean accepted,
        List<OrderLineSnapshot> items,
        String failureReason,
        OffsetDateTime occurredAt
) {
}
