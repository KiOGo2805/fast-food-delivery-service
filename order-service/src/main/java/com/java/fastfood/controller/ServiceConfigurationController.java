package com.java.fastfood.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServiceConfigurationController {

    @Value("${services.product-service.public-url}")
    private String productServiceUrl;

    @GetMapping("/api/public/config")
    public Map<String, String> publicConfig() {
        return Map.of("productServiceUrl", productServiceUrl);
    }
}
