package com.lakroune.backend.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.UserRole;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByTelephone(String telephone);
    List<User> findByEnterprise_Id(UUID enterpriseId);
    Optional<User> findByEnterprise_IdAndRole(UUID enterpriseId, UserRole role);

    @Query("select count(*) from User ")
    Integer getUserCount();
}
