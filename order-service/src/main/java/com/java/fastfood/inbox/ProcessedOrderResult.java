package com.java.fastfood.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "processed_order_results")
@Getter
@NoArgsConstructor
public class ProcessedOrderResult {

    @Id
    @Column(name = "message_id", length = 64)
    private String messageId;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt = OffsetDateTime.now();

    public ProcessedOrderResult(String messageId) {
        this.messageId = messageId;
    }
}
