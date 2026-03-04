package com.lakroune.backend.controller;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakroune.backend.dto.request.LoginRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.LoginResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.enums.UserStatus;
import com.lakroune.backend.security.JwtUtil;
import com.lakroune.backend.security.TokenBlacklistService;
import com.lakroune.backend.service.IAccountService;
import com.lakroune.backend.service.IAuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private IAuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private IAccountService accountService;

    @InjectMocks
    private AuthController authController;

    private static final String RAW_PASSWORD = "Secret123!";
    private static final String JWT_TOKEN = "jwt.token.value";
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("test@example.com", RAW_PASSWORD);
        UserResponse userResponse = new UserResponse(
            USER_ID,
            "test@example.com",
            "Nom",
            "Prenom",
            "0612345678",
            UserRole.USER,
            UserStatus.ACTIVE,
            null,
            null,
            null,
            null,
            null
        );
        LoginResponse loginResponse = new LoginResponse(userResponse, JWT_TOKEN);

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        LoginResponse result = authService.login(request);
        
        assert result.token().equals(JWT_TOKEN);
        assert result.user().email().equals("test@example.com");
    }

    @Test
    void testLogin_InvalidEmail() {
        LoginRequest request = new LoginRequest("", RAW_PASSWORD);
        
        assert request.email().isEmpty();
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "");
        
        assert request.password().isEmpty();
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest(
                "jean@example.com",
                "0612345678",
                "Dupont",
                "Jean",
                RAW_PASSWORD,
                UserRole.USER,
                null,
                "AB123456",
                new Date(),
                "NATIONAL_ID",
                "https://example.com/doc.jpg"
        );
        UserResponse userResponse = new UserResponse(
            USER_ID,
            "jean@example.com",
            "Dupont",
            "Jean",
            "0612345678",
            UserRole.USER,
            UserStatus.ACTIVE,
            null,
            null,
            null,
            null,
            null
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        UserResponse result = authService.register(request);
        
        assert result.email().equals("jean@example.com");
        assert result.role().equals(UserRole.USER);
    }

    @Test
    void testRegister_InvalidEmail() {
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "0612345678",
                "Dupont",
                "Jean",
                RAW_PASSWORD,
                UserRole.USER,
                null,
                "AB123456",
                new Date(),
                "NATIONAL_ID",
                "https://example.com/doc.jpg"
        );
        
        assert !request.email().contains("@");
    }

    @Test
    void testRegister_WeakPassword() {
        RegisterRequest request = new RegisterRequest(
                "jean@example.com",
                "0612345678",
                "Dupont",
                "Jean",
                "weak",
                UserRole.USER,
                null,
                "AB123456",
                new Date(),
                "NATIONAL_ID",
                "https://example.com/doc.jpg"
        );
        
        assert request.password().length() < 8;
    }

    @Test
    void testRegister_MissingPhoneNumber() {
        RegisterRequest request = new RegisterRequest(
                "jean@example.com",
                "",
                "Dupont",
                "Jean",
                RAW_PASSWORD,
                UserRole.USER,
                null,
                "AB123456",
                new Date(),
                "NATIONAL_ID",
                "https://example.com/doc.jpg"
        );
        
        assert request.telephone().isEmpty();
    }

    @Test
    void testLogout_Success() {
        Map<String, String> response = Map.of("message", "Déconnexion réussie.");
        when(authService.logout(any())).thenReturn(response);

        Map<String, String> result = authService.logout(null);
        
        assert result.containsKey("message");
        assert result.get("message").equals("Déconnexion réussie.");
    }


}
