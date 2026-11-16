package com.java.fastfood.repository;

import com.java.fastfood.outbox.ProductOutboxEvent;
import com.java.fastfood.outbox.ProductOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, Long> {
    List<ProductOutboxEvent> findTop50ByStatusOrderByIdAsc(ProductOutboxStatus status);
}
