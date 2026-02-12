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
            @Param("merchantId") String merchantId,
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

    // Count all payments for a merchant since a date
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.createdAt >= :since")
    long countByMerchantIdSince(@Param("merchantId") UUID merchantId,
                                @Param("since") LocalDateTime since);

    // Count payments for a merchant with a specific status since a date
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.status = :status AND p.createdAt >= :since")
    long countByMerchantIdAndStatusSince(@Param("merchantId") UUID merchantId,
                                         @Param("status") String status,
                                         @Param("since") LocalDateTime since);

//    // Sum total revenue for a merchant since a date
//    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.merchantId = :merchantId AND p.createdAt >= :since")
//    BigDecimal getTotalRevenueForMerchantSince(@Param("merchantId") UUID merchantId,
//                                               @Param("since") LocalDateTime since);
}