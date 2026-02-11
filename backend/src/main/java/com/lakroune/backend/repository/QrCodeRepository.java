package com.lakroune.backend.repository;

import com.lakroune.backend.entity.QrCode;
import com.lakroune.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {


}
