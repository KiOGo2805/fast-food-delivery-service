package com.java.fastfood.service;

import com.java.fastfood.domain.model.Product;
import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.lookup.ProductAvailability;
import com.java.fastfood.lookup.ProductPrice;
import com.java.fastfood.lookup.ProductStock;
import com.java.fastfood.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductLookupTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private com.java.fastfood.domain.mapper.ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void lookupMethodsReturnCurrentCatalogValues() {
        Product product = new Product();
        product.setId(12);
        product.setPrice(new BigDecimal("9.50"));
        product.setStockQuantity(4);
        product.setActive(false);
        when(productRepository.findById(12)).thenReturn(Optional.of(product));

        assertThat(productService.getPrice(12)).isEqualTo(new ProductPrice(12, new BigDecimal("9.50")));
        assertThat(productService.getStock(12)).isEqualTo(new ProductStock(12, 4));
        assertThat(productService.getAvailability(12)).isEqualTo(new ProductAvailability(12, false));
    }

    @Test
    void lookupMethodsUseNotFoundForMissingProducts() {
        when(productRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getPrice(404)).isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() -> productService.getStock(404)).isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() -> productService.getAvailability(404)).isInstanceOf(ProductNotFoundException.class);
    }
}
