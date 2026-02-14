package com.lakroune.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakroune.backend.dto.request.ChargeAccountRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.dto.response.EnterpriseStatisticsResponse;
import com.lakroune.backend.dto.response.FundingHistoryResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.service.IEntropriseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/enterprise/{enterpriseId}")
@RequiredArgsConstructor
public class EnterpriseController {

    private final IEntropriseService entropriseService;

    

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable UUID enterpriseId,
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entropriseService.createUser(enterpriseId, request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(entropriseService.getAllUsers(enterpriseId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(entropriseService.getUser(enterpriseId, userId));
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(entropriseService.activateUser(enterpriseId, userId));
    }

    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(entropriseService.deactivateUser(enterpriseId, userId));
    }

    

    @PatchMapping("/deactivate")
    public ResponseEntity<EnterpriseResponse> deactivateEnterprise(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(entropriseService.deactivateEnterprise(enterpriseId));
    }

    

    @GetMapping("/statistics")
    public ResponseEntity<EnterpriseStatisticsResponse> getStatistics(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(entropriseService.getStatistics(enterpriseId));
    }

    

    @GetMapping("/account")
    public ResponseEntity<AccountResponse> getEnterpriseAccount(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(entropriseService.getEnterpriseAccount(enterpriseId));
    }

    

    @PostMapping("/users/{userId}/account/charge")
    public ResponseEntity<AccountResponse> chargeAccount(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChargeAccountRequest request) {
        return ResponseEntity.ok(entropriseService.chargeAccount(enterpriseId, userId, request));
    }

    @PostMapping("/users/{userId}/account/decharge")
    public ResponseEntity<AccountResponse> dechargeAccount(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChargeAccountRequest request) {
        return ResponseEntity.ok(entropriseService.dechargeAccount(enterpriseId, userId, request));
    }

    @GetMapping("/funding-history")
    public ResponseEntity<List<FundingHistoryResponse>> getFundingHistory(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(entropriseService.getFundingHistory(enterpriseId));
    }
}
