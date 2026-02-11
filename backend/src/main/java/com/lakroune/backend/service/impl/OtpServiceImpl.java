package com.lakroune.backend.service.impl;

import com.lakroune.backend.dto.request.OtpSendRequest;
import com.lakroune.backend.dto.request.OtpVerifyRequest;
import com.lakroune.backend.dto.response.OtpResponse;
import com.lakroune.backend.entity.OtpCode;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.repository.OtpCodeRepository;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.IOtpService;
import com.lakroune.backend.service.ISmsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OtpServiceImpl implements IOtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final ISmsService smsService;

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public OtpResponse sendOtp(OtpSendRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getTelephone() == null || user.getTelephone().isBlank()) {
            throw new IllegalStateException("User has no phone number registered");
        }

        
        otpCodeRepository.findTopByUserAndUsedFalseOrderByExpirationDateDesc(user)
                .ifPresent(existing -> {
                    long secondsLeft = ChronoUnit.SECONDS.between(
                            LocalDateTime.now(),
                            existing.getExpirationDate()
                    );
                    long resendAllowedAfter = (long) expirationMinutes * 60 - 60;
                    if (secondsLeft > resendAllowedAfter) {
                        throw new IllegalStateException(
                                "Please wait before requesting a new OTP");
                    }
                });

        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();

        OtpCode otp = OtpCode.builder()
                .code(code)
                .expirationDate(now.plusMinutes(expirationMinutes))
                .used(false)
                .user(user)
                .build();

        otpCodeRepository.save(otp);
        smsService.sendOtp(user.getTelephone(), code);

        String maskedPhone = maskPhone(user.getTelephone());
        log.info("OTP sent to {} for user {}", maskedPhone, user.getEmail());

        return new OtpResponse(
                "OTP sent successfully",
                maskedPhone,
                (long) expirationMinutes * 60
        );
    }

    @Override
    public boolean verifyOtp(OtpVerifyRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return otpCodeRepository
                .findTopByUserAndCodeAndUsedFalseOrderByExpirationDateDesc(user, request.code())
                .filter(otp -> otp.getExpirationDate().isAfter(LocalDateTime.now()))
                .map(otp -> {
                    otp.setUsed(true);
                    otpCodeRepository.save(otp);
                    log.info("OTP verified for user {}", user.getEmail());
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("Invalid or expired OTP for user {}", user.getEmail());
                    return false;
                });
    }

    
    @Scheduled(fixedRate = 600_000)
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteByExpirationDateBefore(LocalDateTime.now());
        log.debug("Expired OTPs cleaned up");
    }

    

    private String generateCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int num = RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", num);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, Math.min(4, phone.length() - 2))
                + "*".repeat(Math.max(0, phone.length() - 6))
                + phone.substring(phone.length() - 2);
    }
}
