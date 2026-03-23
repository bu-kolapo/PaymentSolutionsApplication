package com.paymentsolutions.dto.response;

import com.paymentsolutions.model.PaymentStatus;
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
public class PaymentResponse {

    private UUID id;
    private String paymentReference;
    private String paymentLink;
    private BigDecimal amount;
    private String currency;
    private UUID merchantId;
    private UUID customerId;
    private String transactionReference;
    private LocalDateTime updatedAt;
    private PaymentStatus status;
    private String gatewayReference;
    private String customerEmail;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}