package com.paymentsolutions.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_name", unique = true, nullable = false)
    private String providerName; // STRIPE, PAYSTACK, FLUTTERWAVE

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "api_key", columnDefinition = "TEXT")
    private String apiKey;

    @Column(name = "secret_key", columnDefinition = "TEXT")
    private String secretKey;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 30;

    @Column(name = "priority")
    private Integer priority = 1; // For routing/failover

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}






















