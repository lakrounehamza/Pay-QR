package com.lakroune.backend.mapper;

import com.lakroune.backend.dto.request.CreateOperationRequest;
import com.lakroune.backend.dto.response.OperationResponse;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.Operation;
import com.lakroune.backend.entity.QrCode;
import com.lakroune.backend.entity.TicketPaiement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OperationMapper {

    @Mapping(source = "accountSource.id", target = "sourceAccountId")
    @Mapping(source = "accountDestination.id", target = "destinationAccountId")
    @Mapping(source = "ticketPaiement.id", target = "ticketPaiementId")
    @Mapping(source = "qrCode.id", target = "qrCodeId")
    OperationResponse toResponse(Operation entity);


    @Mapping(target = "accountSource", expression = "java(mapAccount(request.sourceAccountId()))")
    @Mapping(target = "accountDestination", expression = "java(mapAccount(request.destinationAccountId()))")
    @Mapping(target = "qrCode", expression = "java(mapQrCode(request.qrCodeId()))")
     Operation toEntity(CreateOperationRequest request);


    default Account mapAccount(UUID id) {
        if (id == null) return null;
        Account account = new Account();
        account.setId(id);
        return account;
    }

    default QrCode mapQrCode(UUID id) {
        if (id == null) return null;
        QrCode qrCode = new QrCode();
        qrCode.setId(id);
        return qrCode;
    }

    default TicketPaiement mapTicket(UUID id) {
        if (id == null) return null;
        TicketPaiement ticket = new TicketPaiement();
        ticket.setId(id);
        return ticket;
    }
}