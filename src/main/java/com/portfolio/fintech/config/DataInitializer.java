package com.portfolio.fintech.config;

import com.portfolio.fintech.common.Role;
import com.portfolio.fintech.user.AppUser;
import com.portfolio.fintech.user.AppUserRepository;
import com.portfolio.fintech.wallet.WalletService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(AppUserRepository users, PasswordEncoder encoder, WalletService walletService) {
        return args -> {
            ensure(users, encoder, walletService, "alice@demo.local", "Alice Customer", Role.CUSTOMER);
            ensure(users, encoder, walletService, "bob@demo.local", "Bob Customer", Role.CUSTOMER);
            ensure(users, encoder, walletService, "analyst@demo.local", "Fraud Analyst", Role.ANALYST);
            ensure(users, encoder, walletService, "admin@demo.local", "Platform Admin", Role.ADMIN);
        };
    }

    private void ensure(AppUserRepository users, PasswordEncoder encoder, WalletService walletService, String email, String name, Role role) {
        users.findByEmail(email).ifPresentOrElse(existing -> {
            // Local file databases often survive code changes. Refreshing demo
            // credentials keeps Postman/Swagger testing predictable after reruns.
            existing.refreshDemoPassword(encoder.encode("Password@123"));
        }, () -> {
            var user = users.save(new AppUser(email, encoder.encode("Password@123"), name, role));
            if (role == Role.CUSTOMER) {
                walletService.createWalletFor(user);
            }
        });
    }
}
