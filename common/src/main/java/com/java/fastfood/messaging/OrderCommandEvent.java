package com.java.fastfood.messaging;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderCommandEvent(
        String messageId,
        Integer orderId,
        OrderAction action,
        String status,
        List<OrderLineRequest> items,
        OffsetDateTime occurredAt
) {
}
