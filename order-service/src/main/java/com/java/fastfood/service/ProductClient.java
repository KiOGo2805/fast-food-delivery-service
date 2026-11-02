package com.java.fastfood.service;

import com.java.fastfood.domain.dto.ProductInfo;

public interface ProductClient {
    ProductInfo reserveProduct(Integer productId, int quantity);
}
