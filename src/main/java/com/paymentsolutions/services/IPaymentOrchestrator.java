package com.paymentsolutions.services;

import com.paymentsolutions.dto.request.ProcessPaymentRequest;
import com.paymentsolutions.dto.response.PaymentResponse;

import java.util.UUID;

public interface IPaymentOrchestrator {
    /**
     * Process customer payment for existing payment request
     * This is called when customer submits payment on checkout page
     */
    PaymentResponse processCustomerPayment(String paymentReference, ProcessPaymentRequest request);

    /**
     * Reverse a completed payment (refund)
     */
    PaymentResponse reversePayment(UUID paymentId, String reason);
}

