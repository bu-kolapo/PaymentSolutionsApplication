package com.paymentsolutions.services;

import com.paymentsolutions.dto.response.FraudAnalysisResponse;
import com.paymentsolutions.model.Payment;

import java.util.UUID;

/**
 * Service interface for fraud detection and risk analysis.
 */
public interface IFraudDetectionService {

    /**
     * Check if a payment is potentially fraudulent
     *
     * @param payment Payment to analyze
     * @return true if fraud is detected, false otherwise
     */
    boolean checkForFraud(Payment payment);

    /**
     * Calculate risk score for a payment
     *
     * @param payment Payment to analyze
     * @return Risk score between 0 and 100
     */
    double calculateRiskScore(Payment payment);

    /**
     * Get detailed fraud analysis for a payment
     *
     * @param paymentId UUID of the payment
     * @return FraudAnalysisResponse with detailed risk information
     */
    FraudAnalysisResponse analyzeFraud(UUID paymentId);

    /**
     * Check customer velocity (transactions per time period)
     *
     * @param customerId UUID of the customer
     * @param hours Time period in hours
     * @return Number of transactions in the period
     */
    long checkCustomerVelocity(UUID customerId, int hours);

    /**
     * Update fraud rules and thresholds
     *
     * @param ruleType Type of fraud rule
     * @param threshold New threshold value
     */
    void updateFraudRule(String ruleType, double threshold);
}
