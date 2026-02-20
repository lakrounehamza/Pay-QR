package com.lakroune.backend.service;

import com.lakroune.backend.dto.request.DepositRequest;
import com.lakroune.backend.dto.request.WithdrawalRequest;
import com.lakroune.backend.dto.response.OperationResponse;
import com.lakroune.backend.dto.response.StripePaymentIntentResponse;
import com.lakroune.backend.dto.response.WithdrawalResponse;

import java.util.UUID;

public interface IStripeService {

    
    StripePaymentIntentResponse createDepositIntent(DepositRequest request);

    
    OperationResponse confirmDeposit(UUID accountId, String paymentIntentId);

    
    WithdrawalResponse createWithdrawal(WithdrawalRequest request);
}
