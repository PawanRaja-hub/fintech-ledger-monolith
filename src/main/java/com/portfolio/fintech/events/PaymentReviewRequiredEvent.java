package com.portfolio.fintech.events;

public record PaymentReviewRequiredEvent(String reference, int riskScore, String explanation) {}
