package com.lakroune.backend.repository;

import com.lakroune.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository   extends JpaRepository<Account, UUID> {
}
