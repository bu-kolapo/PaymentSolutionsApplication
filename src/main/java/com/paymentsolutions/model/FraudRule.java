package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a configurable fraud detection rule.
 * Rules can be global (merchantId = null) or merchant-specific.
 */
@Entity
@Table(name = "fraud_rules", indexes = {
        @Index(name = "idx_fraud_rule_type",        columnList = "rule_type"),
        @Index(name = "idx_fraud_rule_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_fraud_rule_active",      columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Merchant-specific rule. Null = global rule applied to all merchants.
     */
    @Column(name = "merchant_id")
    private UUID merchantId;

    /**
     * Rule identifier: HIGH_AMOUNT, VELOCITY, NEW_CUSTOMER,
     * UNUSUAL_TIME, BLACKLISTED_IP, INTERNATIONAL_CARD, etc.
     */
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    /**
     * Human-readable name: "High Transaction Amount"
     */
    @Column(nullable = false, length = 100)
    private String name;

    /** Description explaining what the rule detects */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Category: AMOUNT, VELOCITY, BEHAVIOUR, GEOGRAPHY, DEVICE
     */
    @Column(length = 50)
    private String category;

    /**
     * The numeric threshold for this rule (e.g. 5000.00 for HIGH_AMOUNT,
     * 10 for max transactions per hour in VELOCITY check)
     */
    @Column(nullable = false)
    private double threshold;

    /**
     * Risk score added when this rule is triggered (0 - 100)
     */
    @Column(name = "risk_score_weight", nullable = false)
    @Builder.Default
    private double riskScoreWeight = 10.0;

    /**
     * Evaluation order — lower number = evaluated first
     */
    @Column(nullable = false)
    @Builder.Default
    private int priority = 10;

    /** Whether this rule is currently active */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
