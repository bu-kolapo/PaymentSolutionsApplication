package com.paymentsolutions.services;

import com.paymentsolutions.dto.request.TransferRequest;
import com.paymentsolutions.dto.response.AccountResponse;
import com.paymentsolutions.dto.response.LedgerEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ILedgerService {

    /**
     * Get or create MERCHANT settlement account only
     * Customer accounts are NOT created
     */
    AccountResponse getOrCreateAccount(UUID ownerId, String ownerType, String currency);

    /**
     * Get account details
     */
    AccountResponse getAccount(UUID accountId);

    /**
     * Record settlement to merchant account
     * Called when customer payment succeeds
     */
    public void recordSettlement(UUID paymentId, UUID transactionId, UUID accountId, BigDecimal amount, String currency);
    Page<LedgerEntryResponse> getLedgerEntries(UUID accountId, Pageable pageable);
}
