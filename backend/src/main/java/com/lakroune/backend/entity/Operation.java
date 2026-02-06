package com.lakroune.backend.entity;

import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.OperationType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
