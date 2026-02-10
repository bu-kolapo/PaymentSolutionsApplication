package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.RevenueChartData;
import com.paymentsolutions.dto.response.AnalyticsResponse;
import com.paymentsolutions.dto.response.TopCustomerResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for analytics and reporting.
 */
public interface IAnalyticsService {

    /**
     * Get dashboard analytics for a merchant
     *
     * @param merchantId UUID of the merchant
     * @param days Number of days to analyze
     * @return AnalyticsResponse with aggregated metrics
     */
    AnalyticsResponse getDashboardAnalytics(UUID merchantId, int days);

    /**
     * Get revenue data for chart visualization
     *
     * @param merchantId UUID of the merchant
     * @param startDate Start date for the period
     * @param endDate End date for the period
     * @return List of revenue data points
     */
    List<RevenueChartData> getRevenueChartData(
            UUID merchantId, LocalDate startDate, LocalDate endDate);

    /**
     * Get payment method distribution
     *
     * @param merchantId UUID of the merchant
     * @param days Number of days to analyze
     * @return Map of payment method to percentage
     */
    java.util.Map<String, Double> getPaymentMethodDistribution(UUID merchantId, int days);

    /**
     * Get top customers by revenue
     *
     * @param merchantId UUID of the merchant
     * @param limit Number of top customers to return
     * @return List of top customers
     */
    List<TopCustomerResponse> getTopCustomers(
            UUID merchantId, int limit);

    /**
     * Export transaction data to CSV
     *
     * @param merchantId UUID of the merchant
     * @param startDate Start date
     * @param endDate End date
     * @return CSV file as byte array
     */
    byte[] exportTransactions(UUID merchantId, LocalDate startDate, LocalDate endDate);
}
