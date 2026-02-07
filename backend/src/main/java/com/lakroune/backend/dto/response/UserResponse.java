package com.lakroune.backend.dto.response;


import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String telephone,
        UserRole role,
        UserStatus status,
        UUID enterpriseId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
