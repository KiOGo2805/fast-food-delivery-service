package com.java.fastfood.service;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.domain.dto.ProductUpdateRequest;
import com.java.fastfood.domain.mapper.ProductMapper;
import com.java.fastfood.domain.model.Product;
import com.java.fastfood.exception.ProductInUseException;
import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.repository.OrderRepository;
import com.java.fastfood.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product(int id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setDescription("Description " + id);
        product.setCategory("Бургери");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(5);
        return product;
    }

    @Test
    void listAll_mapsEveryProduct() {
        Product p1 = product(1);
        Product p2 = product(2);
        ProductResponse r1 = new ProductResponse();
        ProductResponse r2 = new ProductResponse();
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(productMapper.toResponse(p1)).thenReturn(r1);
        when(productMapper.toResponse(p2)).thenReturn(r2);

        List<ProductResponse> result = productService.listAll();

        assertThat(result).containsExactly(r1, r2);
    }

    @Test
    void listAll_noProducts_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertThat(productService.listAll()).isEmpty();
    }

    @Test
    void getById_productExists_returnsMappedResponse() {
        Product product = product(1);
        ProductResponse response = new ProductResponse();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        assertThat(productService.getById(1)).isSameAs(response);
    }

    @Test
    void getById_productMissing_throwsProductNotFoundException() {
        when(productRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(404))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        ProductCreateRequest request = new ProductCreateRequest(
                "New Part", "New Description", "Бургери", new BigDecimal("12.00"), 10);
        Product entity = product(9);
        Product saved = product(9);
        ProductResponse response = new ProductResponse();
        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(response);

        assertThat(productService.create(request)).isSameAs(response);
        verify(productRepository).save(entity);
    }

    @Test
    void update_productExists_appliesChangesAndSaves() {
        Product existing = product(1);
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Name", "Updated Description", "Бургери", new BigDecimal("20.00"), 3);
        ProductResponse response = new ProductResponse();
        when(productRepository.findById(1)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);
        when(productMapper.toResponse(existing)).thenReturn(response);

        ProductResponse result = productService.update(1, request);

        verify(productMapper).updateEntityFromRequest(request, existing);
        assertThat(result).isSameAs(response);
    }

    @Test
    void update_productMissing_throwsProductNotFoundException_beforeTouchingMapper() {
        when(productRepository.findById(404)).thenReturn(Optional.empty());

        ProductUpdateRequest request = new ProductUpdateRequest(
                "Name", "Description", "Бургери", new BigDecimal("1.00"), 1);

        assertThatThrownBy(() -> productService.update(404, request))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(productMapper);
    }

    @Test
    void delete_productExists_deletesById() {
        when(productRepository.existsById(1)).thenReturn(true);
        when(orderRepository.existsByProductId(1)).thenReturn(false);

        productService.delete(1);

        verify(productRepository).deleteById(1);
    }

    @Test
    void delete_productInUse_throwsProductInUseException() {
        when(productRepository.existsById(1)).thenReturn(true);
        when(orderRepository.existsByProductId(1)).thenReturn(true);

        assertThatThrownBy(() -> productService.delete(1))
                .isInstanceOf(ProductInUseException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void delete_productMissing_throwsProductNotFoundException_andNeverCallsDelete() {
        when(productRepository.existsById(404)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(404))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }
}
