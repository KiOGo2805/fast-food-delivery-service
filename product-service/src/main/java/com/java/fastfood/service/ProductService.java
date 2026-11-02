package com.java.fastfood.service;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.domain.dto.ProductUpdateRequest;
import com.java.fastfood.domain.mapper.ProductMapper;
import com.java.fastfood.domain.model.Product;
import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.exception.ProductStockUnavailableException;
import com.java.fastfood.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Integer id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Integer id, ProductUpdateRequest request) {
        Product product = findOrThrow(id);
        productMapper.updateEntityFromRequest(request, product);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponse reserve(Integer id, int quantity) {
        Product product = findOrThrow(id);
        if (product.getStockQuantity() < quantity) {
            throw new ProductStockUnavailableException(id, quantity, product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productMapper.toResponse(productRepository.save(product));
    }

    private Product findOrThrow(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
