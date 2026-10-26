package com.java.fastfood.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ExternalServiceSim {

    @Async("taskExecutor")
    public void sendEmailNotification(String email, String message) {
        log.info("Starting sendEmailNotification in thread {}", Thread.currentThread().getName());
        simulateSlowOperation(1000);
        if (message != null && message.contains("fail")) {
             throw new RuntimeException("Email delivery failed");
        }
        log.info("Finished sendEmailNotification");
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generateOrderInvoice(Integer orderId) {
        log.info("Starting generateOrderInvoice for order {} in thread {}", orderId, Thread.currentThread().getName());
        simulateSlowOperation(1000);
        log.info("Finished generateOrderInvoice");
        return CompletableFuture.completedFuture("Invoice-PDF-Content-" + orderId);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> callExternalLogistics(Integer orderId) {
        log.info("Starting callExternalLogistics for order {} in thread {}", orderId, Thread.currentThread().getName());
        simulateSlowOperation(1000);
        if (orderId != null && orderId == 666) {
            throw new RuntimeException("Logistics service unavailable");
        }
        log.info("Finished callExternalLogistics");
        return CompletableFuture.completedFuture("Logistics-Status-OK-" + orderId);
    }

    @Async("smallTaskExecutor")
    public CompletableFuture<String> slowSmallTask(String name) {
        log.info("Starting slowSmallTask {} in thread {}", name, Thread.currentThread().getName());
        simulateSlowOperation(2000);
        log.info("Finished slowSmallTask {}", name);
        return CompletableFuture.completedFuture("Done-" + name);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> verySlowTask() {
        log.info("Starting verySlowTask in thread {}", Thread.currentThread().getName());
        simulateSlowOperation(5000);
        return CompletableFuture.completedFuture("Slow-Done");
    }

    private void simulateSlowOperation(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
