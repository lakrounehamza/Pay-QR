package com.lakroune.backend.dto.response;


import java.time.LocalDateTime;
import java.util.UUID;

import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.enums.UserStatus;

public record UserResponse(
        UUID id,
        String email,
        String nom,
        String prenom,
        String telephone,
        UserRole role,
        UserStatus status,
        UUID enterpriseId,
        String enterpriseName,
        ProfileResponse profile,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
