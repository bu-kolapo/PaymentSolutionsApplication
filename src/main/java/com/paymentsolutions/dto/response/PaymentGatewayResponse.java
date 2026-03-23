package com.paymentsolutions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {
    private boolean success;
    private String gatewayReference;
    private String provider; // STRIPE, PAYSTACK, FLUTTERWAVE
    private String message;
    private String errorCode;
}
