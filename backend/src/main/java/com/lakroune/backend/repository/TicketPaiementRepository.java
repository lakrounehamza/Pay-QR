package com.lakroune.backend.repository;

import com.lakroune.backend.entity.TicketPaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketPaiementRepository extends JpaRepository<TicketPaiement, UUID> {
}
