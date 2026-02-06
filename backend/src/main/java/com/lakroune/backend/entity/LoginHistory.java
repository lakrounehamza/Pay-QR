package com.lakroune.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String ipAddress;
    private String device;
    private LocalDateTime createdAt;

    @ManyToOne
    private User user;
}
