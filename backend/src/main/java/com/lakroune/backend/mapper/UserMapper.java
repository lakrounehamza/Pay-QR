package com.lakroune.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.ProfileResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.Profile;
import com.lakroune.backend.entity.User;

@Mapper(componentModel="spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);

    @Mapping(source = "enterprise.id",  target = "enterpriseId")
    @Mapping(source = "enterprise.nom", target = "enterpriseName")
    UserResponse toResponse(User user);

    ProfileResponse toResponse(Profile profile);
}
