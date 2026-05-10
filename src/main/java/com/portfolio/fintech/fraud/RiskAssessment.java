package com.portfolio.fintech.fraud;

public record RiskAssessment(int score, String explanation, boolean block, boolean manualReview) {}
