package com.lakroune.backend.service;

public interface ISmsService {

    
    void sendOtp(String phone, String code);
}
