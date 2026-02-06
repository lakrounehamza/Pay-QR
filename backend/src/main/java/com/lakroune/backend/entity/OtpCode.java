package com.lakroune.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String code;
    private LocalDateTime expirationDate;
    private Boolean used;

    @ManyToOne
    private User user;
}
