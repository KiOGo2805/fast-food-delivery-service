package com.java.fastfood.repository;

import com.java.fastfood.inbox.ProcessedOrderResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderResultRepository extends JpaRepository<ProcessedOrderResult, String> {
}
