package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an outbound webhook notification sent to the merchant's webhook URL.
 */
@Entity
@Table(name = "webhook_events", indexes = {
        @Index(name = "idx_webhook_merchant_id",      columnList = "merchant_id"),
        @Index(name = "idx_webhook_payment_id",       columnList = "payment_id"),
        @Index(name = "idx_webhook_status",           columnList = "status"),
        @Index(name = "idx_webhook_idempotency_key",  columnList = "idempotency_key", unique = true),
        @Index(name = "idx_webhook_next_retry_at",    columnList = "next_retry_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /** The payment that triggered this webhook */
    @Column(name = "payment_id")
    private UUID paymentId;

    /**
     * Event type: PAYMENT_COMPLETED, PAYMENT_FAILED,
     * REFUND_PROCESSED, FRAUD_DETECTED
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    /**
     * Delivery status: PENDING, DELIVERED, FAILED
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** The URL this webhook was sent to */
    @Column(name = "webhook_url", nullable = false)
    private String webhookUrl;

    /** JSON payload sent in the request body */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    /** HTTP status code received from the merchant endpoint */
    @Column(name = "response_status_code")
    private Integer responseStatusCode;

    /** Response body received from the merchant endpoint */
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    /** Error message if delivery failed */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Number of delivery attempts made */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    /** Maximum number of retries allowed */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private int maxRetries = 3;

    /** When the next retry should be attempted */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * Unique key to prevent processing the same event twice.
     * Format: paymentId + "_" + eventType
     */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    /** Timestamp when event was successfully delivered */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}