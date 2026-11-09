package com.java.fastfood.messaging;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.model.Order;
import com.java.fastfood.domain.model.OrderItem;
import com.java.fastfood.inbox.ProcessedOrderResult;
import com.java.fastfood.inbox.ProcessedOrderResultRepository;
import com.java.fastfood.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessingResultProcessorTest {

    @Mock
    private ProcessedOrderResultRepository processedResultRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderProcessingResultProcessor processor;

    @Test
    void process_successfulReservationStoresSnapshotAndTotal() {
        Order order = order();
        when(orderRepository.findByIdForUpdate(42)).thenReturn(Optional.of(order));
        OrderProcessingResultEvent event = new OrderProcessingResultEvent(
                "result-1",
                42,
                true,
                List.of(new OrderLineSnapshot(0, 7, "Burger", 2, new BigDecimal("12.50"))),
                null,
                OffsetDateTime.now());

        processor.process(event);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(new BigDecimal("25.00"), order.getTotalAmount());
        assertEquals("Burger", order.getItems().getFirst().getProductName());
        verify(processedResultRepository).saveAndFlush(any(ProcessedOrderResult.class));
    }

    @Test
    void process_duplicateResultDoesNotUpdateOrderAgain() {
        when(processedResultRepository.existsById("result-1")).thenReturn(true);
        OrderProcessingResultEvent event = new OrderProcessingResultEvent(
                "result-1", 42, true, List.of(), null, OffsetDateTime.now());

        processor.process(event);

        verifyNoInteractions(orderRepository);
    }

    @Test
    void process_rejectedReservationMarksOrderDeclined() {
        Order order = order();
        when(orderRepository.findByIdForUpdate(42)).thenReturn(Optional.of(order));
        OrderProcessingResultEvent event = new OrderProcessingResultEvent(
                "result-2", 42, false, List.of(), "Insufficient stock", OffsetDateTime.now());

        processor.process(event);

        assertEquals(OrderStatus.DECLINED, order.getStatus());
    }

    private Order order() {
        Order order = new Order();
        order.setId(42);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        OrderItem item = new OrderItem();
        item.setLineNumber(0);
        item.setProductId(7);
        item.setProductName("Pending product details");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.ZERO);
        order.addItem(item);
        return order;
    }
}
