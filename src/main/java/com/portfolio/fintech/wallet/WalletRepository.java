package com.portfolio.fintech.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserEmail(String email);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findForUpdate(@Param("id") Long id);

    @Query(value = "select available_balance from wallets where id = :walletId", nativeQuery = true)
    BigDecimal currentBalanceNative(@Param("walletId") Long walletId);
}
