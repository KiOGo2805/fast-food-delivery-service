package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.domain.dto.ProductUpdateRequest;
import com.java.fastfood.domain.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    Product toEntity(ProductCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);
}
