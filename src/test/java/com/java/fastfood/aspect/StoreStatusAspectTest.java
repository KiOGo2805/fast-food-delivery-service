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
class StoreStatusAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private StoreStatusAspect storeStatusAspect;

    @Test
    void testCheckStoreIsOpen() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("placeOrder");

        storeStatusAspect.checkStoreIsOpen(joinPoint);

        verify(joinPoint, atLeastOnce()).getSignature();
        verify(signature, atLeastOnce()).getName();
    }
}