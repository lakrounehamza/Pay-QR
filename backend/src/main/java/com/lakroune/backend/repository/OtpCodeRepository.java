package com.lakroune.backend.repository;

import com.lakroune.backend.entity.OtpCode;
import com.lakroune.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findTopByUserAndCodeAndUsedFalseOrderByExpirationDateDesc(User user, String code);

    
    Optional<OtpCode> findTopByUserAndUsedFalseOrderByExpirationDateDesc(User user);

    
    void deleteByExpirationDateBefore(LocalDateTime dateTime);
}
