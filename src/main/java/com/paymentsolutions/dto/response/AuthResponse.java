package com.paymentsolutions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;  // ✅ Should be Long, not long or Integer

    private UUID userId;

    private String email;

    private String role;

    private UUID merchantId;
}
