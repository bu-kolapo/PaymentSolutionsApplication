package com.paymentsolutions.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {

    @NotBlank
    private String channel; // CARD, BANK_TRANSFER, MOBILE_MONEY, WALLET

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String sourceAccount; // Account to debit from

    @NotBlank
    private String destinationAccount; // Account to credit to

    private String narration;

    private String customerEmail;
    private String customerName;

    // For card payments
    private String paymentMethodToken; // pm_card_visa

    // For webhook callbacks
    private String callbackUrl;

    // For bank transfers
    private String destinationBankCode;
}