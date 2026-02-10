package com.paymentsolutions.repository;


import com.paymentsolutions.model.Payment;
import com.paymentsolutions.model.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<Payment> findByMerchantIdAndStatus(UUID merchantId, PaymentStatus status, Pageable pageable);

    Page<Payment> findByCustomerId(UUID customerId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId " +
            "AND p.transactionDate BETWEEN :startDate AND :endDate")
    List<Payment> findByMerchantIdAndDateRange(
            @Param("merchantId") UUID merchantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.merchantId = :merchantId " +
            "AND p.status = 'COMPLETED' AND p.transactionDate >= :date")
    Optional<BigDecimal> getTotalRevenueForMerchantSince(
            @Param("merchantId") UUID merchantId,
            @Param("date") LocalDateTime date
    );

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId " +
            "AND p.status = :status AND p.transactionDate >= :date")
    Long countByMerchantIdAndStatusSince(
            @Param("merchantId") UUID merchantId,
            @Param("status") PaymentStatus status,
            @Param("date") LocalDateTime date
    );

    Optional<Payment> findByTransactionReference(String transactionReference);

    boolean existsByGatewayReference(String gatewayReference);
}