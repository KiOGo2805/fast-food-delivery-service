package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.domain.dto.ProductUpdateRequest;
import com.java.fastfood.domain.model.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-30T14:48:16+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setCategory( product.getCategory() );
        productResponse.setDescription( product.getDescription() );
        productResponse.setId( product.getId() );
        productResponse.setName( product.getName() );
        productResponse.setPrice( product.getPrice() );
        productResponse.setStockQuantity( product.getStockQuantity() );

        return productResponse;
    }

    @Override
    public Product toEntity(ProductCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Product product = new Product();

        product.setCategory( request.getCategory() );
        product.setDescription( request.getDescription() );
        product.setName( request.getName() );
        product.setPrice( request.getPrice() );
        product.setStockQuantity( request.getStockQuantity() );

        return product;
    }

    @Override
    public void updateEntityFromRequest(ProductUpdateRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getCategory() != null ) {
            product.setCategory( request.getCategory() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getPrice() != null ) {
            product.setPrice( request.getPrice() );
        }
        if ( request.getStockQuantity() != null ) {
            product.setStockQuantity( request.getStockQuantity() );
        }
    }
}
