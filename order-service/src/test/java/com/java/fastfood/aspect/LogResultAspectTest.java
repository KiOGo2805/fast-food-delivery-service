package com.java.fastfood.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogResultAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private LogResultAspect logResultAspect;

    @Test
    void testLogResult() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");

        logResultAspect.logResult(joinPoint, "TestResult");

        verify(joinPoint, atLeastOnce()).getSignature();
        verify(signature, atLeastOnce()).getName();
    }
}
