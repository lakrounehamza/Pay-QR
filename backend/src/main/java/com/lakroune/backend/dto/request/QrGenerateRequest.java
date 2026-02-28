package com.lakroune.backend.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QrGenerateRequest(
        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotNull(message = "Sender ID must not be null")
        UUID expediteurId,
        @NotNull(message = "Recipient ID must not be null")
        UUID destinataireId,
        @NotNull(message = "OTP code must not be null")
        UUID otpCode
) {
}