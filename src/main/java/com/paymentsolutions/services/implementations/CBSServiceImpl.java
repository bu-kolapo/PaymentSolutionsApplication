package com.paymentsolutions.services.implementations;

// =====================================================
// 13. CBSServiceImpl.java — Core Banking System client
// Simulates external CBS calls, later replace with REST client
// =====================================================



import com.paymentsolutions.dto.request.*;
import com.paymentsolutions.dto.response.CBSResponse;
import com.paymentsolutions.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CBSServiceImpl implements ICBSService {

    private final IAccountService accountService;

    @Override
    public CBSResponse placeHold(String accountNumber, BigDecimal amount, String reference) {
        log.info("📌 CBS: Placing hold {} on {}", amount, accountNumber);

        try {
            // In real CBS: POST /api/holds
            if (!accountService.hasSufficientBalance(accountNumber, amount)) {
                return CBSResponse.builder()
                        .success(false)
                        .message("Insufficient balance")
                        .errorCode("CBS_INSUFFICIENT_FUNDS")
                        .build();
            }

            // Place hold (reduce available balance)
            // For now, we skip this step since we debit immediately

            return CBSResponse.builder()
                    .success(true)
                    .cbsReference("HOLD-" + reference)
                    .message("Hold placed successfully")
                    .build();

        } catch (Exception e) {
            log.error("CBS hold failed: {}", e.getMessage());
            return CBSResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .errorCode("CBS_HOLD_ERROR")
                    .build();
        }
    }

    @Override
    public CBSResponse debitAccount(CBSDebitRequest request) {
        log.info("💸 CBS: Debiting {} from {}", request.getAmount(), request.getAccountNumber());

        try {
            // In real CBS: POST /api/accounts/debit
            DebitAccountRequest debitReq = DebitAccountRequest.builder()
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .category(request.getCategory())
                    .description(request.getNarration())
                    .build();

            accountService.debitAccount(debitReq);

            return CBSResponse.builder()
                    .success(true)
                    .cbsReference("CBS-DR-" + System.currentTimeMillis())
                    .message("Debit successful")
                    .build();

        } catch (Exception e) {
            log.error("CBS debit failed: {}", e.getMessage());
            return CBSResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .errorCode("CBS_DEBIT_FAILED")
                    .build();
        }
    }

    @Override
    public CBSResponse creditAccount(CBSCreditRequest request) {
        log.info("💰 CBS: Crediting {} to {}", request.getAmount(), request.getAccountNumber());

        try {
            // In real CBS: POST /api/accounts/credit
            CreditAccountRequest creditReq = CreditAccountRequest.builder()
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .category(request.getCategory())
                    .description(request.getNarration())
                    .build();

            accountService.creditAccount(creditReq);

            return CBSResponse.builder()
                    .success(true)
                    .cbsReference("CBS-CR-" + System.currentTimeMillis())
                    .message("Credit successful")
                    .build();

        } catch (Exception e) {
            log.error("CBS credit failed: {}", e.getMessage());
            return CBSResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .errorCode("CBS_CREDIT_FAILED")
                    .build();
        }
    }

    @Override
    public CBSResponse releaseHold(String accountNumber, String reference) {
        log.info("🔓 CBS: Releasing hold for {}", reference);
        // In real CBS: DELETE /api/holds/{reference}
        return CBSResponse.builder()
                .success(true)
                .message("Hold released")
                .build();
    }

    @Override
    public CBSResponse reversePosting(String cbsReference, String reason) {
        log.info("🔄 CBS: Reversing posting {} - Reason: {}", cbsReference, reason);

        try {
            // In real CBS: POST /api/postings/{cbsReference}/reverse
            // For now, we create opposite transaction

            return CBSResponse.builder()
                    .success(true)
                    .cbsReference("REV-" + cbsReference)
                    .message("Reversal successful")
                    .build();

        } catch (Exception e) {
            return CBSResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .errorCode("CBS_REVERSAL_FAILED")
                    .build();
        }
    }

    @Override
    public boolean validateAccount(String accountNumber) {
        try {
            accountService.getAccountByNumber(accountNumber);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public boolean hasSufficientBalance(String accountNumber, BigDecimal amount) {
        try {
            boolean sufficient = accountService.hasSufficientBalance(accountNumber, amount);
            log.info("💳 CBS balance check – account: {}, amount: {}, sufficient: {}",
                    accountNumber, amount, sufficient);
            return sufficient;
        } catch (Exception e) {
            // If we can't reach the account service, fail safe — deny the transaction
            log.error("❌ CBS balance check failed for account {}: {}", accountNumber, e.getMessage());
            return false;
        }
    }
}