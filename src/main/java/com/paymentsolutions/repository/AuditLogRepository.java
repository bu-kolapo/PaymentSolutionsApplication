package com.paymentsolutions.repository;


import com.paymentsolutions.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // Find audit logs for a specific merchant
    Page<AuditLog> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    // Find audit logs by entity type (e.g. "PAYMENT", "CUSTOMER")
    Page<AuditLog> findByMerchantIdAndEntityType(
            UUID merchantId, String entityType, Pageable pageable);

    // Find audit logs for a specific entity (e.g. a specific payment)
    List<AuditLog> findByEntityIdOrderByCreatedAtDesc(UUID entityId);

    // Find audit logs by action (CREATE, UPDATE, DELETE)
    Page<AuditLog> findByMerchantIdAndAction(
            UUID merchantId, String action, Pageable pageable);

    // Find audit logs by user
    Page<AuditLog> findByPerformedByOrderByCreatedAtDesc(UUID performedBy, Pageable pageable);

    // Find audit logs within a date range
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.merchantId = :merchantId
            AND a.createdAt BETWEEN :start AND :end
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByMerchantIdAndDateRange(
            @Param("merchantId") UUID merchantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // Count actions within a time window (for rate limiting / anomaly detection)
    @Query("""
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.performedBy = :userId
            AND a.action = :action
            AND a.createdAt >= :since
            """)
    long countByUserAndActionSince(
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("since") LocalDateTime since
    );
}