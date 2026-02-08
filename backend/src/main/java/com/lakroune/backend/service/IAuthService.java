package com.lakroune.backend.service;

import com.lakroune.backend.dto.request.LoginRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.LoginResponse;
import com.lakroune.backend.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    UserResponse register(RegisterRequest  request);
    Map<String, String> logout(HttpServletRequest request);

}
