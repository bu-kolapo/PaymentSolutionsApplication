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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {


     @NotNull(message = "Customer ID is required")
     private UUID customerId;

     @NotNull(message = "Amount is required")
     @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
     private BigDecimal amount;

     @NotBlank(message = "Currency is required")
     @Size(min = 3, max = 3, message = "Currency must be 3 characters")
     private String currency;

     @NotBlank(message = "Payment method is required")
     private String paymentMethod;

     @NotBlank(message = "Payment method token is required")
     private String paymentMethodToken;

     private String description;

     @NotBlank(message = "Customer email is required")
     @Email(message = "Invalid email format")
     private String customerEmail;

     @NotBlank(message = "Customer name is required")
     private String customerName;

     private String ipAddress;

     private String userAgent;

}
