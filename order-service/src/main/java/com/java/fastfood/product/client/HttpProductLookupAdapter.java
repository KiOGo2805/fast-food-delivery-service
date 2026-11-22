package com.java.fastfood.product.client;

import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.exception.ProductServiceUnavailableException;
import com.java.fastfood.product.port.ProductAvailabilityLookup;
import com.java.fastfood.product.port.ProductPriceLookup;
import com.java.fastfood.product.port.ProductStockLookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "product.client", name = "transport", havingValue = "http", matchIfMissing = true)
public class HttpProductLookupAdapter implements
        ProductPriceLookup, ProductStockLookup, ProductAvailabilityLookup {

    private final ProductLookupHttpClient client;

    public HttpProductLookupAdapter(ProductLookupHttpClient client) {
        this.client = client;
    }

    @Override
    public BigDecimal findPrice(Integer productId) {
        return call(productId, () -> client.getPrice(productId).price());
    }

    @Override
    public int findAvailableStock(Integer productId) {
        return call(productId, () -> client.getStock(productId).availableQuantity());
    }

    @Override
    public boolean isAvailable(Integer productId) {
        return call(productId, () -> client.getAvailability(productId).available());
    }

    private <T> T call(Integer productId, LookupCall<T> action) {
        try {
            return action.execute();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ProductNotFoundException(productId);
            }
            throw new ProductServiceUnavailableException(
                    "Product service returned HTTP " + exception.getStatusCode().value(), exception);
        } catch (ResourceAccessException exception) {
            throw new ProductServiceUnavailableException("Product service is unavailable", exception);
        }
    }

    @FunctionalInterface
    private interface LookupCall<T> {
        T execute();
    }
}
