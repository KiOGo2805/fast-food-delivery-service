package com.java.fastfood.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreStatusAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Spy
    private StoreStatusAspect storeStatusAspect = new StoreStatusAspect();

    @Test
    void checkStoreIsOpen_whenTimeIsWithinWorkingHours_doesNotThrow() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("placeOrder");

        doReturn(LocalTime.of(14, 0)).when(storeStatusAspect).getCurrentTime();

        assertDoesNotThrow(() -> storeStatusAspect.checkStoreIsOpen(joinPoint));

        verify(joinPoint, atLeastOnce()).getSignature();
    }

    @Test
    void checkStoreIsOpen_whenTimeIsOutsideWorkingHours_throwsException() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("placeOrder");

        doReturn(LocalTime.of(23, 0)).when(storeStatusAspect).getCurrentTime();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> storeStatusAspect.checkStoreIsOpen(joinPoint));

        assertEquals("Sorry, the restaurant is currently closed.", exception.getMessage());
    }
}