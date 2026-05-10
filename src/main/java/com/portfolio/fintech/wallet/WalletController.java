package com.portfolio.fintech.wallet;

import com.portfolio.fintech.common.ApiResponse;
import com.portfolio.fintech.wallet.dto.WalletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;
    public WalletController(WalletService walletService) { this.walletService = walletService; }

    @GetMapping("/me")
    public ApiResponse<WalletResponse> me(Authentication authentication) {
        return ApiResponse.ok("Wallet loaded", walletService.myWallet(authentication.getName()));
    }
}
