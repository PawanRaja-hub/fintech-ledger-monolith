package com.portfolio.fintech.payment;

import com.portfolio.fintech.common.BusinessException;
import com.portfolio.fintech.common.TransactionStatus;
import com.portfolio.fintech.events.AuditEvent;
import com.portfolio.fintech.events.PaymentCompletedEvent;
import com.portfolio.fintech.events.PaymentReviewRequiredEvent;
import com.portfolio.fintech.fraud.FraudDetectionService;
import com.portfolio.fintech.ledger.LedgerService;
import com.portfolio.fintech.payment.dto.FundWalletRequest;
import com.portfolio.fintech.payment.dto.PaymentResponse;
import com.portfolio.fintech.payment.dto.TransferRequest;
import com.portfolio.fintech.wallet.WalletRepository;
import com.portfolio.fintech.wallet.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final WalletRepository wallets;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final FraudDetectionService fraud;
    private final PaymentTransactionRepository transactions;
    private final IdempotencyRecordRepository idempotency;
    private final ApplicationEventPublisher events;

    public PaymentService(WalletRepository wallets, WalletService walletService, LedgerService ledgerService, FraudDetectionService fraud,
                          PaymentTransactionRepository transactions, IdempotencyRecordRepository idempotency, ApplicationEventPublisher events) {
        this.wallets = wallets; this.walletService = walletService; this.ledgerService = ledgerService; this.fraud = fraud;
        this.transactions = transactions; this.idempotency = idempotency; this.events = events;
    }

    @Transactional
    public PaymentResponse transfer(String actorEmail, String idempotencyKey, TransferRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        var existing = idempotency.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(transactions.findByReference(existing.get().getTransactionReference()).orElseThrow());
        }

        var from = wallets.findByUserEmail(actorEmail).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Sender wallet not found"));
        var to = wallets.findForUpdate(request.toWalletId()).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Destination wallet not found"));
        from = wallets.findForUpdate(from.getId()).orElseThrow();
        if (from.getId().equals(to.getId())) throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same wallet");

        var risk = fraud.assess(from.getId(), request.amount());
        String reference = "PAY-" + UUID.randomUUID();
        TransactionStatus status = risk.block() ? TransactionStatus.BLOCKED : (risk.manualReview() ? TransactionStatus.PENDING_REVIEW : TransactionStatus.COMPLETED);
        var tx = transactions.save(new PaymentTransaction(reference, from.getId(), to.getId(), request.amount(), status, risk.score(), risk.explanation()));
        idempotency.save(new IdempotencyRecord(idempotencyKey, reference));

        if (status == TransactionStatus.COMPLETED) {
            moveMoney(from, to, request.amount(), reference);
            events.publishEvent(new PaymentCompletedEvent(reference, from.getId(), to.getId(), request.amount()));
        } else if (status == TransactionStatus.PENDING_REVIEW) {
            events.publishEvent(new PaymentReviewRequiredEvent(reference, risk.score(), risk.explanation()));
        }
        events.publishEvent(new AuditEvent(actorEmail, "PAYMENT_SUBMITTED", "PaymentTransaction", reference, Map.of("status", status.name(), "riskScore", risk.score())));
        return toResponse(tx);
    }

    @Transactional
    public PaymentResponse approve(String actor, String reference) {
        var tx = transactions.findByReference(reference).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Transaction not found"));
        if (tx.getStatus() != TransactionStatus.PENDING_REVIEW) throw new BusinessException(HttpStatus.CONFLICT, "Only pending review transactions can be approved");
        var from = wallets.findForUpdate(tx.getFromWalletId()).orElseThrow();
        var to = wallets.findForUpdate(tx.getToWalletId()).orElseThrow();
        moveMoney(from, to, tx.getAmount(), tx.getReference());
        tx.approve();
        events.publishEvent(new PaymentCompletedEvent(reference, tx.getFromWalletId(), tx.getToWalletId(), tx.getAmount()));
        events.publishEvent(new AuditEvent(actor, "PAYMENT_APPROVED", "PaymentTransaction", reference, Map.of("riskScore", tx.getRiskScore())));
        return toResponse(tx);
    }

    @Transactional
    public PaymentResponse reject(String actor, String reference) {
        var tx = transactions.findByReference(reference).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Transaction not found"));
        if (tx.getStatus() != TransactionStatus.PENDING_REVIEW) throw new BusinessException(HttpStatus.CONFLICT, "Only pending review transactions can be rejected");
        tx.reject();
        events.publishEvent(new AuditEvent(actor, "PAYMENT_REJECTED", "PaymentTransaction", reference, Map.of("riskScore", tx.getRiskScore())));
        return toResponse(tx);
    }

    @Transactional
    public void fund(String actor, FundWalletRequest request) {
        var wallet = wallets.findForUpdate(request.walletId()).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Wallet not found"));
        String reference = "FUND-" + UUID.randomUUID();
        walletService.credit(wallet, request.amount());
        ledgerService.recordFunding(wallet.getId(), request.amount(), reference);
        events.publishEvent(new AuditEvent(actor, "WALLET_FUNDED", "Wallet", wallet.getId().toString(), Map.of("amount", request.amount(), "reference", reference)));
    }

    private void moveMoney(com.portfolio.fintech.wallet.Wallet from, com.portfolio.fintech.wallet.Wallet to, BigDecimal amount, String reference) {
        walletService.debit(from, amount);
        walletService.credit(to, amount);
        ledgerService.recordTransfer(from.getId(), to.getId(), amount, reference);
    }

    private PaymentResponse toResponse(PaymentTransaction tx) {
        return new PaymentResponse(tx.getReference(), tx.getFromWalletId(), tx.getToWalletId(), tx.getAmount(), tx.getStatus(), tx.getRiskScore(), tx.getFraudExplanation());
    }
}
