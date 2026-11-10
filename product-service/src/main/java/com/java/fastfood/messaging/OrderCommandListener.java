package com.java.fastfood.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.listener.simple.auto-startup", havingValue = "true")
public class OrderCommandListener {

    private final OrderMessageProcessor orderMessageProcessor;
    private final OrderLifecycleProcessor orderLifecycleProcessor;

    @RabbitListener(queues = "product-orders.queue")
    public void handle(OrderCommandEvent event) {
        switch (event.action()) {
            case PLACED -> orderMessageProcessor.process(event);
            case CANCELLED, STATUS_CHANGED -> orderLifecycleProcessor.release(event);
        }
    }
}
