package com.portfolio.fintech.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {
    Optional<LedgerAccount> findByWalletId(Long walletId);
    Optional<LedgerAccount> findByAccountCode(String accountCode);
}
