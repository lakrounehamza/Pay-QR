package com.lakroune.backend.controller;

import com.lakroune.backend.dto.request.OtpSendRequest;
import com.lakroune.backend.dto.request.OtpVerifyRequest;
import com.lakroune.backend.dto.response.OtpResponse;
import com.lakroune.backend.service.IOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final IOtpService otpService;

    
    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(otpService.sendOtp(request));
    }

    
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        boolean valid = otpService.verifyOtp(request);
        if (valid) {
            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "OTP verified successfully"
            ));
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "verified", false,
                        "message", "Invalid or expired OTP"
                ));
    }
}
