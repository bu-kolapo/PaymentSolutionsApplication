package com.paymentsolutions.repository;

import com.paymentsolutions.model.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {
    Optional<FraudRule> findByRuleName(String ruleName);
    List<FraudRule> findByEnabledTrue();
    List<FraudRule> findByRuleType(String ruleType);
}