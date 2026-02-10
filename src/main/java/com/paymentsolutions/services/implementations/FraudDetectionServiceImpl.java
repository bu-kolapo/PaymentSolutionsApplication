package com.paymentsolutions.services.implementations;



import com.paymentsolutions.dto.response.FraudAnalysisResponse;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.services.IFraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of IFraudDetectionService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionServiceImpl implements IFraudDetectionService {

    private final PaymentRepository paymentRepository;

    private static final BigDecimal HIGH_RISK_AMOUNT = new BigDecimal("5000.00");
    private static final BigDecimal VERY_HIGH_RISK_AMOUNT = new BigDecimal("10000.00");
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;

    @Override
    public boolean checkForFraud(Payment payment) {
        log.info("Running fraud check for payment: {}", payment.getId());

        double riskScore = calculateRiskScore(payment);

        log.info("Fraud risk score for payment {}: {}", payment.getId(), riskScore);

        return riskScore > 75.0;
    }

    @Override
    public double calculateRiskScore(Payment payment) {
        double score = 0.0;

        // Check 1: Amount-based risk
        if (payment.getAmount().compareTo(VERY_HIGH_RISK_AMOUNT) > 0) {
            score += 40.0;
            log.debug("High amount detected: +40 points");
        } else if (payment.getAmount().compareTo(HIGH_RISK_AMOUNT) > 0) {
            score += 20.0;
            log.debug("Moderate amount detected: +20 points");
        }

        // Check 2: Velocity check
        long recentTransactions = checkCustomerVelocity(payment.getCustomerId(), 1);
        if (recentTransactions > MAX_TRANSACTIONS_PER_HOUR) {
            score += 30.0;
            log.debug("High velocity detected: +30 points");
        }

        // Check 3: New customer (first transaction)
        if (isNewCustomer(payment.getCustomerId())) {
            score += 10.0;
            log.debug("New customer: +10 points");
        }

        // Check 4: Unusual time (e.g., 2 AM - 5 AM)
        if (isUnusualTime(payment.getTransactionDate())) {
            score += 15.0;
            log.debug("Unusual time: +15 points");
        }

        return Math.min(score, 100.0);
    }

    @Override
    public FraudAnalysisResponse analyzeFraud(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        double riskScore = calculateRiskScore(payment);
        String riskLevel = getRiskLevel(riskScore);
        List<String> flags = getFraudFlags(payment, riskScore);

        return FraudAnalysisResponse.builder()
                .paymentId(paymentId)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .flags(flags)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public long checkCustomerVelocity(UUID customerId, int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        // Count transactions in the last N hours
        // This is a simplified version
        return 0L; // Would query payment repository
    }

    @Override
    public void updateFraudRule(String ruleType, double threshold) {
        log.info("Updating fraud rule: {} to threshold: {}", ruleType, threshold);
        // Implementation would update fraud detection rules
        // Could be stored in database or configuration
    }

    // Helper methods

    private boolean isNewCustomer(UUID customerId) {
        // Check if customer has any previous transactions
        // Simplified implementation
        return false;
    }

    private boolean isUnusualTime(LocalDateTime transactionDate) {
        if (transactionDate == null) {
            return false;
        }
        int hour = transactionDate.getHour();
        return hour >= 2 && hour <= 5;
    }

    private String getRiskLevel(double riskScore) {
        if (riskScore >= 75) return "HIGH";
        if (riskScore >= 50) return "MEDIUM";
        return "LOW";
    }

    private List<String> getFraudFlags(Payment payment, double riskScore) {
        List<String> flags = new ArrayList<>();

        if (payment.getAmount().compareTo(VERY_HIGH_RISK_AMOUNT) > 0) {
            flags.add("VERY_HIGH_AMOUNT");
        } else if (payment.getAmount().compareTo(HIGH_RISK_AMOUNT) > 0) {
            flags.add("HIGH_AMOUNT");
        }

        if (isNewCustomer(payment.getCustomerId())) {
            flags.add("NEW_CUSTOMER");
        }

        if (isUnusualTime(payment.getTransactionDate())) {
            flags.add("UNUSUAL_TIME");
        }

        long velocity = checkCustomerVelocity(payment.getCustomerId(), 1);
        if (velocity > MAX_TRANSACTIONS_PER_HOUR) {
            flags.add("HIGH_VELOCITY");
        }

        return flags;
    }
}
