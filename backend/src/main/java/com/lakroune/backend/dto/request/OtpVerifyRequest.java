package com.lakroune.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "OTP code is required")
        String code
) {}
