package com.portfolio.fintech.admin;

import com.portfolio.fintech.common.ApiResponse;
import com.portfolio.fintech.ledger.LedgerService;
import com.portfolio.fintech.wallet.WalletRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reconciliation")
public class ReconciliationController {
    private final WalletRepository wallets;
    private final LedgerService ledger;

    public ReconciliationController(WalletRepository wallets, LedgerService ledger) { this.wallets = wallets; this.ledger = ledger; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Transactional(readOnly = true)
    public ApiResponse<List<ReconciliationRow>> reconcile() {
        var rows = wallets.findAll().stream().map(w -> {
            var ledgerBalance = ledger.ledgerBalance(w.getId());
            return new ReconciliationRow(w.getId(), w.getUser().getEmail(), w.getAvailableBalance(), ledgerBalance, w.getAvailableBalance().compareTo(ledgerBalance) == 0);
        }).toList();
        return ApiResponse.ok("Wallet balance compared with native SQL ledger projection", rows);
    }

    public record ReconciliationRow(Long walletId, String ownerEmail, java.math.BigDecimal walletBalance, java.math.BigDecimal ledgerBalance, boolean matched) {}
}
