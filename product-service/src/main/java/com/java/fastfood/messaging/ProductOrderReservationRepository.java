package com.java.fastfood.messaging;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOrderReservationRepository extends JpaRepository<ProductOrderReservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from ProductOrderReservation reservation "
            + "where reservation.orderId = :orderId order by reservation.productId")
    List<ProductOrderReservation> findByOrderIdForUpdate(@Param("orderId") Integer orderId);
}
