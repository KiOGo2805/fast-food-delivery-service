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
    date = "2026-09-19T12:56:38+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
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

        orderResponse.setId( order.getId() );
        orderResponse.setItems( orderItemListToOrderItemResponseList( order.getItems() ) );
        orderResponse.setTotalAmount( order.getTotalAmount() );
        orderResponse.setStatus( order.getStatus() );
        orderResponse.setCustomerName( order.getCustomerName() );
        orderResponse.setCustomerEmail( order.getCustomerEmail() );
        orderResponse.setDeliveryAddress( order.getDeliveryAddress() );
        orderResponse.setNeedsCutlery( order.getNeedsCutlery() );
        orderResponse.setCreatedAt( order.getCreatedAt() );

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
