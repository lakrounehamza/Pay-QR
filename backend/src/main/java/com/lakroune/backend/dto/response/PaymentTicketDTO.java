package com.lakroune.backend.dto.response;

import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.OperationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTicketDTO(
        UUID operationId,
        OperationType type,
        BigDecimal amount,
        OperationStatus status,
        String sourceAccountRef,
        String sourceUserName,
        String destinationAccountRef,
        String destinationUserName,
        LocalDateTime createdAt,
        UUID qrCodeId
) {
}
