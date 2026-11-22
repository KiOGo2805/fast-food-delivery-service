package com.java.fastfood.controller;

import com.java.fastfood.lookup.ProductAvailability;
import com.java.fastfood.lookup.ProductPrice;
import com.java.fastfood.lookup.ProductStock;
import com.java.fastfood.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/products/{id}")
@RequiredArgsConstructor
public class InternalProductLookupController {

    private final ProductService productService;

    @GetMapping("/price")
    public ProductPrice getPrice(@PathVariable Integer id) {
        return productService.getPrice(id);
    }

    @GetMapping("/stock")
    public ProductStock getStock(@PathVariable Integer id) {
        return productService.getStock(id);
    }

    @GetMapping("/availability")
    public ProductAvailability getAvailability(@PathVariable Integer id) {
        return productService.getAvailability(id);
    }
}
