package com.java.fastfood.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByUsernameAndIdempotencyKey(String username, String idempotencyKey);
    void deleteByUsernameAndIdempotencyKey(String username, String idempotencyKey);
}