package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_checks", indexes = {
        @Index(name = "idx_fraud_payment", columnList = "payment_id"),
        @Index(name = "idx_fraud_decision", columnList = "decision")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore; // 0-100

    /**
     * Decision: ALLOW, BLOCK, REVIEW
     */
    @Column(name = "decision", nullable = false, length = 10)
    private String decision;

    @Column(name = "rules_triggered", columnDefinition = "TEXT")
    private String rulesTriggered; // JSON array of rule IDs

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
