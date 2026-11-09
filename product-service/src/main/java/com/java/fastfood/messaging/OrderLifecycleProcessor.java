package com.java.fastfood.messaging;

import com.java.fastfood.domain.model.Product;
import com.java.fastfood.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderLifecycleProcessor {

    private final ProcessedOrderMessageRepository processedMessageRepository;
    private final ProductOrderReservationRepository reservationRepository;
    private final ProductOrderStatusProjectionRepository statusProjectionRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public void cancel(OrderCommandEvent event) {
        if (processedMessageRepository.existsById(event.messageId())) {
            return;
        }
        processedMessageRepository.saveAndFlush(new ProcessedOrderMessage(event.messageId()));
        releaseReservations(event.orderId());
    }

    @Transactional
    public void updateStatus(OrderCommandEvent event) {
        if (processedMessageRepository.existsById(event.messageId())) {
            return;
        }
        processedMessageRepository.saveAndFlush(new ProcessedOrderMessage(event.messageId()));

        ProductOrderStatusProjection projection = statusProjectionRepository.findById(event.orderId())
                .orElseGet(() -> new ProductOrderStatusProjection(
                        event.orderId(), event.status(), event.occurredAt()));
        projection.setStatus(event.status());
        projection.setUpdatedAt(OffsetDateTime.now());
        statusProjectionRepository.save(projection);
        if ("DECLINED".equals(event.status())) {
            releaseReservations(event.orderId());
        }
    }

    private void releaseReservations(Integer orderId) {
        for (ProductOrderReservation reservation : reservationRepository.findByOrderIdForUpdate(orderId)) {
            if (reservation.getStatus() != ReservationStatus.RESERVED) {
                continue;
            }
            Product product = productStockRepository.findByIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Reserved product " + reservation.getProductId() + " no longer exists"));
            product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
            reservation.setStatus(ReservationStatus.RELEASED);
            productStockRepository.save(product);
        }
    }
}
