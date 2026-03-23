package com.paymentsolutions.repository;

import com.paymentsolutions.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface WebhookRepository extends JpaRepository<Webhook, UUID> {

    /**
     * Find failed webhooks that can be retried
     */
    List<Webhook> findByStatusAndRetryCountLessThan(String status, int maxRetries);

    /**
     * Find all webhooks for a payment
     */
    List<Webhook> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

    /**
     * Find webhooks by merchant
     */
    List<Webhook> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    /**
     * Find pending webhooks older than specified time
     */
    List<Webhook> findByStatusAndLastAttemptAtBefore(String status, LocalDateTime before);

    /**
     * Count failed webhooks for a merchant
     */
    long countByMerchantIdAndStatus(UUID merchantId, String status);
}