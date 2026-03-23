package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.*;
import com.paymentsolutions.dto.response.AccountResponse;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.model.Account;
import com.paymentsolutions.repository.AccountRepository;
import com.paymentsolutions.services.IAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Transactional
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;

    // =========================================================================
    // Account creation
    // =========================================================================
    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for customer: {}", request.getCustomerId());

        // Prevent duplicate accounts for same customer + currency + type
        accountRepository.findByCustomerIdAndCurrencyAndAccountType(
                        request.getCustomerId(), request.getCurrency(), request.getAccountType())
                .ifPresent(existing -> {
                    throw new PaymentException(
                            "Account already exists for this customer, currency and type",
                            "ACCOUNT_EXISTS");
                });

        Account account = Account.builder()
                .customerId(request.getCustomerId())
                .accountNumber(generateAccountNumber())
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .dailyLimit(request.getDailyLimit() != null
                        ? request.getDailyLimit()
                        : new BigDecimal("5000000")) // default 5M NGN
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        account = accountRepository.save(account);
        log.info("Account created: {}", account.getAccountNumber());
        return mapToResponse(account);
    }

    // =========================================================================
    // Account retrieval
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) throws  ResourceNotFoundException{
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber)throws  ResourceNotFoundException {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getCustomerAccounts(UUID customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Balance operations
    // =========================================================================
    @Override
    public AccountResponse debitAccount(DebitAccountRequest request)throws ResourceNotFoundException  {
        log.info("Debiting {} {} from account: {}",
                request.getAmount(), request.getCurrency(), request.getAccountNumber());

        Account account = getActiveAccount(request.getAccountNumber());

        if (!hasSufficientBalance(request.getAccountNumber(), request.getAmount())) {
            throw new PaymentException("Insufficient balance", "INSUFFICIENT_FUNDS");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        account.setAvailableBalance(account.getAvailableBalance().subtract(request.getAmount()));
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        log.info("Debit successful. New balance: {}", account.getBalance());
        return mapToResponse(account);
    }

    @Override
    public AccountResponse creditAccount(CreditAccountRequest request)  throws ResourceNotFoundException{
        log.info("Crediting {} {} to account: {}",
                request.getAmount(), request.getCurrency(), request.getAccountNumber());

        Account account = getActiveAccount(request.getAccountNumber());

        account.setBalance(account.getBalance().add(request.getAmount()));
        account.setAvailableBalance(account.getAvailableBalance().add(request.getAmount()));
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        log.info("Credit successful. New balance: {}", account.getBalance());
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber)  throws ResourceNotFoundException{
        Account account = getActiveAccount(accountNumber);
        return account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAvailableBalance(String accountNumber)  throws ResourceNotFoundException{
        Account account = getActiveAccount(accountNumber);
        return account.getAvailableBalance();
    }

    // =========================================================================
    // Account management
    // =========================================================================
    @Override
    public void freezeAccount(String accountNumber, String reason)  throws ResourceNotFoundException{
        log.warn("Freezing account: {} - Reason: {}", accountNumber, reason);
        Account account = getAccountEntity(accountNumber);
        account.setStatus("FROZEN");
        account.setFreezeReason(reason);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public void unfreezeAccount(String accountNumber) throws ResourceNotFoundException{
        log.info("Unfreezing account: {}", accountNumber);
        Account account = getAccountEntity(accountNumber);

        if (!"FROZEN".equals(account.getStatus())) {
            throw new PaymentException("Account is not frozen", "INVALID_STATUS");
        }

        account.setStatus("ACTIVE");
        account.setFreezeReason(null);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(String accountNumber)throws ResourceNotFoundException {
        log.info("Closing account: {}", accountNumber);
        Account account = getAccountEntity(accountNumber);

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new PaymentException(
                    "Cannot close account with non-zero balance", "ACCOUNT_HAS_BALANCE");
        }

        account.setStatus("CLOSED");
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    // =========================================================================
    // Validation
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(String accountNumber, BigDecimal amount) {
        try {
            Account account = getAccountEntity(accountNumber);
            boolean sufficient = account.getAvailableBalance().compareTo(amount) >= 0;
            log.debug("Balance check – account: {}, required: {}, available: {}, sufficient: {}",
                    accountNumber, amount, account.getAvailableBalance(), sufficient);
            return sufficient;
        } catch (Exception e) {
            log.error("Balance check failed for account {}: {}", accountNumber, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWithinDailyLimit(String accountNumber, BigDecimal amount) {
        try {
            Account account = getAccountEntity(accountNumber);
            if (account.getDailyLimit() == null) return true; // no limit set
            boolean within = amount.compareTo(account.getDailyLimit()) <= 0;
            log.debug("Daily limit check – account: {}, amount: {}, limit: {}, within: {}",
                    accountNumber, amount, account.getDailyLimit(), within);
            return within;
        } catch (Exception e) {
            log.error("Daily limit check failed for account {}: {}", accountNumber, e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Fetch account and enforce ACTIVE status */
    private Account getActiveAccount(String accountNumber) throws ResourceNotFoundException {
        Account account = getAccountEntity(accountNumber);
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new PaymentException(
                    "Account is not active. Status: " + account.getStatus(),
                    "ACCOUNT_NOT_ACTIVE");
        }
        return account;
    }

    /** Fetch account entity without status check */
    private Account getAccountEntity(String accountNumber) throws ResourceNotFoundException {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    /** 10-digit account number generator */
    private String generateAccountNumber() {
        String number;
        do {
            number = String.valueOf((long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

        @Override
        @Transactional
        public AccountResponse fundAccount(UUID accountId, FundAccountRequest request) {
            log.info("💰 Funding account: {} with {}", accountId, request.getAmount());

            // Find account
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new PaymentException("Account not found: " + accountId));

            // Check account is active
            if (!"ACTIVE".equals(account.getStatus())) {
                throw new PaymentException("Account is not active: " + account.getStatus());
            }

            // Add funds
            account.setBalance(account.getBalance().add(request.getAmount()));
            account.setAvailableBalance(account.getAvailableBalance().add(request.getAmount()));

            // Save
            account = accountRepository.save(account);

            log.info("✅ Account funded. New balance: {}", account.getBalance());

            return mapToResponse(account);
        }

        @Override
        public AccountResponse getAccountById(UUID accountId) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new PaymentException("Account not found: " + accountId));
            return mapToResponse(account);
        }

        private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomerId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .dailyLimit(account.getDailyLimit())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}