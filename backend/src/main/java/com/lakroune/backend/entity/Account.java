package com.lakroune.backend.entity;

import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false, length = 15)
    private String ref; //ref ,
    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;
    @Column(nullable = false)
    private BigDecimal solde;
    @Enumerated(EnumType.STRING)
    private CompteStatus status;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private LocalDateTime createdAt;
}