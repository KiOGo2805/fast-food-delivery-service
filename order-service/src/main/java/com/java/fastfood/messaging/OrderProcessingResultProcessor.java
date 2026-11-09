package com.java.fastfood.messaging;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.OrderItem;
import com.java.fastfood.inbox.ProcessedOrderResult;
import com.java.fastfood.inbox.ProcessedOrderResultRepository;
import com.java.fastfood.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingResultProcessor {

    private static final int BULK_DISCOUNT_THRESHOLD = 5;
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.10");

    private final ProcessedOrderResultRepository processedResultRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void process(OrderProcessingResultEvent event) {
        if (processedResultRepository.existsById(event.messageId())) {
            return;
        }
        processedResultRepository.saveAndFlush(new ProcessedOrderResult(event.messageId()));

        Order order = orderRepository.findByIdForUpdate(event.orderId())
                .orElse(null);
        if (order == null) {
            log.warn("Ignoring processing result {} for deleted order {}", event.messageId(), event.orderId());
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PLACED) {
            log.info("Ignoring processing result {} because order {} is already {}",
                    event.messageId(), order.getId(), order.getStatus());
            return;
        }

        if (!event.accepted()) {
            order.setStatus(OrderStatus.DECLINED);
            return;
        }

        Map<Integer, OrderLineSnapshot> snapshots = event.items().stream()
                .collect(Collectors.toMap(OrderLineSnapshot::lineNumber, Function.identity()));
        if (snapshots.size() != order.getItems().size()) {
            throw new IllegalStateException("Unexpected item count in processing result for order " + order.getId());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            OrderLineSnapshot snapshot = snapshots.get(item.getLineNumber());
            if (snapshot == null
                    || !item.getProductId().equals(snapshot.productId())
                    || item.getQuantity() != snapshot.quantity()) {
                throw new IllegalStateException("Processing result does not match order " + order.getId());
            }
            item.setProductName(snapshot.productName());
            item.setUnitPrice(snapshot.unitPrice());
            BigDecimal lineTotal = snapshot.unitPrice().multiply(BigDecimal.valueOf(snapshot.quantity()));
            if (snapshot.quantity() >= BULK_DISCOUNT_THRESHOLD) {
                lineTotal = lineTotal.subtract(lineTotal.multiply(BULK_DISCOUNT_RATE));
            }
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
    }
}
