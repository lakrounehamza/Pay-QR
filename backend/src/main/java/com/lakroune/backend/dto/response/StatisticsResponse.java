package com.lakroune.backend.dto.response;

import java.math.BigDecimal;

public record StatisticsResponse(
        long totalUsers,
        long activeUsers,
        long blockedUsers,
        long totalAccounts,
        long activeAccounts,
        long closedAccounts,
        long totalEnterprises,
        long activeEnterprises,
        long suspendedEnterprises,
        long totalOperations,
        long successOperations,
        long failedOperations,
        long pendingOperations,
        BigDecimal totalVolume
) {
}
