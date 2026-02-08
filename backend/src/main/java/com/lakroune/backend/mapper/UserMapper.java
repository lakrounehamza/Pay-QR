package com.lakroune.backend.mapper;

import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface UserMapper {
    User  toEntity(RegisterRequest request);
    UserResponse toResponse(User user);
}
