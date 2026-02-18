package com.paymentsolutions.services.implementations;

import com.paymentsolutions.dto.response.TransactionResponse;
import com.paymentsolutions.dto.response.TransactionStats;
import com.paymentsolutions.exception.ResourceNotFoundException;

import com.paymentsolutions.model.Transaction;
import com.paymentsolutions.repository.TransactionRepository;
import com.paymentsolutions.services.ITransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Page<TransactionResponse> getTransactions(UUID merchantId, Pageable pageable) {
        log.info("Fetching transactions for merchant: {}", merchantId);
        return transactionRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public TransactionResponse getTransaction(UUID transactionId, UUID merchantId) throws  ResourceNotFoundException{
        log.info("Fetching transaction: {} for merchant: {}", transactionId, merchantId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (!transaction.getMerchantId().equals(merchantId)) {
            throw new ResourceNotFoundException("Transaction not found for this merchant");
        }

        return mapToResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getPaymentTransactions(UUID paymentId, UUID merchantId) throws ResourceNotFoundException{
        log.info("Fetching transaction history for payment: {}", paymentId);
        List<Transaction> transactions = transactionRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId);

        // Verify merchant owns this payment
        if (!transactions.isEmpty() && !transactions.get(0).getMerchantId().equals(merchantId)) {
            throw new ResourceNotFoundException("Payment not found for this merchant");
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TransactionResponse> getTransactionsByType(
            UUID merchantId, String type, Pageable pageable) {
        log.info("Fetching transactions by type: {} for merchant: {}", type, merchantId);
        return transactionRepository.findByMerchantIdAndTypeOrderByCreatedAtDesc(merchantId, type, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<TransactionResponse> getTransactionsByStatus(
            UUID merchantId, String status, Pageable pageable) {
        log.info("Fetching transactions by status: {} for merchant: {}", status, merchantId);
        return transactionRepository.findByMerchantIdAndStatusOrderByCreatedAtDesc(merchantId, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public TransactionStats getTransactionStats(UUID merchantId) {
        log.info("Fetching transaction stats for merchant: {}", merchantId);

        long total      = transactionRepository.countByMerchantId(merchantId);
        long successful = transactionRepository.countByMerchantIdAndType(merchantId, "SUCCESS");
        long failed     = transactionRepository.countByMerchantIdAndType(merchantId, "FAILED");
        long pending    = transactionRepository.countByMerchantIdAndType(merchantId, "PENDING");

        long paymentsCreated   = transactionRepository.countByMerchantIdAndType(merchantId, "PAYMENT_CREATED");
        long paymentsCompleted = transactionRepository.countByMerchantIdAndType(merchantId, "PAYMENT_COMPLETED");
        long refundsIssued     = transactionRepository.countByMerchantIdAndType(merchantId, "REFUND_ISSUED");

        BigDecimal totalVolume = transactionRepository.getTotalVolumeByMerchant(merchantId);
        if (totalVolume == null) totalVolume = BigDecimal.ZERO;

        return TransactionStats.builder()
                .totalTransactions(total)
                .successfulTransactions(successful)
                .failedTransactions(failed)
                .pendingTransactions(pending)
                .totalVolume(totalVolume)
                .paymentsCreated(paymentsCreated)
                .paymentsCompleted(paymentsCompleted)
                .refundsIssued(refundsIssued)
                .build();
    }

    // ── Helper: Map entity to DTO ──
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .paymentId(transaction.getPaymentId())
                .merchantId(transaction.getMerchantId())
                .customerId(transaction.getCustomerId())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .paymentMethod(transaction.getPaymentMethod())
                .previousStatus(transaction.getPreviousStatus())
                .newStatus(transaction.getNewStatus())
                .gatewayReference(transaction.getGatewayReference())
                .transactionReference(transaction.getTransactionReference())
                .description(transaction.getDescription())
                .initiatedBy(transaction.getInitiatedBy())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
