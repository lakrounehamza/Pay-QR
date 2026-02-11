package com.lakroune.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lakroune.backend.entity.Operation;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    @Query("""
            SELECT o FROM Operation o
            WHERE (
                o.type = com.lakroune.backend.enums.OperationType.CHARGE
                AND o.accountDestination.user.enterprise.id = :enterpriseId
                AND o.accountDestination.user.role <> com.lakroune.backend.enums.UserRole.ENTERPRISE_ADMIN
            ) OR (
                o.type = com.lakroune.backend.enums.OperationType.WITHDRAWAL
                AND o.accountSource.user.enterprise.id = :enterpriseId
                AND o.accountSource.user.role <> com.lakroune.backend.enums.UserRole.ENTERPRISE_ADMIN
            )
            ORDER BY o.createdAt DESC
            """)
    List<Operation> findFundingOperationsByEnterprise(@Param("enterpriseId") UUID enterpriseId);
}
