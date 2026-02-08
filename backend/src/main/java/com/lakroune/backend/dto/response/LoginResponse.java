package com.lakroune.backend.dto.response;

public record LoginResponse(
        UserResponse user,
        String token
) {
}
