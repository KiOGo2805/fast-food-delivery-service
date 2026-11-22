package com.java.fastfood.exception;

public class ProductUnavailableException extends RuntimeException {

    public ProductUnavailableException(Integer productId) {
        super("Product is not available: " + productId);
    }
}
