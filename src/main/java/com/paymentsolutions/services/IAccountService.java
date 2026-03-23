package com.paymentsolutions.services;
import com.paymentsolutions.dto.request.*;
import com.paymentsolutions.dto.response.*;
import com.paymentsolutions.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IAccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    // Account retrieval
    AccountResponse getAccount(UUID accountId)throws  ResourceNotFoundException;
    AccountResponse getAccountByNumber(String accountNumber)  throws ResourceNotFoundException;
    List<AccountResponse> getCustomerAccounts(UUID customerId);

    // Balance operations
    AccountResponse debitAccount(DebitAccountRequest request) throws ResourceNotFoundException;
    AccountResponse creditAccount(CreditAccountRequest request) throws ResourceNotFoundException;
    BigDecimal getBalance(String accountNumber) throws ResourceNotFoundException;
    BigDecimal getAvailableBalance(String accountNumber) throws ResourceNotFoundException;

    // Account management
    void freezeAccount(String accountNumber, String reason) throws ResourceNotFoundException;
    void unfreezeAccount(String accountNumber)throws ResourceNotFoundException;
    void closeAccount(String accountNumber)throws ResourceNotFoundException;

    // Validation
    boolean hasSufficientBalance(String accountNumber, BigDecimal amount);
    boolean isWithinDailyLimit(String accountNumber, BigDecimal amount);
    AccountResponse fundAccount(UUID accountId, FundAccountRequest request);

    AccountResponse getAccountById(UUID accountId);

}

