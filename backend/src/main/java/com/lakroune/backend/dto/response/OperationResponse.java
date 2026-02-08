package com.lakroune.backend.dto.response;

import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OperationResponse(
        UUID id,
        OperationType type,
        BigDecimal amount,
        OperationStatus status,
        UUID sourceAccountId,
        UUID destinationAccountId,
        LocalDateTime createdAt,
        UUID ticketPaiementId,
        UUID qrCodeId
) {
}
