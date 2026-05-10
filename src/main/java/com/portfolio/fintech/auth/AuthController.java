package com.portfolio.fintech.auth;

import com.portfolio.fintech.auth.dto.AuthResponse;
import com.portfolio.fintech.auth.dto.LoginRequest;
import com.portfolio.fintech.common.ApiResponse;
import com.portfolio.fintech.user.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository users;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, AppUserRepository users) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.users = users;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = users.findByEmail(request.email()).orElseThrow();
        return ApiResponse.ok("Authenticated", new AuthResponse(jwtService.generate(user.getEmail(), user.getRole().name()), "Bearer", user.getRole().name()));
    }
}
