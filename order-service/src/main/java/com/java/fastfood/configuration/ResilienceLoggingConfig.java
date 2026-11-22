package com.java.fastfood.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienceLoggingConfig {

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void registerResilienceEventListeners() {
        retryRegistry.retry("productLookup")
                .getEventPublisher()
                .onRetry(event -> log.warn(
                        "[Resilience4j][Retry] Спроба #{} для '{}'. Причина: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getName(),
                        event.getLastThrowable() != null ? event.getLastThrowable().toString() : "unknown"
                ));

        circuitBreakerRegistry.circuitBreaker("productLookup")
                .getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "[Resilience4j][CircuitBreaker] Зміна стану '{}': {} -> {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                ))
                .onCallNotPermitted(event -> log.warn(
                        "[Resilience4j][CircuitBreaker] Виклик до '{}' відхилено (стан OPEN)",
                        event.getCircuitBreakerName()
                ));
    }
}