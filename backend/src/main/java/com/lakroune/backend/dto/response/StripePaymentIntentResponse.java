package com.lakroune.backend.dto.response;

import java.math.BigDecimal;

public record StripePaymentIntentResponse(
        String paymentIntentId,
        String clientSecret,
        BigDecimal amount,
        String currency,
        String status
) {}
