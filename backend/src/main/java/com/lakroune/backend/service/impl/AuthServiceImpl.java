package com.lakroune.backend.service.impl;

import com.lakroune.backend.dto.request.LoginRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.LoginResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.exception.DuplicateException;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.mapper.UserMapper;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.security.JwtUtil;
import com.lakroune.backend.security.TokenBlacklistService;
import com.lakroune.backend.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private UserMapper userMapper;
    private JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());
        if (!user.isPresent())
            throw new NotFoundException(" email  incoricte");
        User user1 = user.get();
        if (!passwordEncoder.matches(request.password(), user.get().getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        UserResponse userResponse = userMapper.toResponse(user1);
        String token = jwtUtil.generateToken(user1);
        return new LoginResponse(userResponse, token);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        User user = userMapper.toEntity(request);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateException("L'adresse mail existe déjà.");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        User userSaved = userRepository.save(user);
        return userMapper.toResponse(userSaved);
    }
    @Override
    public Map<String, String> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
        return Map.of("message", "ok");
    }
}
