package com.portfolio.fintech.ledger;

import com.portfolio.fintech.common.BusinessException;
import com.portfolio.fintech.common.LedgerEntryType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class LedgerService {
    private static final String SYSTEM_CASH = "SYSTEM-CASH";
    private final LedgerAccountRepository accounts;
    private final LedgerEntryRepository entries;

    public LedgerService(LedgerAccountRepository accounts, LedgerEntryRepository entries) {
        this.accounts = accounts; this.entries = entries;
    }

    @Transactional
    public LedgerAccount ensureAccount(Long walletId) {
        return accounts.findByWalletId(walletId).orElseGet(() -> accounts.save(new LedgerAccount(walletId, "WALLET-" + walletId)));
    }

    @Transactional
    public LedgerAccount systemCashAccount() {
        return accounts.findByAccountCode(SYSTEM_CASH).orElseGet(() -> accounts.save(new LedgerAccount(null, SYSTEM_CASH)));
    }

    @Transactional
    public void recordTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String reference) {
        // Ledger rows are append-only. Wallet balances are a derived convenience; the audit source is this paired entry set.
        var debit = ensureAccount(fromWalletId);
        var credit = ensureAccount(toWalletId);
        entries.save(new LedgerEntry(debit, LedgerEntryType.DEBIT, amount, reference));
        entries.save(new LedgerEntry(credit, LedgerEntryType.CREDIT, amount, reference));
    }

    @Transactional
    public void recordFunding(Long walletId, BigDecimal amount, String reference) {
        entries.save(new LedgerEntry(systemCashAccount(), LedgerEntryType.DEBIT, amount, reference));
        entries.save(new LedgerEntry(ensureAccount(walletId), LedgerEntryType.CREDIT, amount, reference));
    }

    @Transactional(readOnly = true)
    public BigDecimal ledgerBalance(Long walletId) {
        return entries.ledgerBalanceForWallet(walletId);
    }

    public void assertBalanced(BigDecimal debitTotal, BigDecimal creditTotal) {
        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Double-entry ledger imbalance detected");
        }
    }
}
