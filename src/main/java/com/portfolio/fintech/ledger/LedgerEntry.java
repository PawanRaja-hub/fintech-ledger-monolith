package com.portfolio.fintech.ledger;

import com.portfolio.fintech.common.LedgerEntryType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_txn", columnList = "transactionReference"),
        @Index(name = "idx_ledger_account", columnList = "ledger_account_id")
})
public class LedgerEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ledger_account_id", nullable = false)
    private LedgerAccount account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LedgerEntryType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 80)
    private String transactionReference;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected LedgerEntry() {}
    public LedgerEntry(LedgerAccount account, LedgerEntryType type, BigDecimal amount, String transactionReference) {
        this.account = account; this.type = type; this.amount = amount; this.transactionReference = transactionReference;
    }
    public Long getId() { return id; }
}
