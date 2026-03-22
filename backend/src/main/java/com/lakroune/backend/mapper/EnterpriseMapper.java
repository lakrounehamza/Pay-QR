package com.lakroune.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.entity.Enterprise;

@Mapper(componentModel = "spring")
public interface EnterpriseMapper {

    @Mapping(target = "totalEmployees", expression = "java(entity != null && entity.getUsers() != null ? entity.getUsers().size() : 0)")
    EnterpriseResponse toResponse(Enterprise entity);
}
