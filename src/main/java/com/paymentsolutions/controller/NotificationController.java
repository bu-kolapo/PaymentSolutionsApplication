package com.paymentsolutions.controller;


import com.paymentsolutions.dto.request.EmailRequest;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for notification operations.
 * Manages email, SMS, and other notification channels.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final INotificationService notificationService;

    /**
     * Send custom email
     *
     * @param request Email details
     * @param user Authenticated user
     * @return Success message
     */
    @PostMapping("/email")
    @Operation(summary = "Send email",
            description = "Send a custom email notification")
    public ResponseEntity<Map<String, String>> sendEmail(
            @Valid @RequestBody EmailRequest request,
            @AuthenticationPrincipal User user) {

        log.info("Sending email to: {} from merchant: {}", request.getTo(), user.getMerchantId());

        notificationService.sendEmail(request);

        return ResponseEntity.ok(Map.of(
                "message", "Email sent successfully",
                "to", request.getTo(),
                "subject", request.getSubject()
        ));
    }

    /**
     * Send SMS notification
     *
     * @param phoneNumber Recipient phone number
     * @param message SMS message
     * @param user Authenticated user
     * @return Success message
     */
    @PostMapping("/sms")
    @Operation(summary = "Send SMS",
            description = "Send an SMS notification")
    public ResponseEntity<Map<String, String>> sendSms(
            @RequestParam String phoneNumber,
            @RequestParam String message,
            @AuthenticationPrincipal User user) {

        log.info("Sending SMS to: {} from merchant: {}", phoneNumber, user.getMerchantId());

        notificationService.sendSms(phoneNumber, message);

        return ResponseEntity.ok(Map.of(
                "message", "SMS sent successfully",
                "to", phoneNumber
        ));
    }

    /**
     * Get notification history
     *
     * @param user Authenticated user
     * @return Notification history
     */
    @GetMapping("/history")
    @Operation(summary = "Get notification history",
            description = "Retrieve notification history for the merchant")
    public ResponseEntity<Map<String, Object>> getHistory(
            @AuthenticationPrincipal User user) {

        log.info("Getting notification history for merchant: {}", user.getMerchantId());

        // This would be implemented in the service
        return ResponseEntity.ok(Map.of(
                "merchantId", user.getMerchantId(),
                "totalSent", 150,
                "emails", 120,
                "sms", 30,
                "recentNotifications", new Object[]{}
        ));
    }

    /**
     * Get notification preferences
     *
     * @param user Authenticated user
     * @return Notification preferences
     */
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences",
            description = "Get merchant's notification preferences")
    public ResponseEntity<Map<String, Object>> getPreferences(
            @AuthenticationPrincipal User user) {

        log.info("Getting notification preferences for merchant: {}", user.getMerchantId());

        return ResponseEntity.ok(Map.of(
                "emailNotifications", true,
                "smsNotifications", false,
                "paymentConfirmations", true,
                "fraudAlerts", true,
                "dailySummary", true
        ));
    }

    /**
     * Update notification preferences
     *
     * @param preferences Updated preferences
     * @param user Authenticated user
     * @return Success message
     */
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences",
            description = "Update merchant's notification preferences")
    public ResponseEntity<Map<String, String>> updatePreferences(
            @RequestBody Map<String, Boolean> preferences,
            @AuthenticationPrincipal User user) {

        log.info("Updating notification preferences for merchant: {}", user.getMerchantId());

        // This would be implemented in the service
        return ResponseEntity.ok(Map.of(
                "message", "Notification preferences updated successfully"
        ));
    }
}