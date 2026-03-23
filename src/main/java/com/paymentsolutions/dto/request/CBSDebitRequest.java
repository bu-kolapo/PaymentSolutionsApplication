package com.paymentsolutions.dto.request;


import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CBSDebitRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String narration;
    private String reference;
    private String category; // TRANSFER, PAYMENT, FEE
}