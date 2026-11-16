package com.java.fastfood.repository;

import com.java.fastfood.outbox.OutboxEvent;
import com.java.fastfood.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop50ByStatusOrderByIdAsc(OutboxStatus status);
}
