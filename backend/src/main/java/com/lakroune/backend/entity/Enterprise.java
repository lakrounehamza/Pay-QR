package com.lakroune.backend.entity;

import com.lakroune.backend.enums.EnterpriseStatus;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nom;
    private String email;
    private String telephone;

    @Enumerated(EnumType.STRING)
    private EnterpriseStatus statut;

    private String createdAt;

    @OneToMany(mappedBy = "enterprise")
    private List<User> users;
}