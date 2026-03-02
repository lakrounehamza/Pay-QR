package com.lakroune.backend.dto.response;

import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String ref,
        OwnerType ownerType,
        BigDecimal solde,
        CompteStatus status,
        User user,
        LocalDateTime createdAt
) {
}
