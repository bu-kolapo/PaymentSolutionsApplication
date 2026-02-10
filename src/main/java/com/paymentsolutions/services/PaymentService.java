package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.PaymentRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.dto.request.RefundRequest;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for payment processing operations.
 * Handles payment creation, retrieval, refunds, and cancellations.
 */
public interface PaymentService {

    /**
     * Process a new payment transaction
     *
     * @param request Payment details including amount, currency, and payment method
     * @param merchantId ID of the merchant initiating the payment
     * @return PaymentResponse with transaction details
     * @throws PaymentException if payment processing fails
     * @throws FraudException if fraud is detected
     */
    PaymentResponse processPayment(PaymentRequest request, UUID merchantId) throws PaymentException;

    /**
     * Retrieve a specific payment by ID
     *
     * @param paymentId UUID of the payment
     * @param merchantId UUID of the merchant (for authorization)
     * @return PaymentResponse with full payment details
     * @throws ResourceNotFoundException if payment not found
     */
    PaymentResponse getPayment(UUID paymentId, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Get paginated list of payments for a merchant
     *
     * @param merchantId UUID of the merchant
     * @param status Optional payment status filter
     * @param pageable Pagination parameters
     * @return Page of PaymentResponse objects
     */
    Page<PaymentResponse> getPayments(UUID merchantId, String status, Pageable pageable);

    /**
     * Process a refund for an existing payment
     *
     * @param paymentId UUID of the original payment
     * @param request Refund details (amount, reason)
     * @param merchantId UUID of the merchant (for authorization)
     * @return PaymentResponse representing the refund transaction
     * @throws PaymentException if refund cannot be processed
     */
    PaymentResponse refundPayment(UUID paymentId, RefundRequest request, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Cancel a pending payment
     *
     * @param paymentId UUID of the payment to cancel
     * @param merchantId UUID of the merchant (for authorization)
     * @return Updated PaymentResponse with CANCELLED status
     */
    PaymentResponse cancelPayment(UUID paymentId, UUID merchantId) throws ResourceNotFoundException;

    /**
     * Get payment statistics for a merchant
     *
     * @param merchantId UUID of the merchant
     * @param days Number of days to analyze
     * @return Map containing payment statistics
     */
    java.util.Map<String, Object> getPaymentStatistics(UUID merchantId, int days);
}
