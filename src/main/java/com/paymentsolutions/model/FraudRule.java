package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_name", nullable = false, unique = true)
    private String ruleName;

    @Column(name = "rule_type", nullable = false)
    private String ruleType; // AMOUNT_THRESHOLD, VELOCITY_CHECK, BLACKLIST, etc.

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "threshold_amount", precision = 19, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "time_window_minutes")
    private Integer timeWindowMinutes;

    @Column(name = "max_transactions")
    private Integer maxTransactions;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore; // Points to add when rule triggers

    @Column(name = "action", nullable = false)
    private String action; // ALLOW, BLOCK, REVIEW

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (enabled == null) enabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
