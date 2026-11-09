package com.java.fastfood.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.messaging.OrderCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String EXCHANGE = "orders.exchange";

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusOrderByIdAsc(OutboxStatus.NEW);
        for (OutboxEvent event : events) {
            try {
                OrderCommandEvent payload = objectMapper.readValue(event.getPayload(), OrderCommandEvent.class);
                String routingKey = switch (event.getEventType()) {
                    case "OrderPlacedEvent" -> "order.placed";
                    case "OrderCancelledEvent" -> "order.cancelled";
                    case "OrderStatusChangedEvent" -> "order.status.changed";
                    default -> throw new IllegalArgumentException("Unknown outbox event type " + event.getEventType());
                };
                CorrelationData confirmation = new CorrelationData(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload, message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }, confirmation);
                CorrelationData.Confirm confirm = confirmation.getFuture().get(5, TimeUnit.SECONDS);
                if (!confirm.isAck()) {
                    throw new IllegalStateException("RabbitMQ rejected event: " + confirm.getReason());
                }
                if (confirmation.getReturned() != null) {
                    throw new IllegalStateException("RabbitMQ could not route event to a queue");
                }
                event.setStatus(OutboxStatus.SENT);
                event.setSentAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize outbox event {}", event.getId(), e);
                event.setStatus(OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while publishing outbox event {}; it will be retried.", event.getId(), e);
                return;
            } catch (Exception e) {
                log.warn("Could not publish outbox event {}; it will be retried.", event.getId(), e);
                return;
            }
        }
    }
}
