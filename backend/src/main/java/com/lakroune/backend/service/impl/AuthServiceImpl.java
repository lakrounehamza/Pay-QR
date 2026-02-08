package com.lakroune.backend.service.impl;

import com.lakroune.backend.dto.request.LoginRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.LoginResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.exception.DuplicateException;
import com.lakroune.backend.mapper.UserMapper;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.IAuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private UserRepository userRepository;
    private UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        User user = userMapper.toEntity(request);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateException("L'adresse mail existe déjà.");
        }
        User userSaved = userRepository.save(user);
        return userMapper.toResponse(userSaved);
    }
}
