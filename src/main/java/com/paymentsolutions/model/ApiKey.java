package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a merchant's API key used for authenticating API requests.
 * Supports multiple keys per merchant and key rotation.
 */
@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_api_key_value",       columnList = "key_value", unique = true),
        @Index(name = "idx_api_key_active",      columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /**
     * The actual API key value. Stored hashed in production.
     * Format: sk_live_xxxxxxxxxxxxxxxx or sk_test_xxxxxxxxxxxxxxxx
     */
    @Column(name = "key_value", nullable = false, unique = true, length = 100)
    private String keyValue;

    /**
     * Human-readable label for the key: "Production Key", "Mobile App Key"
     */
    @Column(length = 100)
    private String label;

    /**
     * Key environment: LIVE, TEST
     */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String environment = "LIVE";

    /** Whether this key is currently active */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Optional expiry date. Null = never expires */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Timestamp of last successful use */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /** IP address that last used this key */
    @Column(name = "last_used_ip", length = 45)
    private String lastUsedIp;

    /** Number of times this key has been used */
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private long usageCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---- Helper Methods ----

    /**
     * Returns true if this key is valid for use.
     */
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        return true;
    }

    /**
     * Returns a masked version for display: sk_live_xxxx...xxxx
     */
    public String getMaskedKey() {
        if (keyValue == null || keyValue.length() < 12) return "****";
        return keyValue.substring(0, 8) + "****" + keyValue.substring(keyValue.length() - 4);
    }
}


