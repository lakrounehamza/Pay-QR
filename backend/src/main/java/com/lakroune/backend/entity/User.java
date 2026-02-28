package com.lakroune.backend.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.enums.UserStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String telephone;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OtpCode> otpCodes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<LoginHistory> loginHistories;

    @ManyToOne
    private Enterprise enterprise;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = UserStatus.ACTIVE;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
