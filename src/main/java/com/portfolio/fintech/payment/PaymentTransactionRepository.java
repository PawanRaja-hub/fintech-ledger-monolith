package com.portfolio.fintech.payment;

import com.portfolio.fintech.common.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByReference(String reference);
    List<PaymentTransaction> findTop50ByStatusOrderByIdDesc(TransactionStatus status);
}
