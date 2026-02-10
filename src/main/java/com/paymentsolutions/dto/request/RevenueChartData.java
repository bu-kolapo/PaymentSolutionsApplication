package com.paymentsolutions.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueChartData {

    private LocalDate date;

    private BigDecimal revenue;

    private Long transactionCount;

    private BigDecimal averageTransaction;
}
