package com.lakroune.backend.service;

import java.util.List;
import java.util.UUID;

import com.lakroune.backend.dto.request.UpdateAccountRequest;
import com.lakroune.backend.dto.response.AccountResponse;

public interface IAccountService {
    AccountResponse save(UUID userId);
    List<AccountResponse>  getAllAccount();
    AccountResponse getAccountById(UUID id);
    AccountResponse getAccountByUserId(UUID userId);
    AccountResponse updateAccount(UUID accountId , UpdateAccountRequest request);
    AccountResponse deleteAccount(UUID accountId);
}
