package com.lakroune.backend.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lakroune.backend.dto.request.QrGenerateRequest;
import com.lakroune.backend.dto.response.CloudinaryResponse;
import com.lakroune.backend.dto.response.QrResponse;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.QrCode;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.QrCodeRepository;
import com.lakroune.backend.service.ICloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrService {

    private final QrCodeRepository qrCodeRepository;
    private final AccountRepository accountRepository;
    private final ICloudinaryService cloudinaryService;

    private static final int QR_IMAGE_SIZE = 300;
    private static final String QR_FOLDER = "qr-codes";


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

        byte[] qrCodeImageBytes = generateQrImageBytes(qrCode.getId().toString());
        String qrFileName = "qr_" + qrCode.getId();
        CloudinaryResponse uploadResponse = cloudinaryService.uploadBytes(qrCodeImageBytes, qrFileName, QR_FOLDER);
        String qrCodeImageUrl = uploadResponse.url();

        qrCode.setQrCodeImageUrl(qrCodeImageUrl);
        qrCode = qrCodeRepository.save(qrCode);

        return new QrResponse(
                qrCode.getId(),
                qrCode.getAmount(),
                account.getId(),
                account.getUser().getNom(),
                account.getUser().getPrenom(),
            false,
            qrCodeImageUrl
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
                qrCode.getIsUsed(),
                qrCode.getQrCodeImageUrl()
        );
    }

    public QrResponse markQrCodeAsUsed(UUID qrCodeId) {
        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new com.lakroune.backend.exception.NotFoundException("QR Code not found"));
        
        if (qrCode.getIsUsed()) {
            throw new RuntimeException("QR Code has already been used");
        }
        
        if (LocalDateTime.now().isAfter(qrCode.getDateExpiration())) {
            throw new RuntimeException("QR Code has expired");
        }
        
        qrCode.setIsUsed(true);
        qrCode = qrCodeRepository.save(qrCode);
        
        log.info("QR Code marked as used: id={} account={}", qrCode.getId(), qrCode.getAccount().getId());
        
        return new QrResponse(
                qrCode.getId(),
                qrCode.getAmount(),
                qrCode.getAccount().getId(),
                qrCode.getAccount().getUser().getNom(),
                qrCode.getAccount().getUser().getPrenom(),
                qrCode.getIsUsed(),
                qrCode.getQrCodeImageUrl()
        );
    }

    private byte[] generateQrImageBytes(String payload) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, QR_IMAGE_SIZE, QR_IMAGE_SIZE);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code image", e);
            throw new RuntimeException("Unable to generate QR code image", e);
        }
    }
}