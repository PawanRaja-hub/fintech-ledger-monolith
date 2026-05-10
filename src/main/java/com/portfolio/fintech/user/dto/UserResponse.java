package com.portfolio.fintech.user.dto;

import com.portfolio.fintech.common.Role;

public record UserResponse(Long id, String email, String fullName, Role role) {}
