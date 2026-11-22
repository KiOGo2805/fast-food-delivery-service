package com.java.fastfood.controller;

import com.java.fastfood.product.ProductLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/product-lookups/{productId}")
@RequiredArgsConstructor
public class ProductLookupController {

    private final ProductLookupService productLookupService;

    @GetMapping("/price")
    public BigDecimal getPrice(@PathVariable Integer productId) {
        return productLookupService.getPrice(productId);
    }

    @GetMapping("/stock")
    public int getAvailableStock(@PathVariable Integer productId) {
        return productLookupService.getAvailableStock(productId);
    }

    @GetMapping("/availability")
    public boolean isAvailable(@PathVariable Integer productId) {
        return productLookupService.isAvailable(productId);
    }
}
