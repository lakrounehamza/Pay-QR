package com.lakroune.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String ipAddress;
    private String device;
    private LocalDateTime createdAt;

    @ManyToOne
    private User user;
}
