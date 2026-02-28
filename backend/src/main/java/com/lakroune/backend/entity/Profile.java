package com.lakroune.backend.entity;

import com.lakroune.backend.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String cin;
    private Date dateNaissance;
    private String documentType;
    private String documentImageUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
