package com.portfolio.fintech.events;

import java.math.BigDecimal;

public record PaymentCompletedEvent(String reference, Long fromWalletId, Long toWalletId, BigDecimal amount) {}
