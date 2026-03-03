package com.lakroune.backend.controller;

import com.lakroune.backend.dto.request.CreateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.service.IAccountService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/account")
public class AccountController {

    private final IAccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody CreateAccountRequest request) {
        AccountResponse accountResponse = accountService.save(request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccount() {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAllAccount());
    }
    @GetMapping({"/id"})
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccountById(id));
    }
    @DeleteMapping({"/id"})
    public  ResponseEntity<AccountResponse> deleteAccountById(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(accountService.deleteAccount(id));
    }
}
