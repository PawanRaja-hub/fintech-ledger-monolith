package com.portfolio.fintech.payment;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_records", indexes = @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true))
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, length = 80)
    private String transactionReference;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected IdempotencyRecord() {}
    public IdempotencyRecord(String idempotencyKey, String transactionReference) { this.idempotencyKey = idempotencyKey; this.transactionReference = transactionReference; }
    public String getTransactionReference() { return transactionReference; }
}
