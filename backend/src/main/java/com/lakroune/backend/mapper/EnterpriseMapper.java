package com.lakroune.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.entity.Enterprise;

@Mapper(componentModel = "spring")
public interface EnterpriseMapper {

    @Mapping(target = "totalEmployees", expression = "java(entity.getUsers() == null ? 0 : entity.getUsers().size())")
    EnterpriseResponse toResponse(Enterprise entity);
}
