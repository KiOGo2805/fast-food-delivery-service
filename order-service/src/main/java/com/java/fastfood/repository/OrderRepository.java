package com.java.fastfood.repository;

import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.domain.model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items oi WHERE o.customerName = :username")
    List<Order> findByCustomerName(@Param("username") String username);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items oi WHERE oi.productId = :productId")
    boolean existsByProductId(@Param("productId") Integer productId);

    List<Order> findByStatus(OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Integer orderId);
}
