package com.java.fastfood.service;

import com.java.fastfood.domain.dto.ProductInfo;
import com.java.fastfood.domain.dto.ProductStockError;
import com.java.fastfood.exception.InsufficientStockException;
import com.java.fastfood.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class HttpProductClient implements ProductClient {

    private final RestClient restClient;
    private final String apiKey;

    public HttpProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.product-service.url}") String productServiceUrl,
            @Value("${services.product-service.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl(productServiceUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public ProductInfo reserveProduct(Integer productId, int quantity) {
        try {
            ProductInfo product = restClient.post()
                    .uri("/api/internal/products/{id}/reserve?quantity={quantity}", productId, quantity)
                    .header("X-Internal-Api-Key", apiKey)
                    .retrieve()
                    .body(ProductInfo.class);
            if (product == null) {
                throw new IllegalStateException("Product service returned an empty reservation response");
            }
            return product;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException.Conflict exception) {
            ProductStockError error = exception.getResponseBodyAs(ProductStockError.class);
            if (error == null) {
                throw exception;
            }
            throw new InsufficientStockException(error.productId(), error.requested(), error.available());
        }
    }

    @Override
    public void releaseProduct(Integer productId, int quantity) {
        restClient.post()
                .uri("/api/internal/products/{id}/release?quantity={quantity}", productId, quantity)
                .header("X-Internal-Api-Key", apiKey)
                .retrieve()
                .toBodilessEntity();
    }
}
