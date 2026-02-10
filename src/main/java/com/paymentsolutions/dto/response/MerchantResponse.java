package com.paymentsolutions.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for merchant data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {

    private UUID id;

    private String businessName;

    private String email;

    private String phone;

    private String businessType;

    private String taxId;

    private String status;

    private String apiKey;

    private String webhookUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}