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
class OrderProcessingJobTest {

    @Mock
    private OrderService orderService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private OrderProcessingJob orderProcessingJob;

    @Test
    void shouldProcessPlacedOrdersSuccessfully() throws JobExecutionException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("targetStatus", "PLACED");
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        orderProcessingJob.execute(jobExecutionContext);

        verify(orderService, times(1)).processOrdersInBulk(OrderStatus.PLACED);
    }

    @Test
    void shouldDoNothingWhenNoPlacedOrdersExist() throws JobExecutionException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("targetStatus", "PLACED");
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        orderProcessingJob.execute(jobExecutionContext);

        verify(orderService, times(1)).processOrdersInBulk(OrderStatus.PLACED);
    }
}