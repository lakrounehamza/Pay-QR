package com.lakroune.backend.repository;

import com.lakroune.backend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
}
