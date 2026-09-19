package com.java.fastfood.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer id) {
        super("Product not found: " + id);
    }
}
