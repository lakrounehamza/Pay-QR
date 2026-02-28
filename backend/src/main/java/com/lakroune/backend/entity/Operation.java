package com.lakroune.backend.entity;

import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.OperationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account accountSource;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account accountDestination;

    private LocalDateTime createdAt;

    @OneToOne
    private TicketPaiement ticketPaiement;

    @OneToOne
    private QrCode qrCode;
}
