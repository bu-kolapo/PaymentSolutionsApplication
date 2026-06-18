package com.paymentsolutions.model;
// =====================================================
// 1. src/main/java/com/paymentsolutions/domain/entity/Transaction.java
// =====================================================


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable record of every payment state change
 * Provides complete audit trail for payment lifecycle
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_payment_id", columnList = "payment_id"),
        @Index(name = "idx_transaction_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_transaction_type", columnList = "type"),
        @Index(name = "idx_transaction_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /**
     * Transaction type: PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED, REFUND_ISSUED
     */
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /**
     * Transaction status: SUCCESS, FAILED, PENDING
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
     private  String  referenceId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Previous payment status before this transaction
     */
    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    /**
     * New payment status after this transaction
     */
    @Column(name = "new_status", length = 20)
    private String newStatus;

    /**
     * Stripe PaymentIntent ID or other gateway reference
     */
    @Column(name = "gateway_reference", length = 255)
    private String gatewayReference;

    /**
     * Transaction reference (e.g., TXN-ABCD1234)
     */
    @Column(name = "transaction_reference", unique = true, nullable = false, length = 100)
    private String transactionReference;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * User or system that initiated this transaction
     */
    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    /**
     * IP address of the requester
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Additional metadata (JSON format)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}


