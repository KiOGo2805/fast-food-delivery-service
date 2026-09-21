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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void listAll_productsExist_returnsList() {
        // Arrange
        Product product = new Product();
        ProductResponse response = new ProductResponse();
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        // Act
        List<ProductResponse> result = productService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
    }

    @Test
    void getById_productExists_returnsProduct() {
        // Arrange
        Integer productId = 1;
        Product product = new Product();
        ProductResponse response = new ProductResponse();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        // Act
        ProductResponse result = productService.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void getById_productNotFound_throwsException() {
        // Arrange
        Integer productId = 99;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getById(productId));
    }

    @Test
    void create_validRequest_returnsCreatedProduct() {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest();
        Product product = new Product();
        Product savedProduct = new Product();
        ProductResponse response = new ProductResponse();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(response);

        // Act
        ProductResponse result = productService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);

        // Перевірка факту виклику та переданих параметрів через ArgumentCaptor
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(product, productCaptor.getValue());
    }

    @Test
    void update_validRequest_returnsUpdatedProduct() {
        // Arrange
        Integer productId = 1;
        ProductUpdateRequest request = new ProductUpdateRequest();
        Product product = new Product();
        Product savedProduct = new Product();
        ProductResponse response = new ProductResponse();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).updateEntityFromRequest(request, product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(response);

        // Act
        ProductResponse result = productService.update(productId, request);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        verify(productRepository).save(product);
    }

    @Test
    void delete_validId_deletesProduct() {
        // Arrange
        Integer productId = 1;
        when(productRepository.existsById(productId)).thenReturn(true);
        when(orderRepository.existsByProductId(productId)).thenReturn(false);

        // Act
        productService.delete(productId);

        // Assert
        verify(productRepository).deleteById(productId);
    }

    @Test
    void delete_productNotFound_throwsException() {
        // Arrange
        Integer productId = 99;
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.delete(productId));

        // Перевіряємо, що видалення не було викликане
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void delete_productInUse_throwsException() {
        // Arrange
        Integer productId = 1;
        when(productRepository.existsById(productId)).thenReturn(true);
        when(orderRepository.existsByProductId(productId)).thenReturn(true);

        // Act & Assert
        assertThrows(ProductInUseException.class, () -> productService.delete(productId));
        verify(productRepository, never()).deleteById(any());
    }
}