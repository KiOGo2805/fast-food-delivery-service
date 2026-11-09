package com.java.fastfood.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fastfood.domain.model.Product;
import com.java.fastfood.outbox.ProductOutboxEventRepository;
import com.java.fastfood.repository.ProductStockRepository;
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
class OrderMessageProcessorTest {

    @Mock
    private ProductStockRepository productStockRepository;
    @Mock
    private ProductOrderReservationRepository reservationRepository;
    @Mock
    private ProductOrderStatusProjectionRepository statusProjectionRepository;
    @Mock
    private ProcessedOrderMessageRepository processedOrderMessageRepository;
    @Mock
    private ProductOutboxEventRepository productOutboxEventRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderMessageProcessor processor;

    @Test
    void process_reservesStockOnceAndPersistsResult() throws Exception {
        Product product = new Product(
                7, "Burger", "Description", "Mains", new BigDecimal("12.50"), 5);
        when(productStockRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product));
        when(objectMapper.writeValueAsString(any(OrderProcessingResultEvent.class))).thenReturn("{}");
        OrderCommandEvent event = new OrderCommandEvent(
                "message-1",
                42,
                OrderAction.PLACED,
                null,
                List.of(new OrderLineRequest(0, 7, 2)),
                OffsetDateTime.now());

        processor.process(event);

        assertEquals(3, product.getStockQuantity());
        verify(processedOrderMessageRepository).saveAndFlush(any(ProcessedOrderMessage.class));
        verify(reservationRepository).save(any(ProductOrderReservation.class));
        verify(productOutboxEventRepository).save(any());
    }

    @Test
    void process_duplicateMessageDoesNotReserveTwice() {
        when(processedOrderMessageRepository.existsById("message-1")).thenReturn(true);
        OrderCommandEvent event = new OrderCommandEvent(
                "message-1", 42, OrderAction.PLACED, null,
                List.of(new OrderLineRequest(0, 7, 2)), OffsetDateTime.now());

        processor.process(event);

        verifyNoInteractions(productStockRepository, reservationRepository, productOutboxEventRepository);
    }
}
