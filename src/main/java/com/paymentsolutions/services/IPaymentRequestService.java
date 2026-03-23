package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.PaymentRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for payment processing operations.
 * Handles payment creation, retrieval, refunds, and cancellations.
 */
public interface IPaymentRequestService {

    PaymentResponse createPaymentRequest(PaymentRequest request, UUID merchantId);
    PaymentResponse getPaymentRequest(String paymentReference);
    String generatePaymentLink(String paymentReference);
    PaymentResponse cancelPaymentRequest(String paymentReference, UUID merchantId);
    /**
     * Get paginated payment requests for a merchant
     */
    Page<PaymentResponse> getPayments(UUID merchantId, String status, Pageable pageable);
}
