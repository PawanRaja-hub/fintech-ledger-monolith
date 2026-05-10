package com.portfolio.fintech.common;

import java.time.Instant;

public record ApiResponse<T>(Instant timestamp, String message, T data) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(Instant.now(), message, data);
    }
}
