package com.lakroune.backend.dto.request;

import com.lakroune.backend.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOperationRequest(
        @NotNull(message = "Operation type is required")
        OperationType type,

        @NotNull(message = "Amount is required")

        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Source account id is required")
        UUID sourceAccountId,

        @NotNull(message = "Destination account id is required")
        UUID destinationAccountId,

        UUID qrCodeId
) {
}
