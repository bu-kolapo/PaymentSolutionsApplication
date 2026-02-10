package com.paymentsolutions.dto.request;


import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating merchant information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantUpdateRequest {

    private String businessName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String businessType;

    private String taxId;

    private String webhookUrl;
}