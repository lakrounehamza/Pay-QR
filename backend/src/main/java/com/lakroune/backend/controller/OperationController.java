package com.lakroune.backend.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakroune.backend.dto.request.CreateOperationRequest;
import com.lakroune.backend.dto.response.OperationResponse;
import com.lakroune.backend.dto.response.PaymentTicketDTO;
import com.lakroune.backend.service.IOperationService;
import com.lakroune.backend.service.impl.OperationServiceImpl;
import com.lakroune.backend.service.impl.PdfTicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/operation")
@RequiredArgsConstructor
public class OperationController {

    private final IOperationService operationService;
    private final OperationServiceImpl operationServiceImpl;
    private final PdfTicketService pdfTicketService;

    @PostMapping
    public ResponseEntity<OperationResponse> create(@Valid @RequestBody CreateOperationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operationService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<OperationResponse>> getAllOperation() {
        return ResponseEntity.status(HttpStatus.OK).body(operationService.getAllOperation());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<OperationResponse>> getByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(operationService.getByAccountId(accountId));
    }

    @GetMapping("/ticket/{operationId}")
    public ResponseEntity<PaymentTicketDTO> getPaymentTicket(@PathVariable UUID operationId) {
        PaymentTicketDTO ticket = operationServiceImpl.getPaymentTicket(operationId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/download-ticket/{operationId}")
    public ResponseEntity<byte[]> downloadPaymentTicketPdf(@PathVariable UUID operationId) throws IOException {
        PaymentTicketDTO ticket = operationServiceImpl.getPaymentTicket(operationId);
        byte[] pdfContent = pdfTicketService.generatePaymentTicketPdf(ticket);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payment-receipt-" + operationId + ".pdf");
        headers.setContentLength(pdfContent.length);

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
