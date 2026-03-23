package com.paymentsolutions.model;

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
 * Financial account that holds a balance
 * Types: CUSTOMER_WALLET, MERCHANT_SETTLEMENT, ESCROW, STRIPE_CLEARING
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_owner", columnList = "owner_id, owner_type"),
        @Index(name = "idx_account_type", columnList = "account_type"),
        @Index(name = "idx_account_number", columnList = "account_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique account number (e.g., ACC-CUST-123456, ACC-MERCH-789012)
     */
    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    private String accountNumber;

    /**
     * Account type: CUSTOMER_WALLET, MERCHANT_SETTLEMENT, ESCROW, STRIPE_CLEARING
     */
    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    /**
     * Owner ID (customer ID, merchant ID, or system)
     */
    @Column(name = "owner_id")
    private UUID ownerId;

    private UUID customerId;
    private BigDecimal dailyLimit;

    /**
     * Owner type: CUSTOMER, MERCHANT, SYSTEM
     */
    @Column(name = "owner_type", length = 20)
    private String ownerType;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    private String freezeReason;

    /**
     * Current balance (updated by ledger entries)
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Available balance (balance - holds/pending)
     */
    @Column(name = "available_balance", precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE, FROZEN, CLOSED

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (availableBalance == null) availableBalance = BigDecimal.ZERO;
    }
}

