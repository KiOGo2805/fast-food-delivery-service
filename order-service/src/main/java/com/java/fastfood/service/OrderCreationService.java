package com.java.fastfood.service;

import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.mapper.OrderMapper;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ExternalServiceSim externalServiceSim;

    @Transactional
    public OrderResponse createOrder(Order order) {
        Order savedOrder = orderRepository.save(order);
        externalServiceSim.sendEmailNotification(
                savedOrder.getCustomerEmail(), "Order " + savedOrder.getId() + " placed");
        return orderMapper.toResponse(savedOrder);
    }
}
