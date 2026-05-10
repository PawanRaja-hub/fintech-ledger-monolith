package com.portfolio.fintech.fraud;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VelocityCache {
    private static final long WINDOW_SECONDS = 60;
    private final Map<Long, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    static {
        // Static block intentionally demonstrates one-time JVM initialization for immutable cache policy.
        System.setProperty("fintech.velocity.window.seconds", String.valueOf(WINDOW_SECONDS));
    }

    public int registerAndCount(Long walletId) {
        Deque<Instant> queue = attempts.computeIfAbsent(walletId, id -> new ArrayDeque<>());
        synchronized (queue) {
            Instant cutoff = Instant.now().minusSeconds(WINDOW_SECONDS);
            while (!queue.isEmpty() && queue.peekFirst().isBefore(cutoff)) queue.removeFirst();
            queue.addLast(Instant.now());
            return queue.size();
        }
    }
}
