package com.lakroune.backend.service;

import com.lakroune.backend.dto.request.UpdateAccountRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface IAccountService {
    AccountResponse save(User user);
    List<AccountResponse>  getAllAccount();
    AccountResponse getAccountById(UUID id);
    AccountResponse updateAccount(UUID accountId , UpdateAccountRequest request);
    AccountResponse deleteAccount(UUID accountId);
}
