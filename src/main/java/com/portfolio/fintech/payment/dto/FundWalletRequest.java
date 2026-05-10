package com.portfolio.fintech.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FundWalletRequest(@NotNull Long walletId, @NotNull @DecimalMin("0.01") BigDecimal amount) {}
