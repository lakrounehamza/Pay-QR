package com.lakroune.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.lakroune.backend.dto.request.CreateEnterpriseRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.dto.response.PageResponse;
import com.lakroune.backend.dto.response.StatisticsResponse;
import com.lakroune.backend.dto.response.UserResponse;

public interface IAdminService {

    
    PageResponse<UserResponse> getAllUsers(Pageable pageable);
    UserResponse activateUser(UUID userId);
    UserResponse deactivateUser(UUID userId);

    
    PageResponse<AccountResponse> getAllAccounts(Pageable pageable);
    AccountResponse activateAccount(UUID accountId);
    AccountResponse deactivateAccount(UUID accountId);

    
    PageResponse<EnterpriseResponse> getAllEnterprises(Pageable pageable);
    EnterpriseResponse createEnterprise(CreateEnterpriseRequest request);
    EnterpriseResponse activateEnterprise(UUID enterpriseId);
    EnterpriseResponse deactivateEnterprise(UUID enterpriseId);

    
    StatisticsResponse getStatistics();

    Object userStatisticss();
}
