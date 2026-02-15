package com.lakroune.backend.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakroune.backend.dto.request.CreateEnterpriseRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.dto.response.PageResponse;
import com.lakroune.backend.dto.response.StatisticsResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.service.IAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;

    

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.activateUser(id));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deactivateUser(id));
    }

    

    @GetMapping("/accounts")
    public ResponseEntity<PageResponse<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllAccounts(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PatchMapping("/accounts/{id}/activate")
    public ResponseEntity<AccountResponse> activateAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.activateAccount(id));
    }

    @PatchMapping("/accounts/{id}/deactivate")
    public ResponseEntity<AccountResponse> deactivateAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deactivateAccount(id));
    }

    

    @GetMapping("/enterprises")
    public ResponseEntity<PageResponse<EnterpriseResponse>> getAllEnterprises(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllEnterprises(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/enterprises")
    public ResponseEntity<EnterpriseResponse> createEnterprise(@Valid @RequestBody CreateEnterpriseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createEnterprise(request));
    }

    @PatchMapping("/enterprises/{id}/activate")
    public ResponseEntity<EnterpriseResponse> activateEnterprise(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.activateEnterprise(id));
    }

    @PatchMapping("/enterprises/{id}/deactivate")
    public ResponseEntity<EnterpriseResponse> deactivateEnterprise(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deactivateEnterprise(id));
    }

    

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(adminService.getStatistics());
    }
}
