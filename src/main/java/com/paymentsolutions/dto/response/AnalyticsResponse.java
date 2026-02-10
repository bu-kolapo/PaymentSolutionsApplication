package com.paymentsolutions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {

     private BigDecimal totalRevenue;

     private Long totalTransactions;

     private Long successfulTransactions;

     private Long failedTransactions;

     private Double successRate;

     private BigDecimal averageTransactionValue;

     private String period;
}
