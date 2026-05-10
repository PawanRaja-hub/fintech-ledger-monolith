package com.portfolio.fintech.payment;

import com.portfolio.fintech.common.TransactionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_reference", columnList = "reference", unique = true),
        @Index(name = "idx_payment_status", columnList = "status")
})
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String reference;

    @Column(nullable = false)
    private Long fromWalletId;

    @Column(nullable = false)
    private Long toWalletId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false)
    private int riskScore;

    @Column(nullable = false, length = 1000)
    private String fraudExplanation;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PaymentTransaction() {}

    public PaymentTransaction(String reference, Long fromWalletId, Long toWalletId, BigDecimal amount, TransactionStatus status, int riskScore, String fraudExplanation) {
        this.reference = reference; this.fromWalletId = fromWalletId; this.toWalletId = toWalletId; this.amount = amount;
        this.status = status; this.riskScore = riskScore; this.fraudExplanation = fraudExplanation;
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public Long getFromWalletId() { return fromWalletId; }
    public Long getToWalletId() { return toWalletId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public int getRiskScore() { return riskScore; }
    public String getFraudExplanation() { return fraudExplanation; }

    public void approve() { this.status = TransactionStatus.COMPLETED; }
    public void reject() { this.status = TransactionStatus.REJECTED; }
}
