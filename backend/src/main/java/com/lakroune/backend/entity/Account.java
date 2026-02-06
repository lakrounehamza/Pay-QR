package com.lakroune.backend.entity;



import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;

    private BigDecimal solde;

    @Enumerated(EnumType.STRING)
    private CompteStatus status;

    @ManyToOne
    private User user;

    private String createdAt;
}