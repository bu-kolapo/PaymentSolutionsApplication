package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_events", indexes = {
        @Index(name = "idx_event_payment", columnList = "payment_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_event_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    /**
     * Event type: INITIATED, FRAUD_CHECK, CBS_DEBIT, GATEWAY_CALL,
     * CBS_CREDIT, WEBHOOK_SENT, COMPLETED, FAILED, REVERSED
     */
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "provider", length = 30)
    private String provider;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}

