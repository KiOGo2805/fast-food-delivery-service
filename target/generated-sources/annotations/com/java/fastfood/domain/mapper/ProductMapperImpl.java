package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.ProductCreateRequest;
import com.java.fastfood.domain.dto.ProductResponse;
import com.java.fastfood.domain.dto.ProductUpdateRequest;
import com.java.fastfood.domain.model.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-19T12:56:39+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setId( product.getId() );
        productResponse.setName( product.getName() );
        productResponse.setDescription( product.getDescription() );
        productResponse.setCategory( product.getCategory() );
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

        product.setName( request.getName() );
        product.setDescription( request.getDescription() );
        product.setCategory( request.getCategory() );
        product.setPrice( request.getPrice() );
        product.setStockQuantity( request.getStockQuantity() );

        return product;
    }

    @Override
    public void updateEntityFromRequest(ProductUpdateRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getCategory() != null ) {
            product.setCategory( request.getCategory() );
        }
        if ( request.getPrice() != null ) {
            product.setPrice( request.getPrice() );
        }
        if ( request.getStockQuantity() != null ) {
            product.setStockQuantity( request.getStockQuantity() );
        }
    }
}
