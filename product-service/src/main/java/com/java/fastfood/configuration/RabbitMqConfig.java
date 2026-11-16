package com.java.fastfood.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange("orders.exchange", true, false);
    }

    @Bean
    public Queue productOrdersQueue() {
        return new Queue("product-orders.queue", true);
    }

    @Bean
    public Binding productOrdersBinding(
            @Qualifier("productOrdersQueue") Queue productOrdersQueue,
            @Qualifier("ordersExchange") DirectExchange ordersExchange) {
        return BindingBuilder.bind(productOrdersQueue).to(ordersExchange).with("order.placed");
    }

    @Bean
    public Binding productOrderCancellationsBinding(
            @Qualifier("productOrdersQueue") Queue queue,
            @Qualifier("ordersExchange") DirectExchange ordersExchange) {
        return BindingBuilder.bind(queue).to(ordersExchange).with("order.cancelled");
    }

    @Bean
    public Binding productOrderStatusBinding(
            @Qualifier("productOrdersQueue") Queue queue,
            @Qualifier("ordersExchange") DirectExchange ordersExchange) {
        return BindingBuilder.bind(queue).to(ordersExchange).with("order.status.changed");
    }

    @Bean
    public DirectExchange orderResultsExchange() {
        return new DirectExchange("order-results.exchange", true, false);
    }

    @Bean
    public Queue orderResultsQueue() {
        return new Queue("order-results.queue", true);
    }

    @Bean
    public Binding orderResultsBinding(
            @Qualifier("orderResultsQueue") Queue orderResultsQueue,
            @Qualifier("orderResultsExchange") DirectExchange orderResultsExchange) {
        return BindingBuilder.bind(orderResultsQueue).to(orderResultsExchange).with("order.result");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter("com.java.fastfood.messaging");
    }
}
