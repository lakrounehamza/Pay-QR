package com.lakroune.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lakroune.backend.enums.OwnerType;
import lombok.ToString;

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
    @Column(columnDefinition = "VARCHAR(50)")
    private OwnerType ownerType;

    private BigDecimal amount;
    private LocalDateTime dateExpiration;
    private Boolean isUsed;
    private String createdAt;

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
