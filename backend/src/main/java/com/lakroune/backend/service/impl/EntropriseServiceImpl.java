package com.lakroune.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakroune.backend.dto.request.ChargeAccountRequest;
import com.lakroune.backend.dto.request.RegisterRequest;
import com.lakroune.backend.dto.response.AccountResponse;
import com.lakroune.backend.dto.response.EnterpriseResponse;
import com.lakroune.backend.dto.response.EnterpriseStatisticsResponse;
import com.lakroune.backend.dto.response.FundingHistoryResponse;
import com.lakroune.backend.dto.response.UserResponse;
import com.lakroune.backend.entity.Enterprise;
import com.lakroune.backend.entity.Operation;
import com.lakroune.backend.entity.User;
import com.lakroune.backend.enums.CompteStatus;
import com.lakroune.backend.enums.EnterpriseStatus;
import com.lakroune.backend.enums.OperationStatus;
import com.lakroune.backend.enums.OperationType;
import com.lakroune.backend.enums.UserStatus;
import com.lakroune.backend.exception.ConflictException;
import com.lakroune.backend.exception.InsufficientBalanceException;
import com.lakroune.backend.exception.NotFoundException;
import com.lakroune.backend.exception.UnauthorizedException;
import com.lakroune.backend.mapper.AccountMapper;
import com.lakroune.backend.mapper.EnterpriseMapper;
import com.lakroune.backend.mapper.UserMapper;
import com.lakroune.backend.repository.AccountRepository;
import com.lakroune.backend.repository.EnterpriseRepository;
import com.lakroune.backend.repository.OperationRepository;
import com.lakroune.backend.repository.UserRepository;
import com.lakroune.backend.enums.UserRole;
import com.lakroune.backend.service.IAccountService;
import com.lakroune.backend.service.IEntropriseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EntropriseServiceImpl implements IEntropriseService {

    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final IAccountService accountService;

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final EnterpriseMapper enterpriseMapper;

    

    private Enterprise findEnterprise(UUID enterpriseId) {
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + enterpriseId));
    }

    private User findUserInEnterprise(UUID enterpriseId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (user.getEnterprise() == null || !user.getEnterprise().getId().equals(enterpriseId)) {
            throw new UnauthorizedException("User does not belong to this enterprise");
        }
        return user;
    }

    

    @Override
    public UserResponse createUser(UUID enterpriseId, RegisterRequest request) {
        Enterprise enterprise = findEnterprise(enterpriseId);

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(BCrypt.hashpw(request.password(), BCrypt.gensalt()));
        user.setEnterprise(enterprise);
        User savedUser = userRepository.save(user);
        
        accountService.save(savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers(UUID enterpriseId) {
        findEnterprise(enterpriseId);
        return userRepository.findByEnterprise_Id(enterpriseId).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUser(UUID enterpriseId, UUID userId) {
        return userMapper.toResponse(findUserInEnterprise(enterpriseId, userId));
    }

    @Override
    public UserResponse activateUser(UUID enterpriseId, UUID userId) {
        User user = findUserInEnterprise(enterpriseId, userId);
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse deactivateUser(UUID enterpriseId, UUID userId) {
        User user = findUserInEnterprise(enterpriseId, userId);
        user.setStatus(UserStatus.BLOCKED);
        return userMapper.toResponse(userRepository.save(user));
    }

    

    @Override
    public EnterpriseResponse deactivateEnterprise(UUID enterpriseId) {
        Enterprise enterprise = findEnterprise(enterpriseId);
        enterprise.setStatut(EnterpriseStatus.SUSPENDED);
        return enterpriseMapper.toResponse(enterpriseRepository.save(enterprise));
    }

    

    @Override
    public EnterpriseStatisticsResponse getStatistics(UUID enterpriseId) {
        findEnterprise(enterpriseId);

        List<User> employees = userRepository.findByEnterprise_Id(enterpriseId);
        long totalEmployees = employees.size();
        long activeEmployees = employees.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long blockedEmployees = employees.stream().filter(u -> u.getStatus() == UserStatus.BLOCKED).count();

        var accounts = accountRepository.findByUser_Enterprise_Id(enterpriseId);
        BigDecimal totalBalance = accounts.stream()
                .filter(a -> a.getStatus() == CompteStatus.ACTIVE)
                .map(a -> a.getSolde())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var allOps = operationRepository.findAll();
        var enterpriseAccountIds = accounts.stream().map(a -> a.getId()).toList();

        var relatedOps = allOps.stream()
                .filter(o -> o.getAccountSource() != null && enterpriseAccountIds.contains(o.getAccountSource().getId())
                        || o.getAccountDestination() != null && enterpriseAccountIds.contains(o.getAccountDestination().getId()))
                .toList();

        long totalOperations = relatedOps.size();
        long successOperations = relatedOps.stream().filter(o -> o.getStatus() == OperationStatus.SUCCESS).count();
        long failedOperations = relatedOps.stream().filter(o -> o.getStatus() == OperationStatus.FAILED).count();

        return new EnterpriseStatisticsResponse(
                totalEmployees, activeEmployees, blockedEmployees,
                totalOperations, successOperations, failedOperations,
                totalBalance
        );
    }

    

    @Override
    public AccountResponse getEnterpriseAccount(UUID enterpriseId) {
        findEnterprise(enterpriseId);
        User adminUser = userRepository.findByEnterprise_IdAndRole(enterpriseId, UserRole.ENTERPRISE_ADMIN)
                .orElseThrow(() -> new NotFoundException("Enterprise admin not found for enterprise: " + enterpriseId));
        
        return accountRepository.findByUser_Id(adminUser.getId())
                .map(accountMapper::toResponse)
                .orElseGet(() -> accountService.save(adminUser.getId()));
    }

    @Override
    public AccountResponse chargeAccount(UUID enterpriseId, UUID userId, ChargeAccountRequest request) {
        findUserInEnterprise(enterpriseId, userId);

        
        User adminUser = userRepository.findByEnterprise_IdAndRole(enterpriseId, UserRole.ENTERPRISE_ADMIN)
                .orElseThrow(() -> new NotFoundException("Enterprise admin not found"));
        
        var enterpriseAccount = accountRepository.findByUser_Id(adminUser.getId())
                .orElseGet(() -> {
                    accountService.save(adminUser.getId());
                    return accountRepository.findByUser_Id(adminUser.getId())
                            .orElseThrow(() -> new NotFoundException("Failed to create enterprise account"));
                });
        if (enterpriseAccount.getSolde().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in enterprise account");
        }

        
        var employeeAccount = accountRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    accountService.save(userId);
                    return accountRepository.findByUser_Id(userId)
                            .orElseThrow(() -> new NotFoundException("Failed to create account for user: " + userId));
                });

        
        enterpriseAccount.setSolde(enterpriseAccount.getSolde().subtract(request.amount()));
        accountRepository.save(enterpriseAccount);
        employeeAccount.setSolde(employeeAccount.getSolde().add(request.amount()));
        accountRepository.save(employeeAccount);

        
        operationRepository.save(Operation.builder()
                .type(OperationType.CHARGE)
                .amount(request.amount())
                .status(OperationStatus.SUCCESS)
                .accountSource(enterpriseAccount)
                .accountDestination(employeeAccount)
                .createdAt(java.time.LocalDateTime.now())
                .build());

        return accountMapper.toResponse(accountRepository.findByUser_Id(userId).orElseThrow());
    }

    @Override
    public AccountResponse dechargeAccount(UUID enterpriseId, UUID userId, ChargeAccountRequest request) {
        findUserInEnterprise(enterpriseId, userId);

        
        var employeeAccount = accountRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    accountService.save(userId);
                    return accountRepository.findByUser_Id(userId)
                            .orElseThrow(() -> new NotFoundException("Failed to create account for user: " + userId));
                });

        if (employeeAccount.getSolde().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in employee account");
        }

        
        User adminUser = userRepository.findByEnterprise_IdAndRole(enterpriseId, UserRole.ENTERPRISE_ADMIN)
                .orElseThrow(() -> new NotFoundException("Enterprise admin not found"));
        var enterpriseAccount = accountRepository.findByUser_Id(adminUser.getId())
                .orElseGet(() -> {
                    accountService.save(adminUser.getId());
                    return accountRepository.findByUser_Id(adminUser.getId()).orElseThrow();
                });

        employeeAccount.setSolde(employeeAccount.getSolde().subtract(request.amount()));
        accountRepository.save(employeeAccount);
        enterpriseAccount.setSolde(enterpriseAccount.getSolde().add(request.amount()));
        accountRepository.save(enterpriseAccount);

        
        operationRepository.save(Operation.builder()
                .type(OperationType.WITHDRAWAL)
                .amount(request.amount())
                .status(OperationStatus.SUCCESS)
                .accountSource(employeeAccount)
                .accountDestination(enterpriseAccount)
                .createdAt(java.time.LocalDateTime.now())
                .build());

        return accountMapper.toResponse(accountRepository.findByUser_Id(userId).orElseThrow());
    }

    @Override
    public List<FundingHistoryResponse> getFundingHistory(UUID enterpriseId) {
        findEnterprise(enterpriseId);
        return operationRepository.findFundingOperationsByEnterprise(enterpriseId)
                .stream()
                .map(op -> {
                    User employee = op.getType() == OperationType.CHARGE
                            ? op.getAccountDestination().getUser()
                            : op.getAccountSource().getUser();
                    String opName = op.getType() == OperationType.CHARGE ? "charge" : "decharge";
                    return new FundingHistoryResponse(
                            op.getId(),
                            employee.getId(),
                            employee.getPrenom() + " " + employee.getNom(),
                            employee.getEmail(),
                            opName,
                            op.getAmount(),
                            op.getCreatedAt()
                    );
                })
                .toList();
    }
}
