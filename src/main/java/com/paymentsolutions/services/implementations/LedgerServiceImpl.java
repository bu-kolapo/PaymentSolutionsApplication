package com.paymentsolutions.services.implementations;

import com.paymentsolutions.dto.request.TransferRequest;
import com.paymentsolutions.dto.response.AccountResponse;
import com.paymentsolutions.dto.response.LedgerEntryResponse;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.model.Account;
import com.paymentsolutions.model.LedgerEntry;
import com.paymentsolutions.repository.AccountRepository;
import com.paymentsolutions.repository.LedgerEntryRepository;
import com.paymentsolutions.services.ILedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerServiceImpl implements ILedgerService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    /**
     * ONLY creates merchant settlement accounts
     * Customer accounts are NOT created (they pay via gateway, no internal account)
     */
    @Override
    @Transactional
    public AccountResponse getOrCreateAccount(UUID ownerId, String ownerType, String currency) {

        // ✅ ONLY allow MERCHANT accounts
        if (!"MERCHANT".equals(ownerType)) {
            throw new PaymentException(
                    "Only merchant accounts allowed. Customers don't need internal accounts.",
                    "INVALID_OWNER_TYPE"
            );
        }

        return accountRepository.findByOwnerIdAndOwnerTypeAndCurrency(ownerId, ownerType, currency)
                .map(this::mapAccountToResponse)
                .orElseGet(() -> {
                    Account account = Account.builder()
                            .accountNumber(generateAccountNumber("MERCHANT"))
                            .accountType("MERCHANT_SETTLEMENT")
                            .ownerId(ownerId)
                            .ownerType("MERCHANT")
                            .currency(currency)
                            .balance(BigDecimal.ZERO)
                            .availableBalance(BigDecimal.ZERO)
                            .status("ACTIVE")
                            .createdAt(LocalDateTime.now())
                            .build();

                    account = accountRepository.save(account);
                    log.info("✅ Created merchant settlement account: {}", account.getAccountNumber());
                    return mapAccountToResponse(account);
                });
    }

    /**
     * Records settlement to merchant account
     * Called when customer payment succeeds
     */
    @Override
    @Transactional
    public void recordSettlement(UUID paymentId, UUID transactionId, UUID accountId, BigDecimal amount, String currency) {

        log.info("💰 Recording settlement: {} {} to account {}", amount, currency, accountId);

        // ✅ STEP 1: Load account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new PaymentException("Account not found"));

        // ✅ STEP 2: Calculate new balance
        BigDecimal newBalance = account.getBalance().add(amount);

        // ✅ STEP 3: Create ledger entry (THIS is your fix)
        LedgerEntry entry = LedgerEntry.builder()
                .paymentId(paymentId)
                .transactionId(transactionId) // ✅ CRITICAL FIX
                .accountId(accountId)
                .entryType("CREDIT")
                .amount(amount)
                .currency(currency)
                .balanceAfter(newBalance)
                .description("Payment settlement")
                .entryReference("SETTLEMENT-" + UUID.randomUUID().toString().substring(0, 8))
                .createdAt(LocalDateTime.now())
                .build();

        // ✅ STEP 4: Save
        ledgerEntryRepository.save(entry);

        log.info("✅ Settlement recorded");
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new PaymentException("Account not found"));
        return mapAccountToResponse(account);
    }

    private String generateAccountNumber(String ownerType) {
        return "ACC-MERCH-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private AccountResponse mapAccountToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .ownerId(account.getOwnerId())
                .ownerType(account.getOwnerType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private LedgerEntryResponse mapToLedgerEntryResponse(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .id(entry.getId())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .entryType(entry.getEntryType())
                .reference(entry.getReference())
                .description(entry.getDescription())
                .balanceAfter(entry.getBalanceAfter())
                .createdAt(entry.getCreatedAt())
                .build();
    }

    @Override
    public Page<LedgerEntryResponse> getLedgerEntries(
            UUID accountId,
            Pageable pageable
    ) {
        log.info("Fetching ledger entries for account: {}", accountId);

        return ledgerEntryRepository
                .findByAccountId(accountId, pageable)
                .map(this::mapToLedgerEntryResponse);
    }
}


