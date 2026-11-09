package com.java.fastfood.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "product_order_status")
@Getter
@Setter
@NoArgsConstructor
public class ProductOrderStatusProjection {

    @Id
    @Column(name = "order_id")
    private Integer orderId;

    @Column(nullable = false)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public ProductOrderStatusProjection(Integer orderId, String status, OffsetDateTime updatedAt) {
        this.orderId = orderId;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
