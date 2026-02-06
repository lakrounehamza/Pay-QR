package com.lakroune.backend.entity;

import com.lakroune.backend.enums.VerificationStatus;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
