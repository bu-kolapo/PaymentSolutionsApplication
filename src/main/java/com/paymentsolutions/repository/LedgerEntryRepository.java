package com.paymentsolutions.repository;


import com.paymentsolutions.model.Account;
import com.paymentsolutions.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    // Get all entries for an account
    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    // Get entries for a transaction (should be 2: debit + credit)
    List<LedgerEntry> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId);
//
//    // Get entries for a payment
//    List<LedgerEntry> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);
//
//    // Get account balance
//    @Query("SELECT e.balanceAfter FROM LedgerEntry e WHERE e.accountId = :accountId " +
//            "ORDER BY e.createdAt DESC LIMIT 1")
//    BigDecimal getLatestBalance(UUID accountId);
//
//    // Count entries for account
//    long countByAccountId(UUID accountId);
//
    List<LedgerEntry> findByAccountId(UUID ownerId);
}
