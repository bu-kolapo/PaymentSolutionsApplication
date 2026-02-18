package com.paymentsolutions.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotBlank(message = "Transaction type is required")
    private String type;  // PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED, REFUND_ISSUED

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String description;
    private String metadata;
}

