package com.java.fastfood.service;

import com.java.fastfood.domain.dto.ProductInfo;
import com.java.fastfood.exception.InsufficientStockException;
import com.java.fastfood.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class HttpProductClientTest {

    @Test
    void reserveProduct_sendsApiKeyAndReturnsProductSnapshot() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://product-service/api/internal/products/7/reserve?quantity=2"))
                .andExpect(method(POST))
                .andExpect(header("X-Internal-Api-Key", "internal-key"))
                .andRespond(withSuccess("""
                        {"id":7,"name":"Burger","description":"Meal","category":"Mains",
                         "price":12.50,"stockQuantity":3}
                        """, MediaType.APPLICATION_JSON));
        HttpProductClient client = new HttpProductClient(builder, "http://product-service", "internal-key");

        ProductInfo product = client.reserveProduct(7, 2);

        assertEquals(7, product.id());
        assertEquals("Burger", product.name());
        assertEquals(new BigDecimal("12.50"), product.price());
        assertEquals(3, product.stockQuantity());
        server.verify();
    }

    @Test
    void reserveProduct_missingProductMapsToDomainException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://product-service/api/internal/products/99/reserve?quantity=1"))
                .andRespond(withStatus(NOT_FOUND));
        HttpProductClient client = new HttpProductClient(builder, "http://product-service", "internal-key");

        assertThrows(ProductNotFoundException.class, () -> client.reserveProduct(99, 1));
        server.verify();
    }

    @Test
    void reserveProduct_insufficientStockMapsToDomainException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://product-service/api/internal/products/7/reserve?quantity=5"))
                .andRespond(withStatus(CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"productId":7,"requested":5,"available":3,
                                 "message":"Insufficient stock"}
                                """));
        HttpProductClient client = new HttpProductClient(builder, "http://product-service", "internal-key");

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class, () -> client.reserveProduct(7, 5));

        assertEquals("Insufficient stock for product 7: requested 5, available 3", exception.getMessage());
        server.verify();
    }

    @Test
    void releaseProduct_sendsApiKeyAndAcceptsSuccessfulResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://product-service/api/internal/products/7/release?quantity=2"))
                .andExpect(method(POST))
                .andExpect(header("X-Internal-Api-Key", "internal-key"))
                .andRespond(withSuccess());
        HttpProductClient client = new HttpProductClient(builder, "http://product-service", "internal-key");

        client.releaseProduct(7, 2);

        server.verify();
    }
}
