package com.lakroune.backend.dto.response;

import java.math.BigDecimal;

public record EnterpriseStatisticsResponse(
        long totalEmployees,
        long activeEmployees,
        long blockedEmployees,
        long totalOperations,
        long successOperations,
        long failedOperations,
        BigDecimal totalBalance
) {
}
