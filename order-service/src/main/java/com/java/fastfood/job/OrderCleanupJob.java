package com.java.fastfood.job;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class OrderCleanupJob implements Job {

    private final OrderService orderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();

        if (!dataMap.containsKey("targetStatus")) {
            throw new JobExecutionException("Відсутній обов'язковий параметр: targetStatus");
        }

        String targetStatusStr = dataMap.getString("targetStatus");
        OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.valueOf(targetStatusStr);
        } catch (IllegalArgumentException e) {
            throw new JobExecutionException("Невірний статус: " + targetStatusStr, e);
        }

        log.info("OrderCleanupJob started for status: {}", targetStatus);

        try {
            orderService.cleanupOrdersInBulk(targetStatus);
        } catch (Exception e) {
            int refireCount = context.getRefireCount();
            if (refireCount < 3) {
                log.warn("Failed to cleanup orders. Refiring (attempt {})...", refireCount + 1);
                JobExecutionException je = new JobExecutionException(e);
                je.setRefireImmediately(true);
                throw je;
            } else {
                log.error("Failed to cleanup orders after 3 attempts.", e);
            }
        }
    }
}