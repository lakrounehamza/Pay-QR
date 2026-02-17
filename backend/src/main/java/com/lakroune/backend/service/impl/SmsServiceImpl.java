package com.lakroune.backend.service.impl;

import com.lakroune.backend.service.ISmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements ISmsService {

    @Value("${sms.twilio.account-sid}")
    private String accountSid;

    @Value("${sms.twilio.auth-token}")
    private String authToken;

    @Value("${sms.twilio.phone-number}")
    private String fromPhone;

    @Value("${sms.dev-mode:false}")
    private boolean devMode;

    @Value("${sms.sender:PayQR}")
    private String sender;

    @PostConstruct
    public void init() {
        if (!devMode) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized (from: {})", fromPhone);
        } else {
            log.info("SMS dev-mode enabled — messages will be logged only");
        }
    }

    @Override
    public void sendOtp(String phone, String code) {
        String messageBody = "Your Pay-QR verification code is: " + code
                + "\nValid for 5 minutes. Do not share it.";

        if (devMode) {
            
            log.info("📱 [DEV] OTP for {}: {}", phone, code);
            return;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(phone),
                    new PhoneNumber(fromPhone),
                    messageBody
            ).create();

            log.info("SMS sent to {} — SID: {}", phone, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phone, e.getMessage());
            throw new RuntimeException("SMS sending failed: " + e.getMessage(), e);
        }
    }
}
