package com.portfolio.fintech.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void completed(PaymentCompletedEvent event) {
        log.info("Payment completed after commit: ref={}, from={}, to={}, amount={}", event.reference(), event.fromWalletId(), event.toWalletId(), event.amount());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void review(PaymentReviewRequiredEvent event) {
        log.warn("Payment requires fraud review: ref={}, score={}, explanation={}", event.reference(), event.riskScore(), event.explanation());
    }
}
