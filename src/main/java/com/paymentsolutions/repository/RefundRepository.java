package com.paymentsolutions.repository;

import com.paymentsolutions.model.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    // Find all refunds for a merchant
    Page<Refund> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    // Find refunds for a specific original payment
    List<Refund> findByOriginalPaymentId(UUID originalPaymentId);

    // Find refunds by status (PENDING, COMPLETED, FAILED)
    Page<Refund> findByMerchantIdAndStatus(
            UUID merchantId, String status, Pageable pageable);

    // Find refunds for a specific customer
    Page<Refund> findByCustomerId(UUID customerId, Pageable pageable);

    // Total refunded amount for a payment (for partial refund checks)
    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM Refund r
            WHERE r.originalPaymentId = :paymentId
            AND r.status = 'COMPLETED'
            """)
    BigDecimal getTotalRefundedAmountForPayment(@Param("paymentId") UUID paymentId);

    // Total refunded amount for a merchant in a period
    @Query("""
            SELECT COALESCE(SUM(r.amount), 0) FROM Refund r
            WHERE r.merchantId = :merchantId
            AND r.status = 'COMPLETED'
            AND r.createdAt >= :since
            """)
    Optional<BigDecimal> getTotalRefundedByMerchantSince(
            @Param("merchantId") UUID merchantId,
            @Param("since") LocalDateTime since
    );

    // Count refunds for a merchant within a period
    @Query("""
            SELECT COUNT(r) FROM Refund r
            WHERE r.merchantId = :merchantId
            AND r.createdAt >= :since
            """)
    long countByMerchantIdSince(
            @Param("merchantId") UUID merchantId,
            @Param("since") LocalDateTime since
    );

    // Refund rate: count refunds vs payments for a period
    @Query("""
            SELECT COUNT(r) FROM Refund r
            WHERE r.merchantId = :merchantId
            AND r.status = 'COMPLETED'
            AND r.createdAt BETWEEN :start AND :end
            """)
    long countCompletedByMerchantIdBetween(
            @Param("merchantId") UUID merchantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
