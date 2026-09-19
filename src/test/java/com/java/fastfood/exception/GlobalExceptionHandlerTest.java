package com.java.fastfood.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesIllegalArgumentException_asBadRequest() {
        ResponseEntity<ApplicationErrorResponse> response =
                handler.handle(new IllegalArgumentException("bad input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("bad input", response.getBody().getMessage());
    }

    @Test
    void handlesAccessDeniedException_asForbidden() {
        ResponseEntity<ApplicationErrorResponse> response =
                handler.handle(new AccessDeniedException("not allowed"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("not allowed", response.getBody().getMessage());
    }

    @Test
    void handlesProductNotFoundException_asNotFound() {
        ResponseEntity<ApplicationErrorResponse> response =
                handler.handle(new ProductNotFoundException(42));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Product not found: 42", response.getBody().getMessage());
    }

    @Test
    void handlesOrderNotFoundException_asNotFound() {
        ResponseEntity<ApplicationErrorResponse> response =
                handler.handle(new OrderNotFoundException(7));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order not found: 7", response.getBody().getMessage());
    }

    @Test
    void handlesInsufficientStockException_asNotFound() {
        ResponseEntity<ApplicationErrorResponse> response =
                handler.handle(new InsufficientStockException(1, 5, 2));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage())
                .contains("requested 5")
                .contains("available 2");
    }
}
