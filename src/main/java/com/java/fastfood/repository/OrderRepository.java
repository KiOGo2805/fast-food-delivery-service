package com.java.fastfood.repository;

import com.java.fastfood.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.items oi " +
            "JOIN FETCH oi.product " +
            "WHERE o.customerName = :username")
    List<Order> findByCustomerName(@Param("username") String username);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items oi WHERE oi.product.id = :productId")
    boolean existsByProductId(@Param("productId") Integer productId);
}
