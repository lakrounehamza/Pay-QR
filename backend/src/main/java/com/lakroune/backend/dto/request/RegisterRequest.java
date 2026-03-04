package com.lakroune.backend.dto.request;

import com.lakroune.backend.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Date;

public record RegisterRequest (
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Telephone is required")
        @Pattern(
                regexp = "^[0-9+]{8,15}$",
                message = "Invalid telephone number"
        )
        String telephone,
        @NotBlank(message = "nom is required")
        String nom,
        @NotBlank(message = "prenom is required")
        String  prenom,
        @NotBlank(message = "Password is required")
        String password,

        @NotNull(message = "Role is required")
        UserRole role,

        String enterpriseId,

        @NotBlank(message = "CIN is required")
        String cin,
        Date dateNaissance,
        String documentType,
        String documentImageUrl
){
}
