package com.paymentsolutions.model;

// =====================================================
// 2. LedgerEntry.java — Double-entry bookkeeping
// =====================================================


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
 * Immutable ledger entry (debit or credit)
 * Every financial transaction creates TWO entries (double-entry)
 */
@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_account", columnList = "account_id"),
        @Index(name = "idx_ledger_transaction", columnList = "transaction_id"),
        @Index(name = "idx_ledger_payment", columnList = "payment_id"),
        @Index(name = "idx_ledger_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID ownerId;

    /**
     * Reference to the transaction that created this entry
     */
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    /**
     * Reference to payment (if applicable)
     */
    @Column(name = "payment_id")
    private UUID paymentId;

    /**
     * Account being debited or credited
     */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /**
     * Entry type: DEBIT or CREDIT
     */
    @Column(name = "entry_type", nullable = false, length = 10)
    private String entryType; // DEBIT, CREDIT

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Balance after this entry
     */
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    /**
     * Description of this entry
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Entry reference (unique per transaction pair)
     */
    @Column(name = "entry_reference", nullable = false, length = 100)
    private String entryReference;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

