package com.portfolio.fintech.payment;

import com.portfolio.fintech.common.ApiResponse;
import com.portfolio.fintech.payment.dto.PaymentResponse;
import com.portfolio.fintech.payment.dto.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/transfers")
    public ApiResponse<PaymentResponse> transfer(Authentication auth, @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                 @Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok("Transfer accepted", paymentService.transfer(auth.getName(), idempotencyKey, request));
    }
}
