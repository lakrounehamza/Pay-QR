package com.lakroune.backend.dto.response;

import java.util.Date;
import java.util.UUID;

import com.lakroune.backend.enums.VerificationStatus;

public record ProfileResponse(
        UUID id,
        String cin,
        Date dateNaissance,
        String documentType,
        String documentImageUrl,
        VerificationStatus verificationStatus
) {
}
