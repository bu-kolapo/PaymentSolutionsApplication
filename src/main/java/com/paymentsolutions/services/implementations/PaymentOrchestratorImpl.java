package com.paymentsolutions.services.implementations;
import com.paymentsolutions.dto.request.ProcessPaymentRequest;
import com.paymentsolutions.dto.response.AccountResponse;
import com.paymentsolutions.dto.response.FraudCheckResponse;
import com.paymentsolutions.dto.response.PaymentGatewayResponse;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.model.*;
import com.paymentsolutions.repository.AccountRepository;
import com.paymentsolutions.repository.PaymentEventRepository;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.repository.TransactionRepository;
import com.paymentsolutions.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrchestratorImpl implements IPaymentOrchestrator {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final AccountRepository accountRepository;

    private  final TransactionRepository transactionRepository;
    private final IFraudService fraudService;
    private final IWebhookService webhookService;
    private final IPaymentGatewayService gatewayService;
    private final ILedgerService ledgerService;

    /**
     * REAL PAYMENT GATEWAY FLOW:
     * 1. Payment request already exists (created by merchant)
     * 2. Customer provides payment details
     * 3. Fraud check
     * 4. Call payment gateway (Stripe/Paystack)
     * 5. Gateway contacts card network → issuing bank
     * 6. Bank approves/declines
     * 7. Credit merchant settlement account
     * 8. Send webhook to merchant
     */
    @Override
    @Transactional
    public PaymentResponse processCustomerPayment(String paymentReference, ProcessPaymentRequest request) {
        log.info("💳 Customer paying for: {}", paymentReference);

        // STEP 1: Get payment request (created by merchant)
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentException("Payment request not found", "NOT_FOUND"));

        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new PaymentException("Payment already processed", "INVALID_STATUS");
        }

        // Update with customer's payment method
        payment.setChannel(request.getPaymentMethod());
        // Attach customer to payment (VERY IMPORTANT)
        payment.setCustomerId(request.getCustomerId());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        logEvent(payment.getId(), "CUSTOMER_INITIATED", "SUCCESS", "Customer started payment");

        try {
            // STEP 2: Fraud Check
            log.info("🔍 Running fraud check...");
            FraudCheckResponse fraudCheck = fraudService.evaluatePayment(payment.getId());
            payment.setFraudScore(fraudCheck.getRiskScore());

            if ("BLOCK".equals(fraudCheck.getDecision())) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                logEvent(payment.getId(), "FRAUD_BLOCKED", "FAILED", fraudCheck.getReason());
                throw new PaymentException("Payment blocked: " + fraudCheck.getReason(), "FRAUD_DETECTED");
            }

            logEvent(payment.getId(), "FRAUD_CHECK_PASSED", "SUCCESS", "Risk score: " + fraudCheck.getRiskScore());

            // STEP 3: Call Payment Gateway (Stripe, Paystack, etc.)
            // Gateway will contact:
            //   → Card network (Visa/Mastercard)
            //   → Issuing bank
            //   → Get approval/decline
            log.info("💳 Calling payment gateway...");

            PaymentGatewayResponse gatewayResponse = gatewayService.processPayment(payment, request);

            if (!gatewayResponse.isSuccess()) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                logEvent(payment.getId(), "GATEWAY_DECLINED", "FAILED", gatewayResponse.getMessage());
                throw new PaymentException("Payment declined: " + gatewayResponse.getMessage(), "DECLINED");
            }

            // Gateway approved!
            payment.setGatewayReference(gatewayResponse.getGatewayReference());
            payment.setProvider(gatewayResponse.getProvider());
            logEvent(payment.getId(), "GATEWAY_APPROVED", "SUCCESS", gatewayResponse.getGatewayReference());

            // STEP 4: Credit merchant settlement account
            // This is where merchant receives the money
            log.info("💰 Crediting merchant settlement account...");

            Transaction transaction = transactionRepository.save(
                    Transaction.builder()
                            .paymentId(payment.getId())
                            .amount(payment.getAmount())
                            .customerId(payment.getCustomerId())
                            .merchantId(payment.getMerchantId())
                            .currency(payment.getCurrency())
                            .status("SUCCESS")
                            .type("SETTLEMENT")

                            // ✅ KEEP your existing referenceId (if used elsewhere)
                            .referenceId("TX-" + UUID.randomUUID())

                            // 🔴 THIS IS THE CRITICAL FIX (matches DB column)
                            .transactionReference("TX-" + UUID.randomUUID())

                            .createdAt(LocalDateTime.now())
                            .build()
            );

             log.info("🔥 Transaction created with ID: {}", transaction.getId());

            AccountResponse merchantAccount = ledgerService.getOrCreateAccount(
                    payment.getMerchantId(),
                    "MERCHANT",
                    payment.getCurrency()
            );

            // Lock account and credit
            Account account = accountRepository.findByIdForUpdate(merchantAccount.getId())
                    .orElseThrow(() -> new PaymentException("Merchant account not found"));

            account.setBalance(account.getBalance().add(payment.getAmount()));
            account.setAvailableBalance(account.getAvailableBalance().add(payment.getAmount()));
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

            // Record in ledger
            ledgerService.recordSettlement(
                    payment.getId(),
                    transaction.getId(),   // ✅ THIS IS THE FIX
                    account.getId(),
                    payment.getAmount(),
                    payment.getCurrency()
            );

            logEvent(payment.getId(), "MERCHANT_CREDITED", "SUCCESS",
                    "Credited " + account.getAccountNumber());

            // STEP 5: Mark payment as completed
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setCustomerId(request.getCustomerId());
            payment = paymentRepository.save(payment);

            logEvent(payment.getId(), "PAYMENT_COMPLETED", "SUCCESS", null);

            // STEP 6: Send webhook to merchant (async)
            try {
                webhookService.sendPaymentSuccessWebhook(payment.getId());
                payment.setWebhookSent(true);
                paymentRepository.save(payment);
            } catch (Exception e) {
                log.warn("⚠️ Webhook failed, will retry: {}", e.getMessage());
            }

            log.info("✅ Payment completed: {}", payment.getPaymentReference());
            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("❌ Payment failed: {}", e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            logEvent(payment.getId(), "PAYMENT_FAILED", "FAILED", e.getMessage());

            // Send failure webhook
            try {
                webhookService.sendPaymentFailedWebhook(payment.getId());
            } catch (Exception we) {
                log.warn("Failed to send failure webhook");
            }

            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse reversePayment(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        if (!PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            throw new PaymentException("Can only reverse completed payments");
        }

        log.info("🔄 Reversing payment: {}", payment.getPaymentReference());

        // Refund via gateway
        gatewayService.refund(payment);

        // Debit merchant account
        Account merchantAccount = accountRepository.findByOwnerIdAndOwnerTypeAndCurrency(
                        payment.getMerchantId(), "MERCHANT", payment.getCurrency())
                .orElseThrow(() -> new PaymentException("Merchant account not found"));

        merchantAccount.setBalance(merchantAccount.getBalance().subtract(payment.getAmount()));
        merchantAccount.setAvailableBalance(merchantAccount.getAvailableBalance().subtract(payment.getAmount()));
        accountRepository.save(merchantAccount);

        payment.setStatus(PaymentStatus.REVERSED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        logEvent(payment.getId(), "PAYMENT_REVERSED", "SUCCESS", reason);

        webhookService.sendPaymentFailedWebhook(payment.getId());

        return mapToResponse(payment);
    }

    // Helper methods
    private void logEvent(UUID paymentId, String eventType, String status, String message) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .eventType(eventType)
                .status(status)
                .errorMessage(message)
                .build();
        paymentEventRepository.save(event);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .customerEmail(payment.getCustomerEmail())
                .gatewayReference(payment.getGatewayReference())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}




















