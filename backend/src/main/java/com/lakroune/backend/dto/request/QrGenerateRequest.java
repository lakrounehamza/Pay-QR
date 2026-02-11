package com.lakroune.backend.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QrGenerateRequest(
        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotNull(message = "operatoin ID must not be null")
        UUID accountId
) {
}