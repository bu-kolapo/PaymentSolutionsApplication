package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "customer_id")
    private UUID customerId;
    private String transactionReference;

    @Column(name = "channel", nullable = false)
    private String channel; // CARD, BANK_TRANSFER, MOBILE_MONEY, WALLET

    @Column(name = "provider")
    private String provider; // STRIPE, PAYSTACK, NIP

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING) // ✅ Use enum
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "source_account")
    private String sourceAccount;

    @Column(name = "destination_account")
    private String destinationAccount;

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "cbs_reference")
    private String cbsReference;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "fraud_score")
    private Integer fraudScore;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "webhook_sent")
    private Boolean webhookSent = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
//
//        if (paymentReference == null) {
//            paymentReference = generatePaymentReference();
//        }
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (retryCount == null) retryCount = 0;
        if (webhookSent == null) webhookSent = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
//    private String generatePaymentReference() {
//        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
//    }
}
