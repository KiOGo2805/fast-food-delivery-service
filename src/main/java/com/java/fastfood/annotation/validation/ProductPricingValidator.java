package com.java.fastfood.annotation.validation;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class ProductPricingValidator implements ConstraintValidator<ValidProductPricing, ProductCreateRequest> {

    private static final BigDecimal MIN_COMBO_PRICE = new BigDecimal("15.0");

    @Override
    public boolean isValid(ProductCreateRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getName() == null || request.getPrice() == null) {
            return true; // Let other annotations handle nulls
        }

        if (request.getName().toLowerCase().contains("combo")) {
            return request.getPrice().compareTo(MIN_COMBO_PRICE) >= 0;
        }

        return true;
    }
}
