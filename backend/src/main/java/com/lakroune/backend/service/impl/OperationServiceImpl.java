package com.lakroune.backend.service.impl;

import com.lakroune.backend.dto.request.CreateOperationRequest;
import com.lakroune.backend.dto.response.OperationResponse;
import com.lakroune.backend.dto.response.PaymentTicketDTO;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.Operation;
import com.lakroune.backend.entity.QrCode;
import com.lakroune.backend.exception.FuncErrorException;
import com.lakroune.backend.exception.InsufficientBalanceException;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.mapper.OperationMapper;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.OperationRepository;
import com.lakroune.backend.repository.QrCodeRepository;
import com.lakroune.backend.service.IOperationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationServiceImpl implements IOperationService {

    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;
    private final AccountRepository accountRepository;
    private final QrCodeRepository qrCodeRepository;

    @Override
    public OperationResponse save(CreateOperationRequest request) {
        Operation operation = operationMapper.toEntity(request);
        Account source = accountRepository
                .findById(operation.getAccountSource().getId())
                .orElseThrow(() ->
                        new NotFoundException("Source account not found"));

        Account destination = accountRepository
                .findById(operation.getAccountDestination().getId())
                .orElseThrow(() ->
                        new NotFoundException("Destination account not found"));

        if (source.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (source.getSolde().compareTo(operation.getAmount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance in source account");

        
        if (request.qrCodeId() != null) {
            QrCode qrCode = qrCodeRepository.findById(request.qrCodeId())
                    .orElseThrow(() -> new NotFoundException("QR Code not found"));
            if (Boolean.TRUE.equals(qrCode.getIsUsed()))
                throw new FuncErrorException("This QR code has already been used");
            if (qrCode.getDateExpiration() != null && qrCode.getDateExpiration().isBefore(LocalDateTime.now()))
                throw new FuncErrorException("This QR code has expired");
            qrCode.setIsUsed(true);
            qrCodeRepository.save(qrCode);
        }

        destination.setSolde(destination.getSolde().add(operation.getAmount()));
        source.setSolde(source.getSolde().subtract(operation.getAmount()));
        accountRepository.save(destination);
        accountRepository.save(source);
        operation.setCreatedAt(LocalDateTime.now());
        Operation operationSaved = operationRepository.save(operation);
        return operationMapper.toResponse(operationSaved);
    }

    @Override
    public List<OperationResponse> getAllOperation() {

        List<Operation> operationList = operationRepository.findAll();

        return operationList.stream()
                .map(operationMapper::toResponse)
                .toList();
    }

    @Override
    public List<OperationResponse> getByAccountId(UUID accountId) {
        return getAllOperation().stream()
                .filter(o -> Objects.equals(o.destinationAccountId(), accountId)
                          || Objects.equals(o.sourceAccountId(), accountId))
                .toList();
    }

    public PaymentTicketDTO getPaymentTicket(UUID operationId) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new NotFoundException("Operation not found"));

        String sourceUserName = operation.getAccountSource().getUser().getPrenom() + " " +
                operation.getAccountSource().getUser().getNom();
        String destinationUserName = operation.getAccountDestination().getUser().getPrenom() + " " +
                operation.getAccountDestination().getUser().getNom();

        return new PaymentTicketDTO(
                operation.getId(),
                operation.getType(),
                operation.getAmount(),
                operation.getStatus(),
                operation.getAccountSource().getRef(),
                sourceUserName,
                operation.getAccountDestination().getRef(),
                destinationUserName,
                operation.getCreatedAt(),
                operation.getQrCode() != null ? operation.getQrCode().getId() : null
        );
    }

}
