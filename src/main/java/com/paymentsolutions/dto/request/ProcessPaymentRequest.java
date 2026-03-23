package com.paymentsolutions.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD, BANK_TRANSFER, MOBILE_MONEY, WALLET

    private String paymentMethodToken; // For Stripe: pm_card_visa, etc.

    // Card details (if not using token)
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String cardholderName;

    // Bank transfer details
    private String bankCode;
    private String accountNumber;

    // Mobile money details
    private String phoneNumber;
    private String network; // MTN, AIRTEL, etc.
}
