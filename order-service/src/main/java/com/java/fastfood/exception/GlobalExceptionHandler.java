package com.java.fastfood.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(AccessDeniedException ex) {
        log.error("AccessDeniedException: ", ex);
        return new  ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(ProductNotFoundException ex) {
        log.error("ProductNotFoundException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(OrderNotFoundException ex) {
        log.error("OrderNotFoundException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(InsufficientStockException ex) {
        log.error("InsufficientStockException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApplicationErrorResponse> handleProductServiceFailure(RestClientException ex) {
        log.error("Product service request failed", ex);
        return new ResponseEntity<>(
                new ApplicationErrorResponse("Product service is unavailable"),
                HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: ", ex);
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new ApplicationErrorResponse(message), HttpStatus.BAD_REQUEST);
    }
}
