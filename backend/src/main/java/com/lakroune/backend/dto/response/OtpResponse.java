package com.lakroune.backend.dto.response;

public record OtpResponse(
        String message,
        String maskedPhone,     
        Long expiresInSeconds
) {}
