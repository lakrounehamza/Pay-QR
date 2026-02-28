package com.lakroune.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.lakroune.backend.enums.OwnerType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;

    private BigDecimal solde;
    private LocalDateTime dateExpiration;
    private Boolean isUsed;
    private String createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    @SuppressWarnings("unused")
    private void onCreate() {
        createdAt = LocalDateTime.now().toString();
        if (isUsed == null) isUsed = false;
    }
}
