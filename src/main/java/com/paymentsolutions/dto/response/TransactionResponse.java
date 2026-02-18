package com.paymentsolutions.dto.response;


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
public class TransactionResponse {

    private UUID id;
    private UUID paymentId;
    private UUID merchantId;
    private UUID customerId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String previousStatus;
    private String newStatus;
    private String gatewayReference;
    private String transactionReference;
    private String description;
    private String initiatedBy;
    private LocalDateTime createdAt;
}
