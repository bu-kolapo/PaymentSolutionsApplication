package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit trail of all actions performed in the system.
 * Never updated — only inserted.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_merchant_id",    columnList = "merchant_id"),
        @Index(name = "idx_audit_entity_id",      columnList = "entity_id"),
        @Index(name = "idx_audit_entity_type",    columnList = "entity_type"),
        @Index(name = "idx_audit_performed_by",   columnList = "performed_by"),
        @Index(name = "idx_audit_created_at",     columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /**
     * Type of entity acted upon: PAYMENT, CUSTOMER, MERCHANT,
     * REFUND, USER, API_KEY, FRAUD_RULE, WEBHOOK
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /** ID of the entity that was acted upon */
    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Action performed: CREATE, UPDATE, DELETE, LOGIN, LOGOUT,
     * REFUND, CANCEL, REGENERATE_KEY, WEBHOOK_SENT
     */
    @Column(nullable = false, length = 50)
    private String action;

    /** Snapshot of entity state before the action (JSON string) */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** Snapshot of entity state after the action (JSON string) */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** User who performed the action */
    @Column(name = "performed_by")
    private UUID performedBy;

    /** Email of the user (denormalised for easy reading) */
    @Column(name = "performed_by_email", length = 100)
    private String performedByEmail;

    /** IP address of the request */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** User-Agent header */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /** Optional human-readable description */
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}