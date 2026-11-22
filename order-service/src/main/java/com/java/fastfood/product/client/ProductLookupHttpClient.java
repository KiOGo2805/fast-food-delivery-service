package com.java.fastfood.product.client;

import com.java.fastfood.lookup.ProductAvailability;
import com.java.fastfood.lookup.ProductPrice;
import com.java.fastfood.lookup.ProductStock;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/internal/products")
public interface ProductLookupHttpClient {

    @GetExchange("/{productId}/price")
    ProductPrice getPrice(@PathVariable Integer productId);

    @GetExchange("/{productId}/stock")
    ProductStock getStock(@PathVariable Integer productId);

    @GetExchange("/{productId}/availability")
    ProductAvailability getAvailability(@PathVariable Integer productId);
}
