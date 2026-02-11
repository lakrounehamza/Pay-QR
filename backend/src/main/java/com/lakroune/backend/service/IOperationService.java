package com.lakroune.backend.service;

import com.lakroune.backend.dto.request.CreateOperationRequest;
import com.lakroune.backend.dto.response.OperationResponse;

import java.util.List;
import java.util.UUID;

public interface IOperationService {
    OperationResponse save(CreateOperationRequest request);
    List<OperationResponse> getAllOperation();
    List<OperationResponse>  getByAccountId(UUID accountId);
}
