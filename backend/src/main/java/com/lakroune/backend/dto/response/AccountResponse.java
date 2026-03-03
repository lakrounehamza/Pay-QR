package com.lakroune.backend.dto.response;

import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String ref,
        OwnerType ownerType,
        BigDecimal solde,
        CompteStatus status,
        User user,
        LocalDateTime createdAt
) {
}
