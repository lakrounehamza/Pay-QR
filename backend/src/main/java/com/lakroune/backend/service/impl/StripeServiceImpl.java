package com.lakroune.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lakroune.backend.config.StripeConfig;
import com.lakroune.backend.dto.request.DepositRequest;
import com.lakroune.backend.dto.request.WithdrawalRequest;
import com.lakroune.backend.dto.response.OperationResponse;
import com.lakroune.backend.dto.response.StripePaymentIntentResponse;
import com.lakroune.backend.dto.response.WithdrawalResponse;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.Operation;
import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.OperationType;
import com.lakroune.backend.exception.InsufficientBalanceException;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.mapper.OperationMapper;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.OperationRepository;
import com.lakroune.backend.service.IStripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StripeServiceImpl implements IStripeService {

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;
    private final StripeConfig stripeConfig;

    

    @Override
    public StripePaymentIntentResponse createDepositIntent(DepositRequest request) {
        
        accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        
        long amountInCents = request.amount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(stripeConfig.getCurrency())
                    
                    .addPaymentMethodType("card")
                    
                    .putMetadata("accountId", request.accountId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return new StripePaymentIntentResponse(
                    intent.getId(),
                    intent.getClientSecret(),
                    request.amount(),
                    stripeConfig.getCurrency(),
                    intent.getStatus()
            );
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe PaymentIntent: " + e.getMessage(), e);
        }
    }

    @Override
    public OperationResponse confirmDeposit(UUID accountId, String paymentIntentId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(intent.getStatus())) {
                throw new RuntimeException(
                        "Payment has not succeeded yet. Current status: " + intent.getStatus());
            }

            
            String metaAccountId = intent.getMetadata().get("accountId");
            if (metaAccountId == null || !metaAccountId.equals(accountId.toString())) {
                throw new RuntimeException("PaymentIntent does not belong to this account");
            }

            BigDecimal amount = BigDecimal.valueOf(intent.getAmount())
                    .divide(BigDecimal.valueOf(100));

            
            account.setSolde(account.getSolde().add(amount));
            accountRepository.save(account);

            
            Operation operation = Operation.builder()
                    .type(OperationType.DEPOSIT)
                    .amount(amount)
                    .status(OperationStatus.SUCCESS)
                    .accountDestination(account)
                    .createdAt(LocalDateTime.now())
                    .build();

            Operation saved = operationRepository.save(operation);
            return operationMapper.toResponse(saved);

        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve PaymentIntent: " + e.getMessage(), e);
        }
    }

    

    @Override
    public WithdrawalResponse createWithdrawal(WithdrawalRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getSolde().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        
        account.setSolde(account.getSolde().subtract(request.amount()));
        accountRepository.save(account);

        
        Operation operation = Operation.builder()
                .type(OperationType.WITHDRAWAL)
                .amount(request.amount())
                .status(OperationStatus.PENDING)
                .accountSource(account)
                .createdAt(LocalDateTime.now())
                .build();

        Operation saved = operationRepository.save(operation);

        return new WithdrawalResponse(
                saved.getId(),
                saved.getType(),
                saved.getAmount(),
                saved.getStatus(),
                account.getId(),
                null,
                saved.getCreatedAt()
        );
    }
}
