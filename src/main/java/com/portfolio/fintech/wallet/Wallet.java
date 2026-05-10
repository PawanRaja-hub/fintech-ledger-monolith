package com.portfolio.fintech.wallet;

import com.portfolio.fintech.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallets", indexes = @Index(name = "idx_wallet_user", columnList = "user_id", unique = true))
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Wallet() {}
    public Wallet(AppUser user) { this.user = user; }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public long getVersion() { return version; }

    public void credit(BigDecimal amount) { this.availableBalance = this.availableBalance.add(amount); }
    public void debit(BigDecimal amount) { this.availableBalance = this.availableBalance.subtract(amount); }
}
