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

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByPaymentReference(String paymentReference);

    Page<Payment> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    List<Payment> findByStatusOrderByCreatedAtDesc(String status);

    List<Payment> findByStatusAndRetryCountLessThan(String status, int maxRetries);

    long countBySourceAccountAndCreatedAtAfter(String sourceAccount, LocalDateTime after);

    Page<Payment> findByMerchantIdAndStatus(UUID merchantId, PaymentStatus status, Pageable pageable);
    Optional<Payment> findByIdAndMerchantId(UUID id, UUID merchantId);


    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCESS' AND p.webhookSent = false")
    List<Payment> findUnsentWebhooks();

    Optional<Payment> findByTransactionReference(String transactionReference);

    boolean existsByGatewayReference(String gatewayReference);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.createdAt >= :since")
    long countByMerchantIdSince(@Param("merchantId") UUID merchantId,
                                @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.status = :status AND p.createdAt >= :since")
    long countByMerchantIdAndStatusSince(@Param("merchantId") UUID merchantId,
                                         @Param("status") String status,
                                         @Param("since") LocalDateTime since);

    Page<Payment> findByMerchantIdAndStatus(UUID merchantId, String status, Pageable pageable);

    Page<Payment> findByMerchantId(UUID merchantId, Pageable pageable);




    /**
     * Get average transaction amount for a merchant in a period
     * Useful for pattern analysis
     */
    @Query("""
        SELECT COALESCE(AVG(p.amount), 0) FROM Payment p 
        WHERE p.merchantId = :merchantId 
        AND p.createdAt >= :since
        AND p.status = 'COMPLETED'
    """)
    Optional<BigDecimal> getAverageTransactionAmountSince(
            @Param("merchantId") UUID merchantId,
            @Param("since") LocalDateTime since
    );

    /**
     * Get transaction count by status for a period
     * Useful for insights generation
     */
    @Query("""
        SELECT COUNT(p) FROM Payment p 
        WHERE p.merchantId = :merchantId 
        AND p.createdAt >= :since
        AND p.status IN ('COMPLETED', 'PENDING', 'FAILED')
    """)
    long countPaymentsByMerchantInPeriod(
            @Param("merchantId") UUID merchantId,
            @Param("since") LocalDateTime since
    );
}