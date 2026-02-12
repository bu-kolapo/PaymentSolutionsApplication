package com.paymentsolutions.controller;


import com.paymentsolutions.dto.request.MerchantUpdateRequest;
import com.paymentsolutions.dto.response.MerchantResponse;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.IMerchantService;
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
 * REST Controller for merchant operations.
 * Manages merchant profile, settings, and API keys.
 */
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchants", description = "Merchant management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class MerchantController {

    private final IMerchantService merchantService;

    /**
     * Get merchant profile
     *
     * @param user Authenticated user
     * @return Merchant details
     */
    @GetMapping("/profile")
    @Operation(summary = "Get merchant profile",
            description = "Retrieve merchant account details")
    public ResponseEntity<MerchantResponse> getProfile(
            @AuthenticationPrincipal User user) throws ResourceNotFoundException {

        log.info("Getting profile for merchant: {}", user.getMerchantId());
        MerchantResponse response = merchantService.getMerchant(user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update merchant profile
     *
     * @param request Updated merchant information
     * @param user Authenticated user
     * @return Updated merchant details
     */
    @PutMapping("/profile")
    @Operation(summary = "Update merchant profile",
            description = "Update merchant account information")
    public ResponseEntity<MerchantResponse> updateProfile(
            @Valid @RequestBody MerchantUpdateRequest request,
            @AuthenticationPrincipal User user)throws ResourceNotFoundException {

        log.info("Updating profile for merchant: {}", user.getMerchantId());
        MerchantResponse response = merchantService.updateMerchant(
                user.getMerchantId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update merchant status
     * (Admin only - would need @PreAuthorize)
     *
     * @param status New status (ACTIVE, SUSPENDED, etc.)
     * @param user Authenticated user
     * @return Updated merchant details
     */
    @PatchMapping("/status")
    @Operation(summary = "Update merchant status",
            description = "Update merchant account status")
    public ResponseEntity<MerchantResponse> updateStatus(
            @RequestParam String status,
            @AuthenticationPrincipal User user) throws ResourceNotFoundException{

        log.info("Updating status for merchant: {} to: {}", user.getMerchantId(), status);
        MerchantResponse response = merchantService.updateMerchantStatus(
                user.getMerchantId(), status);
        return ResponseEntity.ok(response);
    }

    /**
     * Regenerate API key
     *
     * @param user Authenticated user
     * @return New API key
     */
    @PostMapping("/api-key/regenerate")
    @Operation(summary = "Regenerate API key",
            description = "Generate a new API key for the merchant")
    public ResponseEntity<Map<String, String>> regenerateApiKey(
            @AuthenticationPrincipal User user) throws ResourceNotFoundException{

        log.info("Regenerating API key for merchant: {}", user.getMerchantId());
        String newApiKey = merchantService.regenerateApiKey(user.getMerchantId());

        return ResponseEntity.ok(Map.of(
                "apiKey", newApiKey,
                "message", "API key regenerated successfully"
        ));
    }

    /**
     * Update webhook URL
     *
     * @param webhookUrl New webhook URL
     * @param user Authenticated user
     * @return Updated merchant details
     */
    @PatchMapping("/webhook")
    @Operation(summary = "Update webhook URL",
            description = "Set or update the webhook URL for payment notifications")
    public ResponseEntity<MerchantResponse> updateWebhookUrl(
            @RequestParam String webhookUrl,
            @AuthenticationPrincipal User user) throws ResourceNotFoundException{

        log.info("Updating webhook URL for merchant: {}", user.getMerchantId());
        MerchantResponse response = merchantService.updateWebhookUrl(
                user.getMerchantId(), webhookUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Get merchant settings
     *
     * @param user Authenticated user
     * @return Merchant settings
     */
    @GetMapping("/settings")
    @Operation(summary = "Get merchant settings",
            description = "Retrieve merchant configuration settings")
    public ResponseEntity<Map<String, Object>> getSettings(
            @AuthenticationPrincipal User user) throws ResourceNotFoundException{

        log.info("Getting settings for merchant: {}", user.getMerchantId());

        MerchantResponse merchant = merchantService.getMerchant(user.getMerchantId());

        return ResponseEntity.ok(Map.of(
                "merchantId", merchant.getId(),
                "businessName", merchant.getBusinessName(),
                "status", merchant.getStatus(),
                "webhookUrl", merchant.getWebhookUrl() != null ? merchant.getWebhookUrl() : "",
                "apiKeyMasked", maskApiKey(merchant.getApiKey())
        ));
    }

    /**
     * Helper method to mask API key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
