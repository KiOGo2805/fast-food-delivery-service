package com.java.fastfood.service;

import com.java.fastfood.annotation.aop.ExecutionTime;
import com.java.fastfood.annotation.aop.LogResult;
import com.java.fastfood.annotation.aop.RequireStoreOpen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AopDemoService {

    @ExecutionTime
    @LogResult
    @RequireStoreOpen
    public String annotatedMethod(String input) {
        log.info("Executing annotatedMethod with input: {}", input);
        try {
            Thread.sleep(100); // Simulate some work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed: " + input;
    }

    public String internalCall(String input) {
        log.info("Calling annotatedMethod internally via 'this'");
        return this.annotatedMethod(input);
    }
}
