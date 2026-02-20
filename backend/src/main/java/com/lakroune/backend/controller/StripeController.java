package com.lakroune.backend.controller;

import com.lakroune.backend.config.StripeConfig;
import com.lakroune.backend.dto.request.DepositRequest;
import com.lakroune.backend.dto.request.WithdrawalRequest;
import com.lakroune.backend.dto.response.OperationResponse;
import com.lakroune.backend.dto.response.StripePaymentIntentResponse;
import com.lakroune.backend.dto.response.WithdrawalResponse;
import com.lakroune.backend.service.IStripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final IStripeService stripeService;
    private final StripeConfig stripeConfig;

    
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", stripeConfig.getPublicKey()));
    }

    
    @PostMapping("/deposit/create-intent")
    public ResponseEntity<StripePaymentIntentResponse> createDepositIntent(
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stripeService.createDepositIntent(request));
    }

    
    @PostMapping("/deposit/confirm")
    public ResponseEntity<OperationResponse> confirmDeposit(
            @RequestParam UUID accountId,
            @RequestParam String paymentIntentId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeService.confirmDeposit(accountId, paymentIntentId));
    }

    
    @PostMapping("/withdrawal")
    public ResponseEntity<WithdrawalResponse> createWithdrawal(
            @Valid @RequestBody WithdrawalRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stripeService.createWithdrawal(request));
    }
}
