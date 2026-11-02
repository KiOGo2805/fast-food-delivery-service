package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.OrderItemResponse;
import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.OrderItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-30T14:48:16+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setCreatedAt( order.getCreatedAt() );
        orderResponse.setCustomerEmail( order.getCustomerEmail() );
        orderResponse.setCustomerName( order.getCustomerName() );
        orderResponse.setDeliveryAddress( order.getDeliveryAddress() );
        orderResponse.setId( order.getId() );
        orderResponse.setItems( orderItemListToOrderItemResponseList( order.getItems() ) );
        orderResponse.setNeedsCutlery( order.getNeedsCutlery() );
        orderResponse.setStatus( order.getStatus() );
        orderResponse.setTotalAmount( order.getTotalAmount() );

        return orderResponse;
    }

    protected List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( orderItemMapper.toResponse( orderItem ) );
        }

        return list1;
    }
}
