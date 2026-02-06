package com.lakroune.backend.entity;

import com.lakroune.backend.enums.EnterpriseStatus;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nom;
    private String email;
    private String telephone;

    @Enumerated(EnumType.STRING)
    private EnterpriseStatus statut;

    private String createdAt;

    @OneToMany(mappedBy = "enterprise")
    private List<User> users;
}