package com.paymentsolutions.repository;


import com.paymentsolutions.model.WebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {

    // Find all webhook events for a merchant
    Page<WebhookEvent> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    // Find by status (PENDING, DELIVERED, FAILED)
    Page<WebhookEvent> findByMerchantIdAndStatus(
            UUID merchantId, String status, Pageable pageable);

    // Find events tied to a specific payment
    List<WebhookEvent> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

    // Find by event type (PAYMENT_COMPLETED, PAYMENT_FAILED, REFUND_PROCESSED)
    Page<WebhookEvent> findByMerchantIdAndEventType(
            UUID merchantId, String eventType, Pageable pageable);

    // Find events that need to be retried (FAILED with retryCount < maxRetries)
    @Query("""
            SELECT w FROM WebhookEvent w
            WHERE w.status = 'FAILED'
            AND w.retryCount < w.maxRetries
            AND w.nextRetryAt <= :now
            """)
    List<WebhookEvent> findPendingRetries(@Param("now") LocalDateTime now);

    // Find duplicate events by idempotency key (prevents processing same event twice)
    Optional<WebhookEvent> findByIdempotencyKey(String idempotencyKey);

    // Count failed events for a merchant in a time range
    @Query("""
            SELECT COUNT(w) FROM WebhookEvent w
            WHERE w.merchantId = :merchantId
            AND w.status = 'FAILED'
            AND w.createdAt >= :since
            """)
    long countFailedByMerchantIdSince(
            @Param("merchantId") UUID merchantId,
            @Param("since") LocalDateTime since
    );

    // Delivery success rate
    @Query("""
            SELECT COUNT(w) FROM WebhookEvent w
            WHERE w.merchantId = :merchantId
            AND w.status = :status
            AND w.createdAt BETWEEN :start AND :end
            """)
    long countByMerchantIdAndStatusBetween(
            @Param("merchantId") UUID merchantId,
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}