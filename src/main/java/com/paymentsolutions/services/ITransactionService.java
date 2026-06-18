package com.paymentsolutions.services;


import com.paymentsolutions.dto.response.TransactionResponse;
import com.paymentsolutions.dto.response.TransactionStats;
import com.paymentsolutions.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITransactionService {

    /**
     * Get all transactions for a merchant
     */
    Page<TransactionResponse> getTransactions(UUID merchantId, Pageable pageable);

    TransactionResponse getTransactionById(UUID id);


    /**
     * Get a single transaction by ID
     */
    TransactionResponse getTransaction(UUID transactionId, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Get all transactions for a specific payment (audit trail)
     */
    List<TransactionResponse> getPaymentTransactions(UUID paymentId, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Get transactions by type
     */
    Page<TransactionResponse> getTransactionsByType(
            UUID merchantId, String type, Pageable pageable);

    /**
     * Get transactions by status
     */
    Page<TransactionResponse> getTransactionsByStatus(
            UUID merchantId, String status, Pageable pageable);

    /**
     * Get transaction statistics
     */
    TransactionStats getTransactionStats(UUID merchantId);
}

