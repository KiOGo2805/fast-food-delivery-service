package com.java.fastfood.domain.mapper;

import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderResponse toResponse(Order order);
}
