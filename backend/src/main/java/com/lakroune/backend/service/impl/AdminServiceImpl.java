package com.lakroune.backend.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakroune.backend.dto.request.CreateEnterpriseRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.dto.response.PageResponse;
import com.lakroune.backend.dto.response.StatisticsResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.Enterprise;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.EnterpriseStatus;
import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.enums.UserStatus;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.mapper.AccountMapper;
import com.lakroune.backend.mapper.EnterpriseMapper;
import com.lakroune.backend.mapper.UserMapper;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.EnterpriseRepository;
import com.lakroune.backend.repository.OperationRepository;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.service.IAccountService;
import com.lakroune.backend.service.IAdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final OperationRepository operationRepository;

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final PasswordEncoder passwordEncoder;
    private final IAccountService accountService;

    

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        return toPageResponse(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse activateUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse deactivateUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        user.setStatus(UserStatus.BLOCKED);
        return userMapper.toResponse(userRepository.save(user));
    }

    

    @Override
    public PageResponse<AccountResponse> getAllAccounts(Pageable pageable) {
        return toPageResponse(accountRepository.findAll(pageable).map(accountMapper::toResponse));
    }

    @Override
    public AccountResponse activateAccount(UUID accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
        account.setStatus(CompteStatus.ACTIVE);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse deactivateAccount(UUID accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
        account.setStatus(CompteStatus.CLOSED);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    

    @Override
    public PageResponse<EnterpriseResponse> getAllEnterprises(Pageable pageable) {
        return toPageResponse(enterpriseRepository.findAll(pageable).map(enterpriseMapper::toResponse));
    }

    @Override
    @Transactional
    public EnterpriseResponse createEnterprise(CreateEnterpriseRequest request) {
        
        Enterprise enterprise = Enterprise.builder()
                .nom(request.nom())
                .email(request.email())
                .telephone(request.telephone())
                .statut(EnterpriseStatus.ACTIVE)
                .build();
        enterprise = enterpriseRepository.save(enterprise);

        
        User adminUser = new User();
        adminUser.setNom(request.adminNom());
        adminUser.setPrenom(request.adminPrenom());
        adminUser.setEmail(request.adminEmail());
        adminUser.setTelephone(request.adminTelephone());
        adminUser.setPassword(passwordEncoder.encode(request.adminPassword()));
        adminUser.setRole(UserRole.ENTERPRISE_ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setEnterprise(enterprise);
        User savedAdmin = userRepository.save(adminUser);

        
        accountService.save(savedAdmin.getId());

        return enterpriseMapper.toResponse(enterprise);
    }

    @Override
    public EnterpriseResponse activateEnterprise(UUID enterpriseId) {
        var enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + enterpriseId));
        enterprise.setStatut(EnterpriseStatus.ACTIVE);
        return enterpriseMapper.toResponse(enterpriseRepository.save(enterprise));
    }

    @Override
    public EnterpriseResponse deactivateEnterprise(UUID enterpriseId) {
        var enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + enterpriseId));
        enterprise.setStatut(EnterpriseStatus.SUSPENDED);
        return enterpriseMapper.toResponse(enterpriseRepository.save(enterprise));
    }

    

    @Override
    public StatisticsResponse getStatistics() {
        var allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long blockedUsers = allUsers.stream().filter(u -> u.getStatus() == UserStatus.BLOCKED).count();

        var allAccounts = accountRepository.findAll();
        long totalAccounts = allAccounts.size();
        long activeAccounts = allAccounts.stream().filter(a -> a.getStatus() == CompteStatus.ACTIVE).count();
        long closedAccounts = allAccounts.stream().filter(a -> a.getStatus() == CompteStatus.CLOSED).count();

        var allEnterprises = enterpriseRepository.findAll();
        long totalEnterprises = allEnterprises.size();
        long activeEnterprises = allEnterprises.stream().filter(e -> e.getStatut() == EnterpriseStatus.ACTIVE).count();
        long suspendedEnterprises = allEnterprises.stream().filter(e -> e.getStatut() == EnterpriseStatus.SUSPENDED).count();

        var allOperations = operationRepository.findAll();
        long totalOperations = allOperations.size();
        long successOperations = allOperations.stream().filter(o -> o.getStatus() == OperationStatus.SUCCESS).count();
        long failedOperations = allOperations.stream().filter(o -> o.getStatus() == OperationStatus.FAILED).count();
        long pendingOperations = allOperations.stream().filter(o -> o.getStatus() == OperationStatus.PENDING).count();

        java.math.BigDecimal totalVolume = allAccounts.stream()
                .map(a -> a.getSolde() != null ? a.getSolde() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new StatisticsResponse(
                totalUsers, activeUsers, blockedUsers,
                totalAccounts, activeAccounts, closedAccounts,
                totalEnterprises, activeEnterprises, suspendedEnterprises,
                totalOperations, successOperations, failedOperations, pendingOperations,
                totalVolume
        );
    }
}
