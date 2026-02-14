package com.lakroune.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FundingHistoryResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        String operation,   
        BigDecimal amount,
        LocalDateTime createdAt
) {
}
