package com.paymentsolutions.dto.response;



import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class AccountResponse {
        private UUID id;
        private String accountNumber;
        private UUID customerId;
        private String accountName;
        private  UUID ownerId;
        private String ownerType;
        private String accountType;
        private String currency;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private BigDecimal dailyLimit;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }



