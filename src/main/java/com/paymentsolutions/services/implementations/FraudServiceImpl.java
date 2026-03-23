package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.response.FraudCheckResponse;
import com.paymentsolutions.model.FraudCheck;
import com.paymentsolutions.model.FraudRule;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.repository.FraudCheckRepository;
import com.paymentsolutions.repository.FraudRuleRepository;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.services.IFraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudServiceImpl implements IFraudService {

    private final PaymentRepository paymentRepository;
    private final FraudCheckRepository fraudCheckRepository;
    private final FraudRuleRepository fraudRuleRepository;


    @Override
    public FraudCheckResponse evaluatePayment(UUID paymentId) {
        log.info("🔍 Evaluating fraud for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        int riskScore = 0;
        List<String> triggeredRules = new ArrayList<>();

        // Rule 1: High amount transactions (>1M)
        if (payment.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            riskScore += 30;
            triggeredRules.add("HIGH_AMOUNT");
            log.warn("⚠️ High amount detected: {}", payment.getAmount());
        }

        // Rule 2: Multiple transactions in short time (velocity check)
        if (payment.getSourceAccount() != null) {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentPayments = paymentRepository.countBySourceAccountAndCreatedAtAfter(
                    payment.getSourceAccount(), oneHourAgo);

            if (recentPayments > 5) {
                riskScore += 25;
                triggeredRules.add("VELOCITY_CHECK");
                log.warn("⚠️ High velocity detected: {} txns in 1 hour", recentPayments);
            }
        }

        // Rule 3: Off-hours transactions (11 PM - 5 AM)
        int hour = LocalDateTime.now().getHour();
        if (hour >= 23 || hour <= 5) {
            riskScore += 15;
            triggeredRules.add("OFF_HOURS");
        }

        // Rule 4: International transfers (currency mismatch)
        if (!"NGN".equals(payment.getCurrency())) {
            riskScore += 10;
            triggeredRules.add("FOREIGN_CURRENCY");
        }

        // Determine decision
        String decision;
        String reason;

        if (riskScore >= 70) {
            decision = "BLOCK";
            reason = "High risk score: " + riskScore;
        } else if (riskScore >= 50) {
            decision = "REVIEW";
            reason = "Medium risk score: " + riskScore;
        } else {
            decision = "ALLOW";
            reason = "Low risk score: " + riskScore;
        }

        // Save fraud check result
        FraudCheck fraudCheck = FraudCheck.builder()
                .paymentId(paymentId)
                .riskScore(riskScore)
                .decision(decision)
                .rulesTriggered(String.join(",", triggeredRules))
                .reason(reason)
                .build();

        fraudCheckRepository.save(fraudCheck);

        log.info("🔍 Fraud check result: {} - Score: {}", decision, riskScore);

        return FraudCheckResponse.builder()
                .riskScore(riskScore)
                .decision(decision)
                .reason(reason)
                .rulesTriggered(triggeredRules.toArray(new String[0]))
                .build();
    }

    @Override
    public void blockEntity(String entityType, String entityId, String reason) {
        log.warn("🚫 Blocking {} {}: {}", entityType, entityId, reason);
        // Implementation: Create a fraud rule to block this entity
        FraudRule blockRule = FraudRule.builder()
                .ruleName("BLOCK_" + entityType + "_" + entityId)
                .ruleType("BLACKLIST")
                .description("Blocked: " + reason)
                .action("BLOCK")
                .riskScore(100)
                .enabled(true)
                .build();

        fraudRuleRepository.save(blockRule);
    }
}