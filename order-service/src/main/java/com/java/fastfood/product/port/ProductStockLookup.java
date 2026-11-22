package com.java.fastfood.product.port;

public interface ProductStockLookup {
    int findAvailableStock(Integer productId);
}
