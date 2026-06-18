package com.paymentsolutions.dto.response;

import com.paymentsolutions.model.LedgerEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryResponse {
    private UUID id;
    private UUID transactionId;
    private UUID paymentId;
    private UUID accountId;
    private String accountNumber;
    private String entryType;
    private String reference;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private BigDecimal availableBalance;
    private String description;
    private String entryReference;
    private LocalDateTime createdAt;
    private List<LedgerEntry> entries;
}