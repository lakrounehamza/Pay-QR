package com.lakroune.backend.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lakroune.backend.dto.request.LoginRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.LoginResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.enums.UserStatus;
import com.lakroune.backend.exception.ConflictException;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.exception.UnauthorizedException;
import com.lakroune.backend.mapper.UserMapper;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.security.JwtUtil;
import com.lakroune.backend.security.TokenBlacklistService;
import com.lakroune.backend.service.IAccountService;
import com.lakroune.backend.service.IProfileService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl – Tests unitaires")
class AuthServiceImplTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private IAccountService accountService;
    @Mock private IProfileService profileService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private UserResponse userResponse;
    private static final String RAW_PASSWORD   = "Secret123!";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String JWT_TOKEN       = "jwt.token.value";
    private static final UUID   USER_ID         = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(USER_ID);
        activeUser.setEmail("test@example.com");
        activeUser.setPassword(HASHED_PASSWORD);
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setRole(UserRole.USER);

        userResponse = new UserResponse(
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
    }

   
    @Nested
    @DisplayName("login()")
    class LoginTests {

        private LoginRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new LoginRequest("test@example.com", RAW_PASSWORD);
        }

        @Test
        @DisplayName(" Succès – retourne LoginResponse avec token")
        void login_success() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(userMapper.toResponse(activeUser)).thenReturn(userResponse);
            when(jwtUtil.generateToken(activeUser)).thenReturn(JWT_TOKEN);

            LoginResponse response = authService.login(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            assertThat(response.user()).isEqualTo(userResponse);

            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).matches(RAW_PASSWORD, HASHED_PASSWORD);
            verify(jwtUtil).generateToken(activeUser);
        }

        @Test
        @DisplayName(" Email introuvable – lève NotFoundException")
        void login_emailNotFound() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Email introuvable");

            verifyNoInteractions(passwordEncoder, jwtUtil);
        }

        @Test
        @DisplayName(" Compte désactivé – lève UnauthorizedException")
        void login_inactiveAccount() {
            activeUser.setStatus(UserStatus.BLOCKED);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("désactivé");

            verifyNoInteractions(passwordEncoder, jwtUtil);
        }

        @Test
        @DisplayName(" Mot de passe incorrect – lève UnauthorizedException")
        void login_wrongPassword() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Mot de passe incorrect");

            verify(passwordEncoder).matches(RAW_PASSWORD, HASHED_PASSWORD);
            verifyNoInteractions(jwtUtil);
        }
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        private RegisterRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new RegisterRequest(
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
        }

        @Test
        @DisplayName("✅ Succès – sauvegarde user + compte + profil, retourne UserResponse")
        void register_success() {
            User mappedUser = new User();
            mappedUser.setEmail("jean@example.com");
            mappedUser.setPassword(RAW_PASSWORD);           // avant hashage

            User savedUser = new User();
            savedUser.setId(USER_ID);
            savedUser.setEmail("jean@example.com");

            when(userMapper.toEntity(validRequest)).thenReturn(mappedUser);
            when(userRepository.existsByEmail("jean@example.com")).thenReturn(false);
            when(userRepository.save(mappedUser)).thenReturn(savedUser);
            when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

            UserResponse result = authService.register(validRequest);

            assertThat(result).isEqualTo(userResponse);

            assertThat(mappedUser.getPassword()).isNotEqualTo(RAW_PASSWORD);
            assertThat(mappedUser.getPassword()).startsWith("$2a$");

            verify(userRepository).save(mappedUser);
            verify(accountService).save(USER_ID);
            verify(profileService).saveProfileForNewUser(USER_ID, validRequest);
        }

        @Test
        @DisplayName(" Email déjà existant – lève ConflictException")
        void register_emailAlreadyExists() {
            User mappedUser = new User();
            mappedUser.setEmail("jean@example.com");
            mappedUser.setPassword(RAW_PASSWORD);

            when(userMapper.toEntity(validRequest)).thenReturn(mappedUser);
            when(userRepository.existsByEmail("jean@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("adresse mail existe déjà");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(accountService);
            verifyNoInteractions(profileService);
        }

        @Test
        @DisplayName(" Le mot de passe est bien hashé avec BCrypt avant sauvegarde")
        void register_passwordIsHashed() {
            User mappedUser = new User();
            mappedUser.setEmail("jean@example.com");
            mappedUser.setPassword(RAW_PASSWORD);

            User savedUser = new User();
            savedUser.setId(USER_ID);

            when(userMapper.toEntity(validRequest)).thenReturn(mappedUser);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

            authService.register(validRequest);

            verify(userRepository).save(argThat(u ->
                    u.getPassword() != null
                    && u.getPassword().startsWith("$2a$")
                    && !u.getPassword().equals(RAW_PASSWORD)
            ));
        }

        @Test
        @DisplayName(" Le profil utilisateur est bien sauvegardé avec les infos CIN, etc")
        void register_profileIsSaved() {
            User mappedUser = new User();
            mappedUser.setEmail("jean@example.com");
            mappedUser.setPassword(RAW_PASSWORD);

            User savedUser = new User();
            savedUser.setId(USER_ID);

            when(userMapper.toEntity(validRequest)).thenReturn(mappedUser);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

            authService.register(validRequest);

            verify(profileService).saveProfileForNewUser(USER_ID, validRequest);
        }
    }

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName(" Succès – blackliste le token et vide le contexte")
        void logout_success() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);

            Map<String, String> result = authService.logout(request);

            assertThat(result).containsEntry("message", "Déconnexion réussie.");
            verify(tokenBlacklistService).blacklistToken(JWT_TOKEN);
        }

        @Test
        @DisplayName(" Header Authorization absent – lève UnauthorizedException")
        void logout_missingHeader() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(null);

            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Token manquant ou invalide");

            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        @DisplayName(" Header sans préfixe 'Bearer ' – lève UnauthorizedException")
        void logout_invalidHeaderPrefix() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Token manquant ou invalide");

            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        @DisplayName(" Le token extrait (sans 'Bearer ') est bien blacklisté")
        void logout_correctTokenExtracted() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Bearer mySpecificToken");

            authService.logout(request);

            verify(tokenBlacklistService).blacklistToken("mySpecificToken");
        }
    }
}