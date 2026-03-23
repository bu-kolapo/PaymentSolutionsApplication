package com.paymentsolutions.controller;

import com.paymentsolutions.dto.response.TransactionResponse;
import com.paymentsolutions.dto.response.TransactionStats;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction history and audit trail endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:4004",
        "http://localhost:4003",
        "http://localhost:5173"
})
public class TransactionController {

    private final ITransactionService transactionService;

    // GET /api/v1/transactions
    @GetMapping
    @Operation(summary = "Get all transactions for merchant")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<TransactionResponse> transactions = transactionService.getTransactions(
                user.getMerchantId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    // GET /api/v1/transactions/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) throws ResourceNotFoundException {

        TransactionResponse transaction = transactionService.getTransaction(id, user.getMerchantId());
        return ResponseEntity.ok(transaction);
    }

    // GET /api/v1/transactions/payment/{paymentId}
    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Get all transactions for a specific payment (audit trail)")
    public ResponseEntity<List<TransactionResponse>> getPaymentTransactions(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal User user) throws ResourceNotFoundException{

        List<TransactionResponse> transactions = transactionService.getPaymentTransactions(
                paymentId, user.getMerchantId());
        return ResponseEntity.ok(transactions);
    }

    // GET /api/v1/transactions/type/{type}
    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponse> transactions = transactionService.getTransactionsByType(
                user.getMerchantId(), type, pageable);
        return ResponseEntity.ok(transactions);
    }

    // GET /api/v1/transactions/status/{status}
    @GetMapping("/status/{status}")
    @Operation(summary = "Get transactions by status")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponse> transactions = transactionService.getTransactionsByStatus(
                user.getMerchantId(), status, pageable);
        return ResponseEntity.ok(transactions);
    }

    // GET /api/v1/transactions/stats
    @GetMapping("/stats")
    @Operation(summary = "Get transaction statistics")
    public ResponseEntity<TransactionStats> getTransactionStats(
            @AuthenticationPrincipal User user) {

        TransactionStats stats = transactionService.getTransactionStats(user.getMerchantId());
        return ResponseEntity.ok(stats);
    }
}




















