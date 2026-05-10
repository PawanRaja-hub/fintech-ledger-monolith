package com.portfolio.fintech.user;

import com.portfolio.fintech.common.BusinessException;
import com.portfolio.fintech.common.Role;
import com.portfolio.fintech.user.dto.OnboardUserRequest;
import com.portfolio.fintech.user.dto.UserResponse;
import com.portfolio.fintech.wallet.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final WalletService walletService;

    public UserService(AppUserRepository users, PasswordEncoder encoder, WalletService walletService) {
        this.users = users;
        this.encoder = encoder;
        this.walletService = walletService;
    }

    @Transactional
    public UserResponse onboard(OnboardUserRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already onboarded");
        }
        var user = users.save(new AppUser(request.email(), encoder.encode(request.password()), request.fullName(), Role.CUSTOMER));
        walletService.createWalletFor(user);
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
