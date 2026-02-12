package com.paymentsolutions.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an email or SMS notification sent to a customer or merchant.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_notification_payment_id",  columnList = "payment_id"),
        @Index(name = "idx_notification_status",       columnList = "status"),
        @Index(name = "idx_notification_created_at",   columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Merchant who owns this notification */
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /** Optional: link to the payment that triggered this notification */
    @Column(name = "payment_id")
    private UUID paymentId;

    /**
     * Notification channel: EMAIL, SMS
     */
    @Column(nullable = false, length = 10)
    private String type;

    /**
     * Notification status: PENDING, SENT, FAILED
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** Recipient email address or phone number */
    @Column(nullable = false)
    private String recipient;

    /** Email subject or SMS header */
    @Column(length = 255)
    private String subject;

    /** Full notification body */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    /** Whether the notification has been read/acknowledged by the merchant */
    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    /** Error message if delivery failed */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** External provider message ID (e.g. SendGrid ID, Twilio SID) */
    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    /** Number of delivery attempts */
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Timestamp when notification was successfully delivered */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}