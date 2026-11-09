package com.java.fastfood.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.listener.simple.auto-startup", havingValue = "true")
public class ProductStockConsumerConfig {

    @Bean
    public DirectExchange productStockExchange() {
        return new DirectExchange("product-stock.exchange", true, false);
    }

    @Bean
    public Queue productStockQueue() {
        return new Queue("product-stock.queue", true);
    }

    @Bean
    public Binding productStockBinding(Queue productStockQueue, DirectExchange productStockExchange) {
        return BindingBuilder.bind(productStockQueue).to(productStockExchange).with("product.stock");
    }
}
