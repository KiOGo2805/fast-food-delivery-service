package com.java.fastfood.product.client;

import com.java.fastfood.exception.ProductLookupCircuitOpenException;
import com.java.fastfood.product.port.ProductAvailabilityLookup;
import com.java.fastfood.product.port.ProductPriceLookup;
import com.java.fastfood.product.port.ProductStockLookup;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.math.BigDecimal;

public class ResilientProductLookupDecorator
        implements ProductPriceLookup, ProductStockLookup, ProductAvailabilityLookup {

    private static final String BACKEND_NAME = "productLookup";

    private final ProductPriceLookup priceDelegate;
    private final ProductStockLookup stockDelegate;
    private final ProductAvailabilityLookup availabilityDelegate;

    public ResilientProductLookupDecorator(
            ProductPriceLookup priceDelegate,
            ProductStockLookup stockDelegate,
            ProductAvailabilityLookup availabilityDelegate) {
        this.priceDelegate = priceDelegate;
        this.stockDelegate = stockDelegate;
        this.availabilityDelegate = availabilityDelegate;
    }

    @Override
    @CircuitBreaker(name = BACKEND_NAME, fallbackMethod = "findPriceFallback")
    @Retry(name = BACKEND_NAME)
    public BigDecimal findPrice(Integer productId) {
        return priceDelegate.findPrice(productId);
    }

    @Override
    @CircuitBreaker(name = BACKEND_NAME, fallbackMethod = "findAvailableStockFallback")
    @Retry(name = BACKEND_NAME)
    public int findAvailableStock(Integer productId) {
        return stockDelegate.findAvailableStock(productId);
    }

    @Override
    @CircuitBreaker(name = BACKEND_NAME, fallbackMethod = "isAvailableFallback")
    @Retry(name = BACKEND_NAME)
    public boolean isAvailable(Integer productId) {
        return availabilityDelegate.isAvailable(productId);
    }

    // --- Fallback лише для стану OPEN (CallNotPermittedException) ---

    @SuppressWarnings("unused")
    private BigDecimal findPriceFallback(Integer productId, CallNotPermittedException ex) {
        throw new ProductLookupCircuitOpenException(
                "Circuit breaker is OPEN for findPrice, productId=" + productId, ex);
    }

    @SuppressWarnings("unused")
    private int findAvailableStockFallback(Integer productId, CallNotPermittedException ex) {
        throw new ProductLookupCircuitOpenException(
                "Circuit breaker is OPEN for findAvailableStock, productId=" + productId, ex);
    }

    @SuppressWarnings("unused")
    private boolean isAvailableFallback(Integer productId, CallNotPermittedException ex) {
        throw new ProductLookupCircuitOpenException(
                "Circuit breaker is OPEN for isAvailable, productId=" + productId, ex);
    }

    // --- Звичайні помилки виконання прокидаємо далі без підміни на 503 Circuit Open ---

    @SuppressWarnings("unused")
    private BigDecimal findPriceFallback(Integer productId, Throwable ex) throws Throwable {
        throw ex;
    }

    @SuppressWarnings("unused")
    private int findAvailableStockFallback(Integer productId, Throwable ex) throws Throwable {
        throw ex;
    }

    @SuppressWarnings("unused")
    private boolean isAvailableFallback(Integer productId, Throwable ex) throws Throwable {
        throw ex;
    }
}