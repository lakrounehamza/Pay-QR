package com.lakroune.backend.service.impl;

import com.lakroune.backend.dto.request.UpdateAccountRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.entity.Account;
import com.lakroune.backend.entity.BlacklistedRefAccount;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.OwnerType;
import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.mapper.AccountMapper;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.service.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class AccountServiceImpl implements IAccountService {

    private AccountRepository accountRepository;
    private BlacklistedRefAccount blacklistedRefAccount;
    private AccountMapper accountMapper;

    @Override
    public AccountResponse save(User user) {
        Account account = Account.builder()
                .ref(blacklistedRefAccount.getRefAccount())
                .solde(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .status(CompteStatus.ACTIVE)
                .user(user)
                .ownerType(user.getRole() == UserRole.USER ? OwnerType.USER : OwnerType.ENTERPRISE)
                .build();
        Account accountSaved = accountRepository.save(account);
        return accountMapper.toResponse(accountSaved);
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
