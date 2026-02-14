package com.lakroune.backend.service;

import java.util.List;
import java.util.UUID;

import com.lakroune.backend.dto.request.ChargeAccountRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.dto.response.EnterpriseStatisticsResponse;
import com.lakroune.backend.dto.response.FundingHistoryResponse;
import com.lakroune.backend.dto.response.UserResponse;

public interface IEntropriseService {

    
    UserResponse createUser(UUID enterpriseId, RegisterRequest request);
    List<UserResponse> getAllUsers(UUID enterpriseId);
    UserResponse getUser(UUID enterpriseId, UUID userId);
    UserResponse activateUser(UUID enterpriseId, UUID userId);
    UserResponse deactivateUser(UUID enterpriseId, UUID userId);

    
    EnterpriseResponse deactivateEnterprise(UUID enterpriseId);

    
    EnterpriseStatisticsResponse getStatistics(UUID enterpriseId);
    
    AccountResponse getEnterpriseAccount(UUID enterpriseId);
    
    AccountResponse chargeAccount(UUID enterpriseId, UUID userId, ChargeAccountRequest request);
    AccountResponse dechargeAccount(UUID enterpriseId, UUID userId, ChargeAccountRequest request);
    List<FundingHistoryResponse> getFundingHistory(UUID enterpriseId);
}
