package com.java.fastfood.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.listener.simple.auto-startup", havingValue = "true")
public class OrderProcessingResultListener {

    private final OrderProcessingResultProcessor resultProcessor;

    @RabbitListener(queues = "order-results.queue")
    public void handle(OrderProcessingResultEvent event) {
        resultProcessor.process(event);
    }
}
