package com.java.fastfood.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    public <T> T execute(
            String username,
            String idempotencyKey,
            Class<T> responseType,
            Supplier<T> action) {

        TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            requiresNew.executeWithoutResult(status -> repository.saveAndFlush(
                    IdempotencyRecord.builder()
                            .username(username)
                            .idempotencyKey(idempotencyKey)
                            .status(IdempotencyStatus.IN_PROGRESS)
                            .createdAt(Instant.now())
                            .build()
            ));
        } catch (DataIntegrityViolationException ex) {
            IdempotencyRecord existing = repository
                    .findByUsernameAndIdempotencyKey(username, idempotencyKey)
                    .orElseThrow(() -> ex);

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                log.info("Returning cached idempotent response for user='{}', key='{}'", username, idempotencyKey);
                return deserialize(existing.getResponseBody(), responseType);
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Request with this Idempotency-Key is already in progress"
            );
        }

        try {
            T result = action.get();
            String jsonBody = serialize(result);

            requiresNew.executeWithoutResult(status -> {
                IdempotencyRecord record = repository
                        .findByUsernameAndIdempotencyKey(username, idempotencyKey)
                        .orElseThrow();
                record.setStatus(IdempotencyStatus.COMPLETED);
                record.setHttpStatus(HttpStatus.CREATED.value());
                record.setResponseBody(jsonBody);
                repository.save(record);
            });

            return result;
        } catch (RuntimeException ex) {
            requiresNew.executeWithoutResult(status ->
                    repository.deleteByUsernameAndIdempotencyKey(username, idempotencyKey)
            );
            throw ex;
        }
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotent response", e);
        }
    }

    private <T> T deserialize(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize cached idempotent response", e);
        }
    }
}