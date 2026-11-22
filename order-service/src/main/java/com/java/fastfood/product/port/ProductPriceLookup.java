package com.java.fastfood.product.port;

import java.math.BigDecimal;

public interface ProductPriceLookup {
    BigDecimal findPrice(Integer productId);
}
