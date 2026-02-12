package com.paymentsolutions.repository;


import com.paymentsolutions.model.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {

    // Find a rule by its type (e.g. "HIGH_AMOUNT", "VELOCITY")
    Optional<FraudRule> findByRuleType(String ruleType);

    // Get all active rules
    List<FraudRule> findByActiveTrue();

    // Get all active rules for a specific merchant (merchant-level overrides)
    List<FraudRule> findByMerchantIdAndActiveTrue(UUID merchantId);

    // Get global rules (not tied to any merchant)
    List<FraudRule> findByMerchantIdIsNullAndActiveTrue();

    // Check if a rule type exists for a merchant
    boolean existsByMerchantIdAndRuleType(UUID merchantId, String ruleType);

    // Find rules by category
    List<FraudRule> findByCategory(String category);

    // Find all rules ordered by priority
    @Query("SELECT f FROM FraudRule f WHERE f.active = true ORDER BY f.priority ASC")
    List<FraudRule> findAllActiveOrderByPriority();
}
