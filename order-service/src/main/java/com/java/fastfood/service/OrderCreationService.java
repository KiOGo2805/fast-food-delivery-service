package com.java.fastfood.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.OrderItem;
import com.java.fastfood.messaging.ProductStockEvent;
import com.java.fastfood.messaging.StockAction;
import com.java.fastfood.outbox.OutboxEvent;
import com.java.fastfood.outbox.OutboxEventRepository;
import com.java.fastfood.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

        for (OrderItem item : savedOrder.getItems()) {
            ProductStockEvent event = new ProductStockEvent(
                    UUID.randomUUID().toString(),
                    savedOrder.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    StockAction.ORDER_PLACED,
                    item.getUnitPrice(),
                    item.getProductName(),
                    savedOrder.getCustomerName(),
                    OffsetDateTime.now());

            try {
                outboxEventRepository.save(new OutboxEvent(
                        "Order",
                        savedOrder.getId().toString(),
                        "ProductStockEvent",
                        objectMapper.writeValueAsString(event)));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not serialize stock event for order " + savedOrder.getId(), e);
            }
        }

        externalServiceSim.sendEmailNotification(
                savedOrder.getCustomerEmail(), "Order " + savedOrder.getId() + " placed");
        return orderMapper.toResponse(savedOrder);
    }
}
