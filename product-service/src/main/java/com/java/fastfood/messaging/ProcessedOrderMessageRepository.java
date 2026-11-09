package com.java.fastfood.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderMessageRepository extends JpaRepository<ProcessedOrderMessage, String> {
}
