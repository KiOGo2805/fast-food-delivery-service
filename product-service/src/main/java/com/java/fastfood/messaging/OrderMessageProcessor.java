package com.java.fastfood.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.domain.model.Product;
import com.java.fastfood.outbox.ProductOutboxEvent;
import com.java.fastfood.outbox.ProductOutboxEventRepository;
import com.java.fastfood.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderMessageProcessor {

    private final ProductStockRepository productStockRepository;
    private final ProductOrderReservationRepository reservationRepository;
    private final ProcessedOrderMessageRepository processedOrderMessageRepository;
    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(OrderCommandEvent event) {
        if (processedOrderMessageRepository.existsById(event.messageId())) {
            return;
        }
        processedOrderMessageRepository.saveAndFlush(new ProcessedOrderMessage(event.messageId()));

        Map<Integer, Integer> requestedQuantities = new LinkedHashMap<>();
        event.items().stream()
                .sorted(Comparator.comparing(OrderLineRequest::productId))
                .forEach(item -> requestedQuantities.merge(item.productId(), item.quantity(), Integer::sum));

        Map<Integer, Product> products = new HashMap<>();
        String failureReason = null;
        for (Map.Entry<Integer, Integer> requested : requestedQuantities.entrySet()) {
            Product product = productStockRepository.findByIdForUpdate(requested.getKey()).orElse(null);
            if (product == null) {
                failureReason = "Product " + requested.getKey() + " does not exist";
                break;
            }
            if (product.getStockQuantity() < requested.getValue()) {
                failureReason = "Insufficient stock for product " + product.getId();
                break;
            }
            products.put(product.getId(), product);
        }

        List<OrderLineSnapshot> snapshots = new ArrayList<>();
        if (failureReason == null) {
            requestedQuantities.forEach((productId, quantity) -> {
                Product product = products.get(productId);
                product.setStockQuantity(product.getStockQuantity() - quantity);
                reservationRepository.save(new ProductOrderReservation(event.orderId(), productId, quantity));
            });
            productStockRepository.saveAll(products.values());

            for (OrderLineRequest item : event.items()) {
                Product product = products.get(item.productId());
                snapshots.add(new OrderLineSnapshot(
                        item.lineNumber(),
                        product.getId(),
                        product.getName(),
                        item.quantity(),
                        product.getPrice()));
            }
        }

        OrderProcessingResultEvent result = new OrderProcessingResultEvent(
                UUID.randomUUID().toString(),
                event.orderId(),
                failureReason == null,
                snapshots,
                failureReason,
                OffsetDateTime.now());
        try {
            productOutboxEventRepository.save(new ProductOutboxEvent(
                    "OrderProcessingResultEvent",
                    objectMapper.writeValueAsString(result)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Could not serialize processing result for order " + event.orderId(), e);
        }
    }
}
