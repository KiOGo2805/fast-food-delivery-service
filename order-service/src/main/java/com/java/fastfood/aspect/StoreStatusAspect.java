package com.java.fastfood.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Aspect
@Component
@Slf4j
public class StoreStatusAspect {

    @Before("@annotation(com.java.fastfood.annotation.aop.RequireStoreOpen)")
    public void checkStoreIsOpen(JoinPoint joinPoint) {
        log.info("Business rule check: Verifying store is open before executing '{}'", joinPoint.getSignature().getName());

        LocalTime now = getCurrentTime();

        if (now.isBefore(LocalTime.of(8, 0)) || now.isAfter(LocalTime.of(22, 0))) {
            log.warn("Store is closed. Blocking execution.");
            throw new IllegalStateException("Sorry, the restaurant is currently closed.");
        }
    }

    protected LocalTime getCurrentTime() {
        return LocalTime.now();
    }
}