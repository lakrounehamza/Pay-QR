package com.lakroune.backend.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QrResponse(
        UUID rqId,
        BigDecimal amount,
        UUID expediteurAccountId,
        String expediteurNom,
        String expediteurPrenom,
        @JsonProperty("isUsed") boolean isUsed,
        String qrCodeImage
) {
}
