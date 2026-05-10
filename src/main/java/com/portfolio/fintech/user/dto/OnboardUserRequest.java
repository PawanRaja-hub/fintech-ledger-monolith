package com.portfolio.fintech.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardUserRequest(
        @Email String email,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotBlank @Size(max = 80) String fullName
) {}
