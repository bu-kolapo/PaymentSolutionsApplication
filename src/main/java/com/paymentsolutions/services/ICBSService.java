package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.CBSCreditRequest;
import com.paymentsolutions.dto.request.CBSDebitRequest;
import com.paymentsolutions.dto.response.CBSResponse;

import java.math.BigDecimal;

/**
 * Interface to external Core Banking System
 * In production: REST API calls to actual CBS
 * For now: Simulated with Account/Transaction services
 */
public interface ICBSService {

    /**
     * Place hold on source account before payment
     */
    CBSResponse placeHold(String accountNumber, java.math.BigDecimal amount, String reference);

    /**
     * Debit account (actual posting)
     */
    CBSResponse debitAccount(CBSDebitRequest request);

    /**
     * Credit account (actual posting)
     */
    CBSResponse creditAccount(CBSCreditRequest request);
    boolean hasSufficientBalance(String accountNumber, BigDecimal amount);

    /**
     * Release hold after payment completes/fails
     */
    CBSResponse releaseHold(String accountNumber, String reference);

    /**
     * Reverse a posting
     */
    CBSResponse reversePosting(String cbsReference, String reason);

    /**
     * Validate account exists and is active
     */
    boolean validateAccount(String accountNumber);
}
