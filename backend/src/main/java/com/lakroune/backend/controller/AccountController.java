package com.lakroune.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakroune.backend.dto.request.CreateAccountRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.IAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/account")
public class AccountController {

    private final IAccountService accountService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse accountResponse = accountService.save(request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccount() {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAllAccount());
    }

    @GetMapping("/my")
    public ResponseEntity<AccountResponse> getMyAccount(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        return ResponseEntity.ok(accountService.getAccountByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccountById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AccountResponse> deleteAccountById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.deleteAccount(id));
    }
}
