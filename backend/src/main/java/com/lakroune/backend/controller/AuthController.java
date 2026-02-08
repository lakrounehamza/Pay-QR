package com.lakroune.backend.controller;

import com.lakroune.backend.dto.request.LoginRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.LoginResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private IAuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
    @PostMapping("/signin")
    public  ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        return  ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
    @PostMapping("/logout")
    public  ResponseEntity<Map> logout(HttpServletRequest reques){
        return  ResponseEntity.status(HttpStatus.OK).body(authService.logout(reques));
    }
}
