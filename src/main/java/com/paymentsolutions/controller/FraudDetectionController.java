package com.paymentsolutions.controller;

import com.paymentsolutions.dto.response.FraudAnalysisResponse;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.IFraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for fraud detection operations.
 * Provides fraud analysis, risk scoring, and fraud rule management.
 */
@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Detection", description = "Fraud detection and prevention endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class FraudDetectionController {

    private final IFraudDetectionService fraudDetectionService;

    /**
     * Get detailed fraud analysis for a payment
     *
     * @param paymentId Payment ID to analyze
     * @param user Authenticated user
     * @return Detailed fraud analysis
     */
    @GetMapping("/analyze/{paymentId}")
    @Operation(summary = "Analyze payment for fraud",
            description = "Get detailed fraud analysis including risk score and flags")
    public ResponseEntity<FraudAnalysisResponse> analyzeFraud(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal User user) {

        log.info("Fraud analysis requested for payment: {} by merchant: {}",
                paymentId, user.getMerchantId());

        FraudAnalysisResponse response = fraudDetectionService.analyzeFraud(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check customer velocity (transaction frequency)
     *
     * @param customerId Customer ID to check
     * @param hours Time period in hours (default: 1)
     * @param user Authenticated user
     * @return Number of transactions in the period
     */
    @GetMapping("/velocity/{customerId}")
    @Operation(summary = "Check customer velocity",
            description = "Check transaction frequency for a customer")
    public ResponseEntity<Map<String, Object>> checkVelocity(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "1") int hours,
            @AuthenticationPrincipal User user) {

        log.info("Checking velocity for customer: {} over {} hours", customerId, hours);

        long transactionCount = fraudDetectionService.checkCustomerVelocity(customerId, hours);

        return ResponseEntity.ok(Map.of(
                "customerId", customerId,
                "period", hours + " hours",
                "transactionCount", transactionCount,
                "highRisk", transactionCount > 10
        ));
    }

    /**
     * Update fraud detection rules
     * (Admin only)
     *
     * @param ruleType Type of fraud rule to update
     * @param threshold New threshold value
     * @param user Authenticated user
     * @return Success message
     */
    @PutMapping("/rules/{ruleType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update fraud rule",
            description = "Update fraud detection rule threshold (Admin only)")
    public ResponseEntity<Map<String, String>> updateFraudRule(
            @PathVariable String ruleType,
            @RequestParam double threshold,
            @AuthenticationPrincipal User user) {

        log.info("Updating fraud rule: {} to threshold: {} by user: {}",
                ruleType, threshold, user.getId());

        fraudDetectionService.updateFraudRule(ruleType, threshold);

        return ResponseEntity.ok(Map.of(
                "message", "Fraud rule updated successfully",
                "ruleType", ruleType,
                "threshold", String.valueOf(threshold)
        ));
    }

    /**
     * Get fraud statistics for merchant
     *
     * @param days Analysis period in days (default: 30)
     * @param user Authenticated user
     * @return Fraud statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get fraud statistics",
            description = "Get fraud detection statistics for the merchant")
    public ResponseEntity<Map<String, Object>> getFraudStatistics(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User user) {

        log.info("Getting fraud statistics for merchant: {} over {} days",
                user.getMerchantId(), days);

        // This would be implemented in the service
        return ResponseEntity.ok(Map.of(
                "period", days + " days",
                "totalPayments", 100,
                "fraudDetected", 5,
                "fraudRate", 5.0,
                "averageRiskScore", 25.5
        ));
    }
}