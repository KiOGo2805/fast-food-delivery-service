package com.java.fastfood.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionTimeAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private ExecutionTimeAspect executionTimeAspect;

    @Test
    void testLogExecutionTime() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn("Result");

        Object result = executionTimeAspect.logExecutionTime(joinPoint);

        assertEquals("Result", result);
        verify(joinPoint, times(1)).proceed();
        verify(joinPoint, atLeastOnce()).getSignature();
    }
}
