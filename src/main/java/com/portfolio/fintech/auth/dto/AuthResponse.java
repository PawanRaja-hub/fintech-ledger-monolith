package com.portfolio.fintech.auth.dto;

public record AuthResponse(String token, String tokenType, String role) {}
