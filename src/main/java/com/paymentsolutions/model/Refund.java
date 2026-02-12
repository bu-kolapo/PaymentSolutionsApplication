package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a refund transaction linked to an original payment.
 */
@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_merchant_id",         columnList = "merchant_id"),
        @Index(name = "idx_refund_original_payment_id", columnList = "original_payment_id"),
        @Index(name = "idx_refund_customer_id",         columnList = "customer_id"),
        @Index(name = "idx_refund_status",              columnList = "status"),
        @Index(name = "idx_refund_created_at",          columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /** The original payment being refunded */
    @Column(name = "original_payment_id", nullable = false)
    private UUID originalPaymentId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /** Refund amount — must not exceed original payment amount */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Status: PENDING, PROCESSING, COMPLETED, FAILED
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * Reason for refund: CUSTOMER_REQUEST, DUPLICATE, FRAUDULENT,
     * PRODUCT_NOT_RECEIVED, PRODUCT_UNACCEPTABLE, OTHER
     */
    @Column(length = 50)
    private String reason;

    /** Optional note from the merchant */
    @Column(length = 500)
    private String notes;

    /** Reference ID returned by the payment gateway for this refund */
    @Column(name = "gateway_refund_reference", length = 100)
    private String gatewayRefundReference;

    /** Internal refund reference code */
    @Column(name = "refund_reference", nullable = false, unique = true, length = 50)
    private String refundReference;

    /** Error message if refund failed */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Customer email at time of refund */
    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    /** Who initiated the refund */
    @Column(name = "initiated_by")
    private UUID initiatedBy;

    /** Timestamp when refund was completed */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
