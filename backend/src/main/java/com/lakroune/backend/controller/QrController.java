package com.lakroune.backend.controller;

import com.lakroune.backend.dto.request.QrGenerateRequest;
import com.lakroune.backend.dto.response.QrResponse;
import com.lakroune.backend.service.impl.QrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Slf4j
public class QrController {

    private final QrService qrService;

    @PostMapping("/generate")
    public ResponseEntity<QrResponse> generateQrCode(
            @Valid @RequestBody QrGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(qrService.generateQRCode(request));
    }


    @PostMapping(value = "/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> decodeQrCode(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(qrService.decodeQRCode(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getQrById(@PathVariable UUID id) {
        return ResponseEntity.ok(qrService.getQrById(id));
    }
}