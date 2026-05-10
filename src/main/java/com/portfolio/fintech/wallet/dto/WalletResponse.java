package com.portfolio.fintech.wallet.dto;

import java.math.BigDecimal;

public record WalletResponse(Long walletId, String ownerEmail, BigDecimal availableBalance, long version) {}
