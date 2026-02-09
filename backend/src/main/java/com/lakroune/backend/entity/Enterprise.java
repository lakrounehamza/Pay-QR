package com.lakroune.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lakroune.backend.enums.EnterpriseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nom;
    private String email;
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private EnterpriseStatus statut;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "enterprise")
    @JsonIgnore
    @ToString.Exclude
    private List<User> users;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}