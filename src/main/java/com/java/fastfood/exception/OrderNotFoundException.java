package com.java.fastfood.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Integer id) {
        super("Order not found: " + id);
    }
}
