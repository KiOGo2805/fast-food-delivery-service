package com.java.fastfood.exception;

public class ProductServiceUnavailableException extends RuntimeException {

    public ProductServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductServiceUnavailableException(String message) {
        super(message);
    }
}
