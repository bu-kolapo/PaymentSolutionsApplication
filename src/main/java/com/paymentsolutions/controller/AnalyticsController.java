package com.paymentsolutions.controller;

import com.paymentsolutions.dto.response.AnalyticsResponse;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.IAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for analytics operations.
 * Uses IAnalyticsService interface for loose coupling.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final IAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard analytics")
    public ResponseEntity<AnalyticsResponse> getDashboardAnalytics(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User user) {
        AnalyticsResponse response = analyticsService.getDashboardAnalytics(
                user.getMerchantId(), days);
        return ResponseEntity.ok(response);
    }
}