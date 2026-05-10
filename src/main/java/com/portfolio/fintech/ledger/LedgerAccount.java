package com.portfolio.fintech.ledger;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ledger_accounts", indexes = {
        @Index(name = "idx_ledger_wallet", columnList = "wallet_id", unique = true),
        @Index(name = "idx_ledger_code", columnList = "accountCode", unique = true)
})
public class LedgerAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", unique = true)
    private Long walletId;

    @Column(nullable = false, unique = true, length = 80)
    private String accountCode;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected LedgerAccount() {}
    public LedgerAccount(Long walletId, String accountCode) { this.walletId = walletId; this.accountCode = accountCode; }
    public Long getId() { return id; }
    public Long getWalletId() { return walletId; }
    public String getAccountCode() { return accountCode; }
}
