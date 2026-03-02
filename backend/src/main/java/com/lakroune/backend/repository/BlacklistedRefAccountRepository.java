package com.lakroune.backend.repository;

import com.lakroune.backend.entity.BlacklistedRefAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedRefAccountRepository   extends JpaRepository<BlacklistedRefAccount, Long> {

    boolean existsByRefAccount(String ref);
}