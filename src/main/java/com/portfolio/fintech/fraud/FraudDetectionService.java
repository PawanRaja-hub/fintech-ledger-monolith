package com.portfolio.fintech.fraud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class FraudDetectionService {
    private final VelocityCache velocityCache;
    private final LocalAiFraudExplanationService ai;
    private final int manualReviewThreshold;
    private final int hardBlockThreshold;

    public FraudDetectionService(VelocityCache velocityCache, LocalAiFraudExplanationService ai,
                                 @Value("${app.fraud.manual-review-threshold}") int manualReviewThreshold,
                                 @Value("${app.fraud.hard-block-threshold}") int hardBlockThreshold) {
        this.velocityCache = velocityCache; this.ai = ai;
        this.manualReviewThreshold = manualReviewThreshold; this.hardBlockThreshold = hardBlockThreshold;
    }

    public RiskAssessment assess(Long fromWalletId, BigDecimal amount) {
        int velocity = velocityCache.registerAndCount(fromWalletId);
        int score = 10;
        if (amount.compareTo(new BigDecimal("1000")) > 0) score += 35;
        if (amount.compareTo(new BigDecimal("5000")) > 0) score += 35;
        if (velocity > 3) score += 25;
        score = Math.min(score, 100);
        return new RiskAssessment(score, ai.explain(amount, velocity, score), score >= hardBlockThreshold, score >= manualReviewThreshold);
    }
}
