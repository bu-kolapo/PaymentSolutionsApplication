package com.paymentsolutions.repository;


import com.paymentsolutions.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    /**
     * Find merchant by email
     */
    Optional<Merchant> findByEmail(String email);

    /**
     * Find merchant by business name
     */
    Optional<Merchant> findByBusinessName(String businessName);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find active merchants
     */
    List<Merchant> findByStatus(String status);
}

