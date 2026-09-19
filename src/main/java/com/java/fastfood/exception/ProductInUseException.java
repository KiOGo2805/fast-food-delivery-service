package com.java.fastfood.exception;

public class ProductInUseException extends RuntimeException {
    public ProductInUseException(Integer productId) {
        super("Product with ID " + productId + " cannot be deleted because it is associated with existing orders.");
    }
}
