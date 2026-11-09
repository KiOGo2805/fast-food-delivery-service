package com.java.fastfood.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.messaging.OrderAction;
import com.java.fastfood.messaging.OrderCommandEvent;
import com.java.fastfood.messaging.OrderLineRequest;
import com.java.fastfood.outbox.OutboxEvent;
import com.java.fastfood.outbox.OutboxEventRepository;
import com.java.fastfood.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ExternalServiceSim externalServiceSim;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse createOrder(Order order) {
        Order savedOrder = orderRepository.save(order);
        OrderCommandEvent event = new OrderCommandEvent(
                UUID.randomUUID().toString(),
                savedOrder.getId(),
                OrderAction.PLACED,
                null,
                savedOrder.getItems().stream()
                        .map(item -> new OrderLineRequest(
                                item.getLineNumber(), item.getProductId(), item.getQuantity()))
                        .toList(),
                OffsetDateTime.now());
        try {
            outboxEventRepository.save(new OutboxEvent(
                    "Order",
                    savedOrder.getId().toString(),
                    "OrderPlacedEvent",
                    objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize order event for order " + savedOrder.getId(), e);
        }

        externalServiceSim.sendEmailNotification(
                savedOrder.getCustomerEmail(), "Order " + savedOrder.getId() + " placed");
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public void recordCancellation(Order order) {
        OrderCommandEvent event = new OrderCommandEvent(
                UUID.randomUUID().toString(), order.getId(), OrderAction.CANCELLED, null, List.of(),
                OffsetDateTime.now());
        saveOutboxEvent(order, "OrderCancelledEvent", event);
    }

    @Transactional
    public void recordStatusChange(Order order, com.java.fastfood.domain.enums.OrderStatus status) {
        OrderCommandEvent event = new OrderCommandEvent(
                UUID.randomUUID().toString(), order.getId(), OrderAction.STATUS_CHANGED, status.name(), List.of(),
                OffsetDateTime.now());
        saveOutboxEvent(order, "OrderStatusChangedEvent", event);
    }

    private void saveOutboxEvent(Order order, String eventType, Object event) {
        try {
            outboxEventRepository.save(new OutboxEvent(
                    "Order",
                    order.getId().toString(),
                    eventType,
                    objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize " + eventType + " for order " + order.getId(), e);
        }
    }
}
