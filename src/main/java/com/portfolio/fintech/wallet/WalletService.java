package com.portfolio.fintech.wallet;

import com.portfolio.fintech.common.BusinessException;
import com.portfolio.fintech.user.AppUser;
import com.portfolio.fintech.wallet.dto.WalletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {
    private final WalletRepository wallets;

    public WalletService(WalletRepository wallets) { this.wallets = wallets; }

    @Transactional
    public Wallet createWalletFor(AppUser user) {
        return wallets.save(new Wallet(user));
    }

    @Transactional(readOnly = true)
    public WalletResponse myWallet(String email) {
        var wallet = wallets.findByUserEmail(email).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Wallet not found"));
        return new WalletResponse(wallet.getId(), wallet.getUser().getEmail(), wallet.getAvailableBalance(), wallet.getVersion());
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> allWallets() {
        return wallets.findAll().stream()
                .map(wallet -> new WalletResponse(wallet.getId(), wallet.getUser().getEmail(), wallet.getAvailableBalance(), wallet.getVersion()))
                .toList();
    }

    @Transactional
    public void credit(Wallet wallet, BigDecimal amount) { wallet.credit(amount); }

    @Transactional
    public void debit(Wallet wallet, BigDecimal amount) {
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }
        wallet.debit(amount);
    }
}
