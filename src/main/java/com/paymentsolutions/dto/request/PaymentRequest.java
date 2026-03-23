package com.paymentsolutions.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
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
public class PaymentRequest {

     @NotNull(message = "Amount is required")
     @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
     private BigDecimal amount;

     @NotBlank(message = "Currency is required")
     private String currency;

     @NotBlank(message = "Customer email is required")
     @Email(message = "Invalid email format")
     private String customerEmail;

     private String customerName;
     private String description;
     private String channel; // CARD, BANK_TRANSFER, etc. (optional, defaults to CARD)
     private String callbackUrl; // Merchant's callback URL (optional)
}