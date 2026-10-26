package com.java.fastfood.service;

import com.java.fastfood.repository.OrderRepository;
import com.java.fastfood.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        com.java.fastfood.configuration.AsyncConfig.class,
        OrderService.class,
        ExternalServiceSim.class
}, properties = "spring.main.allow-bean-definition-overriding=true")
public class AsyncOrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ExternalServiceSim externalServiceSim;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private com.java.fastfood.domain.mapper.OrderMapper orderMapper;

    @Test
    void testThreadNamePrefix() throws Exception {
        CompletableFuture<String> future = externalServiceSim.generateOrderInvoice(1);
        String threadName = future.thenApply(v -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS);

        assertTrue(threadName.startsWith("FastFoodAsync-"), "Thread name should start with FastFoodAsync-, but was " + threadName);
    }

    @Test
    void testTimeoutHandling() {
        String result = orderService.testTimeout();

        assertEquals("Timeout-Caught", result);
    }

    @Test
    void testParallelExecutionAndCombinedResult() {
        long start = System.currentTimeMillis();
        String result = orderService.processOrderAsyncDetails(123);
        long end = System.currentTimeMillis();

        assertTrue(result.contains("Invoice-PDF-Content-123"));
        assertTrue(result.contains("Logistics-Status-OK-123"));

        long duration = end - start;
        assertTrue(duration < 1800, "Should be parallel, but took " + duration + "ms");
    }

    @Test
    void testAsyncExceptionHandling() {
        Integer failingOrderId = 666;

        String result = orderService.processOrderAsyncDetails(failingOrderId);

        assertTrue(result.contains("Logistics-Unavailable"));
        assertTrue(result.contains("Invoice-PDF-Content-666"));
    }

    @Test
    void testFireAndForgetWithVoidAsync() {
        String email = "test@example.com";
        String message = "fail this message";

        assertDoesNotThrow(() -> {
            externalServiceSim.sendEmailNotification(email, message);
        });
    }

    @Test
    void testTaskRejectedException() throws Exception {

        CompletableFuture<String> f1 = externalServiceSim.slowSmallTask("Task1");
        
        CompletableFuture<String> f2 = externalServiceSim.slowSmallTask("Task2");
        
        String result = orderService.triggerRejectedExecution("Task3");

        assertEquals("System-Overloaded-Try-Later", result);

        CompletableFuture.allOf(f1, f2).join();
    }
}
