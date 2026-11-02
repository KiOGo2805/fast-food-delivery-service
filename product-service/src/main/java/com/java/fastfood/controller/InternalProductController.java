package com.java.fastfood.controller;

import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.service.ProductService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@Validated
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @Value("${internal.api-key}")
    private String expectedApiKey;

    @PostMapping("/api/internal/products/{id}/reserve")
    public ProductResponse reserve(
            @PathVariable Integer id,
            @RequestParam @Min(1) int quantity,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String apiKey) {
        if (expectedApiKey.isBlank() || apiKey == null || apiKey.isBlank()
                || !MessageDigest.isEqual(
                        expectedApiKey.getBytes(StandardCharsets.UTF_8),
                        apiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Invalid product service API key");
        }
        return productService.reserve(id, quantity);
    }
}
