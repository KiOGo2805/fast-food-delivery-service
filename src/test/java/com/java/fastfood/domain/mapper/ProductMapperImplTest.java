package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.domain.dto.ProductUpdateRequest;
import com.java.fastfood.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProductMapperImpl.class)
class ProductMapperImplTest {

    @Autowired
    private ProductMapper mapper;
    @Test
    void toResponse_mapsAllFields() {
        Product product = new Product();
        product.setId(1);
        product.setName("Чікен рол");
        product.setDescription("Смачний рол");
        product.setCategory("Роли");
        product.setPrice(new BigDecimal("120.00"));
        product.setStockQuantity(100);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Чікен рол");
        assertThat(response.getDescription()).isEqualTo("Смачний рол");
        assertThat(response.getCategory()).isEqualTo("Роли");
        assertThat(response.getPrice()).isEqualByComparingTo("120.00");
        assertThat(response.getStockQuantity()).isEqualTo(100);
    }

    @Test
    void toEntity_mapsCreateRequestFields() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Нагетси", "Курячі нагетси", "Снеки", new BigDecimal("80.00"), 100);

        Product entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Нагетси");
        assertThat(entity.getDescription()).isEqualTo("Курячі нагетси");
        assertThat(entity.getPrice()).isEqualByComparingTo("80.00");
        assertThat(entity.getStockQuantity()).isEqualTo(100);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void updateEntityFromRequest_overwritesMutableFields_leavesIdAlone() {
        Product existing = new Product();
        existing.setId(7);
        existing.setName("Old Name");
        existing.setDescription("Old Description");
        existing.setCategory("OLD");
        existing.setPrice(new BigDecimal("1.00"));
        existing.setStockQuantity(1);

        ProductUpdateRequest request = new ProductUpdateRequest(
                "New Name", "New Description", "NEW", new BigDecimal("99.99"), 50);

        mapper.updateEntityFromRequest(request, existing);

        assertThat(existing.getId()).isEqualTo(7);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getDescription()).isEqualTo("New Description");
        assertThat(existing.getCategory()).isEqualTo("NEW");
        assertThat(existing.getPrice()).isEqualByComparingTo("99.99");
        assertThat(existing.getStockQuantity()).isEqualTo(50);
    }
}
