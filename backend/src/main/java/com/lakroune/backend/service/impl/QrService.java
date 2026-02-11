package com.lakroune.backend.service.impl;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.lakroune.backend.dto.request.QrGenerateRequest;
import com.lakroune.backend.dto.response.QrResponse;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.QrCode;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrService {

    private final QrCodeRepository qrCodeRepository;
    private final AccountRepository accountRepository;


    public QrResponse generateQRCode(QrGenerateRequest request) {

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new com.lakroune.backend.exception.NotFoundException("Account not found"));
        if (account.getStatus().equals(CompteStatus.CLOSED))
            throw new com.lakroune.backend.exception.NotFoundException("This account is closed");

        QrCode qrCode = QrCode.builder()
                .amount(request.amount())
                .ownerType(OwnerType.USER)
                .dateExpiration(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .account(account)
                .build();

        qrCode = qrCodeRepository.save(qrCode);

        log.info("QR Code created id={} amount={} account={}", qrCode.getId(), request.amount(), account.getId());

        return new QrResponse(
                qrCode.getId(),
                qrCode.getAmount(),
                account.getId(),
                account.getUser().getNom(),
                account.getUser().getPrenom(),
                false
        );
    }

    public QrResponse decodeQRCode(MultipartFile file) throws IOException {

        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

        if (bufferedImage == null) {
            throw new IllegalArgumentException("Invalid image file");
        }

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);

            log.info("QR decoded successfully: {}", result.getText());
            String text = result.getText();
            UUID id = UUID.fromString(text);

            return getQrById(id);
        } catch (NotFoundException e) {
            throw new RuntimeException("No QR code found in image");
        }
    }

    public QrResponse getQrById(UUID id) {
        QrCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new com.lakroune.backend.exception.NotFoundException("QR Code not found"));
        return new QrResponse(
                qrCode.getId(),
                qrCode.getAmount(),
                qrCode.getAccount().getId(),
                qrCode.getAccount().getUser().getNom(),
                qrCode.getAccount().getUser().getPrenom(),
                qrCode.getIsUsed()
        );
    }
}