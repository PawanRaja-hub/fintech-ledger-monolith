package com.portfolio.fintech.fraud;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class LocalAiFraudExplanationService {
    private final List<String> explanationTemplates;

    {
        // Instance block shows per-bean initialization. In production this could hydrate prompt templates or model metadata.
        explanationTemplates = List.of(
                "Risk is driven by transaction size, recent velocity, and account behavior.",
                "The explanation is deterministic so tests are stable and no external AI service is required."
        );
    }

    public String explain(BigDecimal amount, int velocity, int score) {
        Prompt prompt = new Prompt("Explain fraud score " + score + " for amount " + amount + " and velocity " + velocity);
        return "Local Spring AI fraud explanation: " + prompt.getContents() + ". "
                + explanationTemplates.get(0) + " " + explanationTemplates.get(1);
    }
}