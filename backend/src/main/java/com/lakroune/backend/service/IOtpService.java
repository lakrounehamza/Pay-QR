package com.lakroune.backend.service;

import com.lakroune.backend.dto.request.OtpSendRequest;
import com.lakroune.backend.dto.request.OtpVerifyRequest;
import com.lakroune.backend.dto.response.OtpResponse;

public interface IOtpService {

    
    OtpResponse sendOtp(OtpSendRequest request);

    
    boolean verifyOtp(OtpVerifyRequest request);
}
