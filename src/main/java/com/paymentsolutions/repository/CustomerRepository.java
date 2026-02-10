package com.paymentsolutions.repository;

import com.paymentsolutions.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findByMerchantId(UUID merchantId, Pageable pageable);

    Optional<Customer> findByMerchantIdAndEmail(UUID merchantId, String email);

    boolean existsByMerchantIdAndEmail(UUID merchantId, String email);
}