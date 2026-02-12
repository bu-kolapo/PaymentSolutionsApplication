package com.paymentsolutions.repository;



import com.paymentsolutions.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    // Find active API key by key value (used for authentication)
    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);

    // Find all API keys for a merchant
    List<ApiKey> findByMerchantId(UUID merchantId);

    // Find only active keys for a merchant
    List<ApiKey> findByMerchantIdAndActiveTrue(UUID merchantId);

    // Check if a key value already exists (uniqueness check)
    boolean existsByKeyValue(String keyValue);

    // Deactivate all keys for a merchant (used when regenerating)
    List<ApiKey> findByMerchantIdAndActiveTrueOrderByCreatedAtDesc(UUID merchantId);
}