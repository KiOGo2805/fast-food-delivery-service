package com.java.fastfood.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.messaging.ProductStockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String EXCHANGE = "product-stock.exchange";
    private static final String ROUTING_KEY = "product.stock";

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        for (OutboxEvent event : events) {
            try {
                ProductStockEvent payload = objectMapper.readValue(event.getPayload(), ProductStockEvent.class);
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, payload);
                event.setStatus(OutboxStatus.SENT);
                event.setSentAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize outbox event {}", event.getId(), e);
                event.setStatus(OutboxStatus.SENT);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.warn("RabbitMQ unavailable while publishing outbox event {}. It will be retried later.", event.getId(), e);
                return;
            }
        }
    }
}
