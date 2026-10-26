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
public class OrderCleanupJob implements Job {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("OrderCleanupJob started execution");
        
        List<Order> declinedOrders = orderRepository.findByStatus(OrderStatus.DECLINED);
        
        if (declinedOrders.isEmpty()) {
            log.info("No orders in DECLINED status found for cleanup.");
            return;
        }
        
        log.info("Found {} declined orders to remove", declinedOrders.size());
        for (Order order : declinedOrders) {
            try {
                orderService.deleteOrder(order.getId());
                log.info("Removed order ID: {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to delete order ID: {}", order.getId(), e);
            }
        }
        log.info("OrderCleanupJob finished execution.");
    }
}
