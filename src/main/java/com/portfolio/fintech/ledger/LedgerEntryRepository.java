package com.portfolio.fintech.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    @Query(value = """
            select coalesce(sum(case when type = 'CREDIT' then amount else -amount end), 0)
            from ledger_entries le join ledger_accounts la on le.ledger_account_id = la.id
            where la.wallet_id = :walletId
            """, nativeQuery = true)
    BigDecimal ledgerBalanceForWallet(@Param("walletId") Long walletId);
}
