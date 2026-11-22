package com.java.fastfood.product.client;

import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.exception.ProductServiceUnavailableException;
import com.java.fastfood.lookup.ProductAvailability;
import com.java.fastfood.lookup.ProductPrice;
import com.java.fastfood.lookup.ProductStock;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpProductLookupAdapterTest {

    private final ProductLookupHttpClient httpClient = mock(ProductLookupHttpClient.class);
    private final HttpProductLookupAdapter adapter = new HttpProductLookupAdapter(httpClient);

    @Test
    void delegatesAllLookupCalls() {
        when(httpClient.getPrice(1)).thenReturn(new ProductPrice(1, new BigDecimal("4.25")));
        when(httpClient.getStock(1)).thenReturn(new ProductStock(1, 7));
        when(httpClient.getAvailability(1)).thenReturn(new ProductAvailability(1, true));

        assertThat(adapter.findPrice(1)).isEqualByComparingTo("4.25");
        assertThat(adapter.findAvailableStock(1)).isEqualTo(7);
        assertThat(adapter.isAvailable(1)).isTrue();
    }

    @Test
    void mapsNotFoundToProductNotFound() {
        when(httpClient.getPrice(99)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> adapter.findPrice(99))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found: 99");
    }

    @Test
    void mapsConnectionFailuresToUnavailable() {
        when(httpClient.getStock(1)).thenThrow(new ResourceAccessException("connection refused"));

        assertThatThrownBy(() -> adapter.findAvailableStock(1))
                .isInstanceOf(ProductServiceUnavailableException.class);
    }

    @Test
    void mapsServerErrorsToUnavailable() {
        when(httpClient.getAvailability(1)).thenThrow(HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Unavailable", HttpHeaders.EMPTY,
                new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> adapter.isAvailable(1))
                .isInstanceOf(ProductServiceUnavailableException.class);
    }
}
