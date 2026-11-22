package com.java.fastfood.exception;

public class ProductLookupCircuitOpenException extends RuntimeException {

    public ProductLookupCircuitOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}