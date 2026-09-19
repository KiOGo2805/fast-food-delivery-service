package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.OrderItemResponse;
import com.java.fastfood.domain.model.OrderItem;
import com.java.fastfood.domain.model.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-19T12:56:39+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItemResponse toResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse orderItemResponse = new OrderItemResponse();

        orderItemResponse.setProductId( orderItemProductId( orderItem ) );
        orderItemResponse.setProductName( orderItemProductName( orderItem ) );
        orderItemResponse.setQuantity( orderItem.getQuantity() );
        orderItemResponse.setUnitPrice( orderItem.getUnitPrice() );

        orderItemResponse.setLineTotal( orderItem.lineTotal() );

        return orderItemResponse;
    }

    private Integer orderItemProductId(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        Integer id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderItemProductName(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
