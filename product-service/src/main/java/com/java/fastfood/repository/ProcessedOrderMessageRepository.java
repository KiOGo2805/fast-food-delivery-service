package com.java.fastfood.repository;

import com.java.fastfood.messaging.ProcessedOrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderMessageRepository extends JpaRepository<ProcessedOrderMessage, String> {
}
