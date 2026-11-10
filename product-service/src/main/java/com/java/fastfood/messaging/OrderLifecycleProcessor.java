package com.java.fastfood.messaging;

import com.java.fastfood.domain.model.Product;
import com.java.fastfood.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLifecycleProcessor {

    private final ProcessedOrderMessageRepository processedMessageRepository;
    private final ProductOrderReservationRepository reservationRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public void release(OrderCommandEvent event) {
        if (processedMessageRepository.existsById(event.messageId())) {
            return;
        }
        processedMessageRepository.saveAndFlush(new ProcessedOrderMessage(event.messageId()));
        if (event.action() == OrderAction.CANCELLED || "DECLINED".equals(event.status())) {
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
