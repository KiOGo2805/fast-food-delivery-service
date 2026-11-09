package com.java.fastfood.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.messaging.OrderProcessingResultEvent;
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
public class ProductOutboxPublisher {

    private final ProductOutboxEventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<ProductOutboxEvent> events =
                eventRepository.findTop50ByStatusOrderByIdAsc(ProductOutboxStatus.NEW);
        for (ProductOutboxEvent event : events) {
            try {
                OrderProcessingResultEvent payload =
                        objectMapper.readValue(event.getPayload(), OrderProcessingResultEvent.class);
                CorrelationData confirmation = new CorrelationData(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend(
                        "order-results.exchange",
                        "order.result",
                        payload,
                        message -> {
                            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            return message;
                        },
                        confirmation);
                CorrelationData.Confirm confirm = confirmation.getFuture().get(5, TimeUnit.SECONDS);
                if (!confirm.isAck()) {
                    throw new IllegalStateException("RabbitMQ rejected event: " + confirm.getReason());
                }
                if (confirmation.getReturned() != null) {
                    throw new IllegalStateException("RabbitMQ could not route event to a queue");
                }
                event.setStatus(ProductOutboxStatus.SENT);
                event.setSentAt(OffsetDateTime.now());
                eventRepository.save(event);
            } catch (JsonProcessingException e) {
                log.error("Invalid product outbox event {}; marking it failed", event.getId(), e);
                event.setStatus(ProductOutboxStatus.FAILED);
                eventRepository.save(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while publishing product outbox event {}; it will be retried",
                        event.getId(), e);
                return;
            } catch (Exception e) {
                log.warn("Could not publish product outbox event {}; it will be retried", event.getId(), e);
                return;
            }
        }
    }
}
