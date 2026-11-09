package com.java.fastfood.messaging;

import com.java.fastfood.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.listener.simple.auto-startup", havingValue = "true")
public class ProductStockEventListener {

    private final ProductService productService;
    private final Set<String> handledMessages = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = "product-stock.queue")
    public void handle(ProductStockEvent event) {
        if (!handledMessages.add(event.messageId())) {
            log.info("Ignoring duplicate stock event {}", event.messageId());
            return;
        }

        log.info("Processing stock event {} for product {}", event.action(), event.productId());

        if (event.action() == StockAction.ORDER_PLACED) {
            log.info("Order {} placed with {} units of product {}", event.orderId(),
                    event.quantity(), event.productId());
            return;
        }

        if (event.action() == StockAction.RESERVE) {
            productService.reserve(event.productId(), event.quantity());
            return;
        }

        productService.release(event.productId(), event.quantity());
    }
}
