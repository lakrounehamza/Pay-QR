package com.lakroune.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record QrResponse(
        UUID rqId,
        BigDecimal amount,
        UUID expediteurAccountId,
        String expediteurNom,
        String expediteurPrenom,
        @JsonProperty("isUsed") boolean isUsed
) {
}
