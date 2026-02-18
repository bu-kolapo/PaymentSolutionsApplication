package com.paymentsolutions.repository;


import com.paymentsolutions.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Find all transactions for a merchant
    Page<Transaction> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    // Find all transactions for a specific payment
    List<Transaction> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    // Find by transaction type
    Page<Transaction> findByMerchantIdAndTypeOrderByCreatedAtDesc(
            UUID merchantId, String type, Pageable pageable);

    // Find by status
    Page<Transaction> findByMerchantIdAndStatusOrderByCreatedAtDesc(
            UUID merchantId, String status, Pageable pageable);

    // Find by customer
    Page<Transaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    // Count transactions by merchant
    long countByMerchantId(UUID merchantId);

    // Count by type
    long countByMerchantIdAndType(UUID merchantId, String type);

    // Get recent transactions
    @Query("SELECT t FROM Transaction t WHERE t.merchantId = :merchantId " +
            "AND t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactions(UUID merchantId, LocalDateTime since);

    // Get transaction volume (sum of amounts)
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.merchantId = :merchantId " +
            "AND t.type = 'PAYMENT_COMPLETED' AND t.status = 'SUCCESS'")
    BigDecimal getTotalVolumeByMerchant(UUID merchantId);
}

