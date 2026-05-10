package com.portfolio.fintech.payment.dto;

import com.portfolio.fintech.common.TransactionStatus;
import java.math.BigDecimal;

public record PaymentResponse(String reference, Long fromWalletId, Long toWalletId, BigDecimal amount, TransactionStatus status, int riskScore, String fraudExplanation) {}
