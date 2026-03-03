package com.lakroune.backend.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record QrMarkUsedRequest(
        @NotNull(message = "QR Code ID must not be null")
        UUID qrCodeId
) {
}
