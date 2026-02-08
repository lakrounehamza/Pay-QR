package com.lakroune.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.lakroune.backend.enums.EnterpriseStatus;

public record EnterpriseResponse(
        UUID id,
        String nom,
        String email,
        String telephone,
        EnterpriseStatus statut,
        LocalDateTime createdAt,
        int totalEmployees
) {
}
