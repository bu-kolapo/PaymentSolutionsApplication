package com.paymentsolutions.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class TopCustomerResponse {


    private UUID customerId;

    private String customerName;

    private String email;

    private BigDecimal totalSpent;

    private Long transactionCount;

    private BigDecimal averageTransactionValue;
}
