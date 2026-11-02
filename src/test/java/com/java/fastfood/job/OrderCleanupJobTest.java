package com.java.fastfood.job;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCleanupJobTest {

    @Mock
    private OrderService orderService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private OrderCleanupJob orderCleanupJob;

    @Test
    void shouldCleanupDeclinedOrdersSuccessfully() throws JobExecutionException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("targetStatus", "DECLINED");
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        orderCleanupJob.execute(jobExecutionContext);

        verify(orderService, times(1)).cleanupOrdersInBulk(OrderStatus.DECLINED);
    }

    @Test
    void shouldDoNothingWhenNoDeclinedOrdersExist() throws JobExecutionException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("targetStatus", "DECLINED");
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        orderCleanupJob.execute(jobExecutionContext);

        verify(orderService, times(1)).cleanupOrdersInBulk(OrderStatus.DECLINED);
    }
}