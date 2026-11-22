package com.java.fastfood.product;

import com.java.fastfood.product.port.ProductAvailabilityLookup;
import com.java.fastfood.product.port.ProductPriceLookup;
import com.java.fastfood.product.port.ProductStockLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductLookupService {

    private final ProductPriceLookup priceLookup;
    private final ProductStockLookup stockLookup;
    private final ProductAvailabilityLookup availabilityLookup;

    public BigDecimal getPrice(Integer productId) {
        return priceLookup.findPrice(productId);
    }

    public int getAvailableStock(Integer productId) {
        return stockLookup.findAvailableStock(productId);
    }

    public boolean isAvailable(Integer productId) {
        return availabilityLookup.isAvailable(productId);
    }
}
