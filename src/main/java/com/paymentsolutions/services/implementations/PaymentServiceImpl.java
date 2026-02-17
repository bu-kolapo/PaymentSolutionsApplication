package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.PaymentRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.dto.request.RefundRequest;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.model.PaymentStatus;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.services.IFraudDetectionService;
import com.paymentsolutions.services.INotificationService;
import com.paymentsolutions.services.IPaymentGateway;
import com.paymentsolutions.services.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

/**
 * Implementation of IPaymentService.
 * Handles all payment processing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

   @Value("${stripe.api-key}")
    private String stripeApiKey;

    private final PaymentRepository paymentRepository;
    private final IFraudDetectionService fraudDetectionService;
    private final INotificationService notificationService;
    private final IPaymentGateway paymentGateway;


    @Override
    public PaymentResponse processPayment(PaymentRequest request, UUID merchantId) {
        log.info("Processing payment for merchant: {}", merchantId);

        Stripe.apiKey = stripeApiKey;

        // ✅ Step 1: Generate idempotency key FIRST (before using it)
        String idempotencyKey = merchantId + "_"
                + request.getCustomerId() + "_"
                + request.getAmount() + "_"
                + request.getCurrency();

        // ✅ Step 2: Check for duplicate payment — return existing if found
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.warn("Duplicate payment detected, returning existing: {}", existing.get().getId());
            return mapToResponse(existing.get());
        }

        // ✅ Step 3: Save payment as PENDING
        Payment payment = Payment.builder()
                .merchantId(merchantId)
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .idempotencyKey(idempotencyKey)   // ✅ now declared above, safe to use
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        try {
            // ✅ Step 4: Convert amount to smallest currency unit (kobo/cents)
            long amountInSmallestUnit = convertToSmallestUnit(
                    request.getAmount().doubleValue(),
                    request.getCurrency()
            );

            // ✅ Step 5: Build Stripe PaymentIntent params
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setPaymentMethod(request.getPaymentMethodToken())
                    .setConfirm(true)
                    .setReturnUrl("http://localhost:4003/payments")
                    .putMetadata("merchantId",    merchantId.toString())
                    .putMetadata("customerId",    request.getCustomerId().toString())
                    .putMetadata("customerEmail", request.getCustomerEmail())
                    .build();

            // ✅ Step 6: Build Stripe request options with idempotency key
            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            // ✅ Step 7: Call Stripe
            PaymentIntent paymentIntent = PaymentIntent.create(createParams, requestOptions);

            // ✅ Step 8: Update payment as COMPLETED
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewayReference(paymentIntent.getId());
            payment.setUpdatedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            log.info("Payment completed: {}", payment.getId());
            return mapToResponse(payment);

        } catch (StripeException e) {
            log.error("Payment failed: {}, error: {}", payment.getId(), e.getMessage());

            // ✅ Step 9: Mark as FAILED and save
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }


    @Override
    @Cacheable(value = "payments", key = "#paymentId")
    public PaymentResponse getPayment(UUID paymentId, UUID merchantId) throws ResourceNotFoundException{
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new PaymentException("Access denied", "UNAUTHORIZED");
        }

        return mapToResponse(payment);
    }

    @Override
    public Page<PaymentResponse> getPayments(UUID merchantId, String status, Pageable pageable) {
        Page<Payment> payments;

        if (status != null && !status.isEmpty()) {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            payments = paymentRepository.findByMerchantIdAndStatus(merchantId, paymentStatus, pageable);
        } else {
            payments = paymentRepository.findByMerchantId(merchantId, pageable);
        }

        return payments.map(this::mapToResponse);
    }

    @Override
    @CacheEvict(value = "payments", key = "#paymentId")
    public PaymentResponse refundPayment(UUID paymentId, RefundRequest request, UUID merchantId)throws ResourceNotFoundException {
        log.info("Processing refund for payment: {}", paymentId);

        Payment originalPayment = getPaymentEntity(paymentId, merchantId);
        validateRefund(originalPayment);

        BigDecimal refundAmount = request.getAmount() != null
                ? request.getAmount()
                : originalPayment.getAmount();

        if (refundAmount.compareTo(originalPayment.getAmount()) > 0) {
            throw new PaymentException("Refund amount exceeds original amount", "INVALID_AMOUNT");
        }

        try {
            // Process refund with gateway
            String refundReference = paymentGateway.refundPayment(
                    originalPayment.getGatewayReference(),
                    refundAmount
            );

            // Create refund record
            Payment refund = createRefundEntity(originalPayment, refundAmount, refundReference);
            refund = paymentRepository.save(refund);

            // Update original payment status
            updateOriginalPaymentStatus(originalPayment, refundAmount);

            notificationService.sendRefundConfirmation(refund);

            log.info("Refund processed successfully: {}", refund.getId());
            return mapToResponse(refund);

        } catch (Exception e) {
            log.error("Refund processing failed: {}", e.getMessage(), e);
            throw new PaymentException("Refund processing failed: " + e.getMessage(), "REFUND_ERROR");
        }
    }

    @Override
    public PaymentResponse cancelPayment(UUID paymentId, UUID merchantId)  throws  ResourceNotFoundException{
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new PaymentException("Access denied", "UNAUTHORIZED");
        }

        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new PaymentException("Only pending payments can be cancelled", "INVALID_STATUS");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Override
    public Map<String, Object> getPaymentStatistics(UUID merchantId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        long totalCount = paymentRepository.countByMerchantIdAndStatusSince(
                merchantId, PaymentStatus.COMPLETED, startDate);

        BigDecimal totalRevenue = paymentRepository.getTotalRevenueForMerchantSince(
                merchantId, startDate).orElse(BigDecimal.ZERO);

        return Map.of(
                "totalTransactions", totalCount,
                "totalRevenue", totalRevenue,
                "period", days + " days"
        );
    }

    // Helper methods

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Invalid payment amount", "INVALID_AMOUNT");
        }
        if (request.getCurrency() == null || request.getCurrency().length() != 3) {
            throw new PaymentException("Invalid currency code", "INVALID_CURRENCY");
        }
        if (request.getCustomerId() == null) {
            throw new PaymentException("Customer ID is required", "MISSING_CUSTOMER");
        }
    }

    private Payment createPaymentEntity(PaymentRequest request, UUID merchantId) {
        return Payment.builder()
                .merchantId(merchantId)
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .status(PaymentStatus.PENDING)
                .transactionReference(generateTransactionReference())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build();
    }

    private String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(payment.getTransactionReference())
                .gatewayReference(payment.getGatewayReference())
                .description(payment.getDescription())
                .customerEmail(payment.getCustomerEmail())
                .customerName(payment.getCustomerName())
                .transactionDate(payment.getTransactionDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Handle fraud detection - update payment and send alerts
     */
    private void handleFraudDetection(Payment payment) {
        log.warn("Fraud detected for payment: {}", payment.getId());

        payment.setStatus(PaymentStatus.FRAUD_DETECTED);
        payment.setErrorMessage("Transaction flagged as potentially fraudulent");
        paymentRepository.save(payment);

        // Send fraud alert to merchant
        notificationService.sendFraudAlert(payment);
    }

    /**
     * Handle payment failure - update status and send notification
     */
    private void handlePaymentFailure(Payment payment, Exception exception) {
        log.error("Payment failed: {}, error: {}", payment.getId(), exception.getMessage());

        payment.markAsFailed("GATEWAY_ERROR", exception.getMessage());
        paymentRepository.save(payment);

        // Send failure notification to customer
        notificationService.sendPaymentFailureNotification(payment);
    }
    /**
     * Create payment entity from request
     */
    /**
     * Validate if payment can be refunded
     */
    private void validateRefund(Payment payment) {
        if (!payment.canBeRefunded()) {
            throw new PaymentException(
                    "Payment cannot be refunded. Status: " + payment.getStatus(),
                    "REFUND_NOT_ALLOWED"
            );
        }
    }

    /**
     * Create refund entity
     */
    private Payment createRefundEntity(Payment originalPayment, BigDecimal amount, String refundReference) {
        return Payment.builder()
                .merchantId(originalPayment.getMerchantId())
                .customerId(originalPayment.getCustomerId())
                .amount(amount.negate())  // Negative amount for refund
                .currency(originalPayment.getCurrency())
                .status(PaymentStatus.REFUNDED)
                .paymentMethod(originalPayment.getPaymentMethod())
                .gatewayReference(refundReference)
                .transactionReference(generateTransactionReference())
                .description("Refund for " + originalPayment.getTransactionReference())
                .customerEmail(originalPayment.getCustomerEmail())
                .customerName(originalPayment.getCustomerName())
                .transactionDate(LocalDateTime.now())
                .build();
    }

    /**
     * Update original payment status after refund
     */
    private void updateOriginalPaymentStatus(Payment originalPayment, BigDecimal refundAmount) {
        boolean isFullRefund = refundAmount.compareTo(originalPayment.getAmount()) == 0;

        originalPayment.setStatus(isFullRefund
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED);

        paymentRepository.save(originalPayment);
    }

    /**
     * Get payment entity with authorization check
     */
    private Payment getPaymentEntity(UUID paymentId, UUID merchantId)throws ResourceNotFoundException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new PaymentException("Access denied", "UNAUTHORIZED");
        }

        return payment;
    }

    // =====================================================
    // Helper: Convert amount to smallest unit
    // =====================================================
    private long convertToSmallestUnit(double amount, String currency) {
        // Zero-decimal currencies (no conversion needed)
        if (currency.equalsIgnoreCase("JPY") || currency.equalsIgnoreCase("KRW")) {
            return (long) amount;
        }
        // All others: multiply by 100 (USD→cents, NGN→kobo, EUR→cents)
        return (long) (amount * 100);
    }

}
