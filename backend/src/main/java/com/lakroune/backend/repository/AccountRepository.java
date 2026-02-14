package com.lakroune.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakroune.backend.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUser_Id(UUID userId);
    List<Account> findByUser_Enterprise_Id(UUID enterpriseId);
}
