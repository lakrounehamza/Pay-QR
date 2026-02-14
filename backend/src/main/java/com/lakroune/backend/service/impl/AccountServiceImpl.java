package com.lakroune.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lakroune.backend.dto.request.UpdateAccountRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;
import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.mapper.AccountMapper;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.IAccountService;
import com.lakroune.backend.service.IBlacklistedRefAccountService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;
    private final IBlacklistedRefAccountService blacklistedRefAccount;
    private final AccountMapper accountMapper;
    private final UserRepository  userRepository;

    @Override
    public AccountResponse save(UUID userId) {

        User  user = userRepository.findById(userId).orElseThrow(()->new  NotFoundException("Not Fount User"));
        Account account = Account.builder()
                .ref(blacklistedRefAccount.addReferenceAccount())
                .solde(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .status(CompteStatus.ACTIVE)
                .user(user)
                .ownerType(user.getRole() == UserRole.USER || user.getRole() == UserRole.ENTERPRISE_USER
                        ? OwnerType.USER
                        : OwnerType.ENTERPRISE)
                .build();
        Account accountSaved = accountRepository.save(account);
        return accountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAllAccount() {
        return accountRepository.findAll().stream().map(accountMapper::toResponse).toList();
    }

    @Override
    public AccountResponse getAccountById(UUID id) {
        return accountMapper.toResponse(
                accountRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Not found account id: " + id))
        );
    }

    @Override
    public AccountResponse getAccountByUserId(UUID userId) {
        return accountMapper.toResponse(
                accountRepository.findByUser_Id(userId)
                        .orElseThrow(() -> new NotFoundException("No account found for user: " + userId))
        );
    }

    @Override
    public AccountResponse updateAccount(UUID accountId, UpdateAccountRequest request) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Not found account id: " + accountId));
        if (request.ownerType() != null)
            account.setOwnerType(request.ownerType());
        if (request.solde() != null)
            account.setSolde(request.solde());
        if (request.status() != null)
            account.setStatus(request.status());
        Account accountSaved = accountRepository.save(account);
        return accountMapper.toResponse(accountSaved);
    }

    @Override
    public AccountResponse deleteAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Not found account id: " + accountId));
        accountRepository.delete(account);
        return accountMapper.toResponse(account);
    }
}
