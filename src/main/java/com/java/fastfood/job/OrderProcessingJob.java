package com.java.fastfood.job;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.repository.OrderRepository;
import com.java.fastfood.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class OrderProcessingJob implements Job {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("OrderProcessingJob started execution");
        
        List<Order> placedOrders = orderRepository.findByStatus(OrderStatus.PLACED);
        
        if (placedOrders.isEmpty()) {
            log.info("No orders in PLACED status found.");
            return;
        }
        
        log.info("Found {} orders to process", placedOrders.size());
        
        for (Order order : placedOrders) {
            try {
                log.info("Processing order ID: {}", order.getId());
                orderService.updateOrderStatus(order.getId(), OrderStatus.CONFIRMED);
                log.info("Order ID: {} status updated to CONFIRMED", order.getId());
            } catch (Exception e) {
                log.error("Failed to process order ID: {}", order.getId(), e);
            }
        }
        
        log.info("OrderProcessingJob finished execution");
    }
}
