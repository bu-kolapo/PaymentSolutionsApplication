package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_reference", unique = true, length = 100)
    private String transactionReference;

    @Column(name = "gateway_reference", length = 255)
    private String gatewayReference;

    @Column(length = 500)
    private String description;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
    @Column(name = "idempotency_key", unique = true, length = 200)
    private String idempotencyKey;

    // Business methods
    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(status);
    }

    public boolean canBeRefunded() {
        return isCompleted() && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public void markAsCompleted(String gatewayRef) {
        this.status = PaymentStatus.COMPLETED;
        this.gatewayReference = gatewayRef;
        this.transactionDate = LocalDateTime.now();
    }

    public void markAsFailed(String code, String message) {
        this.status = PaymentStatus.FAILED;
        this.errorCode = code;
        this.errorMessage = message;
    }
}






