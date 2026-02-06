package com.lakroune.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String code;
    private LocalDateTime expirationDate;
    private Boolean used;

    @ManyToOne
    private User user;
}
