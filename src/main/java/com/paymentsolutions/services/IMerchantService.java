package com.paymentsolutions.services;



import com.paymentsolutions.dto.request.MerchantUpdateRequest;
import com.paymentsolutions.dto.response.MerchantResponse;

import java.util.UUID;

/**
 * Service interface for merchant management operations.
 */
public interface IMerchantService {

    /**
     * Get merchant details by ID
     *
     * @param merchantId UUID of the merchant
     * @return MerchantResponse with merchant details
     * @throws ResourceNotFoundException if merchant not found
     */
    MerchantResponse getMerchant(UUID merchantId);

    /**
     * Update merchant information
     *
     * @param merchantId UUID of the merchant
     * @param request Updated merchant details
     * @return Updated MerchantResponse
     */
    MerchantResponse updateMerchant(UUID merchantId, MerchantUpdateRequest request);

    /**
     * Update merchant status (ACTIVE, SUSPENDED, etc.)
     *
     * @param merchantId UUID of the merchant
     * @param status New status
     * @return Updated MerchantResponse
     */
    MerchantResponse updateMerchantStatus(UUID merchantId, String status);

    /**
     * Generate new API key for merchant
     *
     * @param merchantId UUID of the merchant
     * @return New API key
     */
    String regenerateApiKey(UUID merchantId);

    /**
     * Update webhook URL for merchant
     *
     * @param merchantId UUID of the merchant
     * @param webhookUrl New webhook URL
     * @return Updated MerchantResponse
     */
    MerchantResponse updateWebhookUrl(UUID merchantId, String webhookUrl);
}
