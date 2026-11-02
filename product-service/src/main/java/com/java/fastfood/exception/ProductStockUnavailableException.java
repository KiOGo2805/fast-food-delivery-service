package com.java.fastfood.exception;

public class ProductStockUnavailableException extends RuntimeException {

    private final Integer productId;
    private final int requested;
    private final int available;

    public ProductStockUnavailableException(Integer productId, int requested, int available) {
        super("Insufficient stock for product " + productId
                + ": requested " + requested + ", available " + available);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public Integer getProductId() {
        return productId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
