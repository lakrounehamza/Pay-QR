package com.lakroune.backend.mapper;

import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AccountMapper {
    AccountResponse toResponse(Account entity);
}
