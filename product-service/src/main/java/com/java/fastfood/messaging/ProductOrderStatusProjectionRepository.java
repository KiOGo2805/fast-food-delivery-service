package com.java.fastfood.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderStatusProjectionRepository extends JpaRepository<ProductOrderStatusProjection, Integer> {
}
