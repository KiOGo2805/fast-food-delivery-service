package com.java.fastfood.annotation.validation;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductPricingValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void whenComboProductHasLowPrice_thenValidationFails() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Big Combo");
        request.setDescription("Large meal");
        request.setCategory("Mains");
        request.setPrice(new BigDecimal("10.0")); // Too low for Combo
        request.setStockQuantity(100);

        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Combo products must have a price of at least 15.0", violations.iterator().next().getMessage());
    }

    @Test
    void whenComboProductHasHighPrice_thenValidationSucceeds() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Big Combo");
        request.setDescription("Large meal");
        request.setCategory("Mains");
        request.setPrice(new BigDecimal("20.0"));
        request.setStockQuantity(100);

        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void whenNonComboProductHasLowPrice_thenValidationSucceeds() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Burger");
        request.setDescription("Tasty burger");
        request.setCategory("Mains");
        request.setPrice(new BigDecimal("5.0"));
        request.setStockQuantity(100);

        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
