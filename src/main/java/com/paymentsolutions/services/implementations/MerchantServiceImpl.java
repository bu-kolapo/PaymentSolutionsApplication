package com.paymentsolutions.services.implementations;

import com.paymentsolutions.dto.request.MerchantUpdateRequest;
import com.paymentsolutions.dto.response.MerchantResponse;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.model.Merchant;
import com.paymentsolutions.repository.MerchantRepository;
import com.paymentsolutions.services.IMerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of IMerchantService.
 * Handles merchant profile, settings, API key, and webhook management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MerchantServiceImpl implements IMerchantService {

    private final MerchantRepository merchantRepository;

    // =====================================================
    // 1. getMerchant
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(UUID merchantId) throws ResourceNotFoundException{
        log.info("Fetching merchant: {}", merchantId);

        Merchant merchant = findMerchantById(merchantId);
        return mapToResponse(merchant);
    }

    // =====================================================
    // 2. updateMerchant
    // =====================================================
    @Override
    public MerchantResponse updateMerchant(UUID merchantId, MerchantUpdateRequest request) throws ResourceNotFoundException{
        log.info("Updating merchant: {}", merchantId);

        Merchant merchant = findMerchantById(merchantId);

        if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
            merchant.setBusinessName(request.getBusinessName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            merchant.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            merchant.setPhone(request.getPhone());
        }
        if (request.getBusinessType() != null && !request.getBusinessType().isBlank()) {
            merchant.setBusinessType(request.getBusinessType());
        }
        if (request.getTaxId() != null && !request.getTaxId().isBlank()) {
            merchant.setTaxId(request.getTaxId());
        }
        if (request.getWebhookUrl() != null && !request.getWebhookUrl().isBlank()) {
            merchant.setWebhookUrl(request.getWebhookUrl());
        }

        merchant.setUpdatedAt(LocalDateTime.now());
        merchant = merchantRepository.save(merchant);

        log.info("Merchant updated successfully: {}", merchantId);
        return mapToResponse(merchant);
    }

    // =====================================================
    // 3. updateMerchantStatus
    // =====================================================
    @Override
    public MerchantResponse updateMerchantStatus(UUID merchantId, String status) throws ResourceNotFoundException {
        log.info("Updating merchant status: {} to {}", merchantId, status);

        Merchant merchant = findMerchantById(merchantId);
        merchant.setStatus(status);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchant = merchantRepository.save(merchant);

        log.info("Merchant status updated: {} -> {}", merchantId, status);
        return mapToResponse(merchant);
    }

    // =====================================================
    // 4. regenerateApiKey
    // =====================================================
    @Override
    public String regenerateApiKey(UUID merchantId) throws ResourceNotFoundException {
        log.info("Regenerating API key for merchant: {}", merchantId);

        Merchant merchant = findMerchantById(merchantId);

        String newApiKey = generateApiKey();
        merchant.setApiKey(newApiKey);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);

        log.info("API key regenerated for merchant: {}", merchantId);
        return newApiKey;
    }

    // =====================================================
    // 5. updateWebhookUrl
    // =====================================================
    @Override
    public MerchantResponse updateWebhookUrl(UUID merchantId, String webhookUrl)throws ResourceNotFoundException {
        log.info("Updating webhook URL for merchant: {}", merchantId);

        Merchant merchant = findMerchantById(merchantId);
        merchant.setWebhookUrl(webhookUrl);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchant = merchantRepository.save(merchant);

        log.info("Webhook URL updated for merchant: {}", merchantId);
        return mapToResponse(merchant);
    }

    // =====================================================
    // Private Helpers
    // =====================================================

    private Merchant findMerchantById(UUID merchantId)throws ResourceNotFoundException {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found with id: " + merchantId));
    }

    private String generateApiKey() {
        return "sk_live_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .businessName(merchant.getBusinessName())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .businessType(merchant.getBusinessType())
                .taxId(merchant.getTaxId())
                .status(merchant.getStatus())
                .apiKey(merchant.getApiKey())
                .webhookUrl(merchant.getWebhookUrl())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }
}