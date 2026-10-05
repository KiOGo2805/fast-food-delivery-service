package com.java.fastfood.service;

import com.java.fastfood.aspect.ExecutionTimeAspect;
import com.java.fastfood.aspect.LogResultAspect;
import com.java.fastfood.aspect.StoreStatusAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AopSelfInvocationTest.Config.class)
class AopSelfInvocationTest {

    @TestConfiguration
    @EnableAspectJAutoProxy
    @Import({AopDemoService.class})
    static class Config {
        @Bean
        public ExecutionTimeAspect executionTimeAspect() {
            return Mockito.spy(new ExecutionTimeAspect());
        }

        @Bean
        public LogResultAspect logResultAspect() {
            return Mockito.spy(new LogResultAspect());
        }

        @Bean
        public StoreStatusAspect storeStatusAspect() {
            return Mockito.spy(new StoreStatusAspect());
        }
    }

    @Autowired
    private AopDemoService aopDemoService;

    @Autowired
    private ExecutionTimeAspect executionTimeAspect;

    @Autowired
    private LogResultAspect logResultAspect;

    @Autowired
    private StoreStatusAspect storeStatusAspect;

    @Test
    void testExternalCallTriggersAspects() throws Throwable {
        aopDemoService.annotatedMethod("test input");

        verify(executionTimeAspect, times(1)).logExecutionTime(any(ProceedingJoinPoint.class));
        verify(logResultAspect, times(1)).logResult(any(JoinPoint.class), any());
        verify(storeStatusAspect, times(1)).checkStoreIsOpen(any(JoinPoint.class));
    }

    @Test
    void testSelfInvocationDoesNotTriggerAspects() throws Throwable {
        reset(executionTimeAspect, logResultAspect, storeStatusAspect);

        aopDemoService.internalCall("test input");

        verify(executionTimeAspect, never()).logExecutionTime(any(ProceedingJoinPoint.class));
        verify(logResultAspect, never()).logResult(any(JoinPoint.class), any());
        verify(storeStatusAspect, never()).checkStoreIsOpen(any(JoinPoint.class));
    }
}