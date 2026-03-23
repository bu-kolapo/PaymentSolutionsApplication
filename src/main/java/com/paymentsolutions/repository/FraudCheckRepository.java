package com.paymentsolutions.repository;

import com.paymentsolutions.model.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID> {
    /**
     * Find fraud check by payment ID
     */
    Optional<FraudCheck> findByPaymentId(UUID paymentId);

    /**
     * Find all blocked transactions
     */
    List<FraudCheck> findByDecision(String decision);

    /**
     * Count blocked transactions for a merchant
     */
    long countByDecision(String decision);

    Optional<FraudCheck> findTopByPaymentIdOrderByCreatedAtDesc(UUID paymentId);
}