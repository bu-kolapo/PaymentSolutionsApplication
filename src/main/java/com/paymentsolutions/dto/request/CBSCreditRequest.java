package com.paymentsolutions.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CBSCreditRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String narration;
    private String reference;
    private String category;
}