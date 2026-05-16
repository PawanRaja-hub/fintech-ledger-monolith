package com.portfolio.fintech.admin;

import com.portfolio.fintech.common.ApiResponse;
import com.portfolio.fintech.common.TransactionStatus;
import com.portfolio.fintech.payment.PaymentService;
import com.portfolio.fintech.payment.PaymentTransactionRepository;
import com.portfolio.fintech.payment.dto.FundWalletRequest;
import com.portfolio.fintech.payment.dto.PaymentResponse;
import com.portfolio.fintech.wallet.WalletRepository;
import com.portfolio.fintech.wallet.dto.WalletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final PaymentService paymentService;
    private final PaymentTransactionRepository transactions;
    private final WalletRepository wallets;

    public AdminController(PaymentService paymentService, PaymentTransactionRepository transactions, WalletRepository wallets) {
        this.paymentService = paymentService; this.transactions = transactions; this.wallets = wallets;
    }

    @GetMapping("/wallets")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Transactional(readOnly = true)
    public ApiResponse<List<WalletResponse>> wallets() {
        var rows = wallets.findAll().stream()
                .map(wallet -> new WalletResponse(wallet.getId(), wallet.getUser().getEmail(), wallet.getAvailableBalance(), wallet.getVersion()))
                .toList();
        return ApiResponse.ok("Wallets loaded", rows);
    }

    @PostMapping("/wallets/fund")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> fund(Authentication auth, @Valid @RequestBody FundWalletRequest request) {
        paymentService.fund(auth.getName(), request);
        return ApiResponse.ok("Wallet funded from system cash account", null);
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ApiResponse<List<PaymentResponse>> pendingReviews() {
        var pending = transactions.findTop50ByStatusOrderByIdDesc(TransactionStatus.PENDING_REVIEW)
                .stream().map(tx -> new PaymentResponse(tx.getReference(), tx.getFromWalletId(), tx.getToWalletId(), tx.getAmount(), tx.getStatus(), tx.getRiskScore(), tx.getFraudExplanation())).toList();
        return ApiResponse.ok("Pending fraud reviews", pending);
    }

    @PostMapping("/reviews/{reference}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ApiResponse<PaymentResponse> approve(Authentication auth, @PathVariable String reference) {
        return ApiResponse.ok("Payment approved", paymentService.approve(auth.getName(), reference));
    }

    @PostMapping("/reviews/{reference}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ApiResponse<PaymentResponse> reject(Authentication auth, @PathVariable String reference) {
        return ApiResponse.ok("Payment rejected", paymentService.reject(auth.getName(), reference));
    }
}
