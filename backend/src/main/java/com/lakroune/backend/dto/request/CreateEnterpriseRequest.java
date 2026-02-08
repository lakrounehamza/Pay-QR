package com.lakroune.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateEnterpriseRequest(
        
        @NotBlank String nom,
        @Email @NotBlank String email,
        @NotBlank String telephone,

        
        @NotBlank String adminNom,
        @NotBlank String adminPrenom,
        @Email @NotBlank String adminEmail,
        @NotBlank
        @Pattern(regexp = "^[0-9+]{8,15}$", message = "Invalid telephone number")
        String adminTelephone,
        @NotBlank String adminPassword
) {
}
