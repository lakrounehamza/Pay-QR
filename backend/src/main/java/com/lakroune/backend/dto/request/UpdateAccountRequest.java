package com.lakroune.backend.dto.request;

import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;

import java.math.BigDecimal;

public record UpdateAccountRequest(
        OwnerType ownerType,
        BigDecimal solde,
        CompteStatus status
) {
}
