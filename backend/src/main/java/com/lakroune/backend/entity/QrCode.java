package com.lakroune.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lakroune.backend.enums.OwnerType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @Column(columnDefinition = "VARCHAR(50)")
    private OwnerType ownerType;

    private BigDecimal amount;
    private LocalDateTime dateExpiration;
    private Boolean isUsed;
    private String createdAt;
    private String qrCodeImageUrl;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonIgnore
    @ToString.Exclude
    private Account account;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now().toString();
        if (isUsed == null) isUsed = false;
        dateExpiration = LocalDateTime.now().plusMinutes(2);
    }
}
