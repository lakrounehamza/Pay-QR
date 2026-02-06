package com.lakroune.backend.entity;

import com.lakroune.backend.enums.OwnerType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
public class TicketPaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;

    private BigDecimal solde;
    private LocalDateTime dateExpiration;
    private Boolean isUsed;
    private String createdAt;
}
