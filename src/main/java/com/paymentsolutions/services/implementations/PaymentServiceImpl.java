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
import java.util.UUID;

/**
 * Implementation of IPaymentService.
 * Handles all payment processing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final IFraudDetectionService fraudDetectionService;
    private final INotificationService notificationService;
    private final IPaymentGateway paymentGateway;

//    @Override
//    public PaymentResponse processPayment(PaymentRequest request, UUID merchantId) throws PaymentException {
//        log.info("Processing payment for merchant: {}, amount: {} {}",
//                merchantId, request.getAmount(), request.getCurrency());
//
//        // Validate request
//        validatePaymentRequest(request);
//
//        // Create payment entity
//        Payment payment = createPaymentEntity(request, merchantId);
//        payment = paymentRepository.save(payment);
//
//        try {
//            // Fraud detection check
//            if (fraudDetectionService.checkForFraud(payment)) {
//                payment.setStatus(PaymentStatus.FRAUD_DETECTED);
//                payment.setErrorMessage("Transaction flagged as potentially fraudulent");
//                paymentRepository.save(payment);
//                notificationService.sendFraudAlert(payment);
//                throw new PaymentException("Fraud detected", "FRAUD_DETECTED");
//            }
//
//            // Process with payment gateway
//            payment.setStatus(PaymentStatus.PROCESSING);
//            paymentRepository.save(payment);
//
//            String gatewayReference = stripeGateway.processPayment(
//                    payment.getAmount(),
//                    payment.getCurrency(),
//                    request.getPaymentMethodToken()
//            );
//
//            // Mark as completed
//            payment.markAsCompleted(gatewayReference);
//            payment = paymentRepository.save(payment);
//
//            // Post-processing
//            notificationService.sendPaymentConfirmation(payment);
//
//            log.info("Payment processed successfully: {}", payment.getId());
//            return mapToResponse(payment);
//
//        } catch (Exception e) {
//            log.error("Payment processing failed: {}", e.getMessage(), e);
//            payment.markAsFailed("GATEWAY_ERROR", e.getMessage());
//            paymentRepository.save(payment);
//            notificationService.sendPaymentFailureNotification(payment);
//            throw new PaymentException("Payment processing failed: " + e.getMessage(), "PROCESSING_ERROR");
//        }
//    }


    @Override
    public PaymentResponse processPayment(PaymentRequest request, UUID merchantId) throws PaymentException{
        log.info("Processing payment for merchant: {}, amount: {} {}",
                merchantId, request.getAmount(), request.getCurrency());

        // Create and save payment entity
        Payment payment = createPaymentEntity(request, merchantId);
        payment = paymentRepository.save(payment);

        try {
            // Fraud detection
            if (fraudDetectionService.checkForFraud(payment)) {
                handleFraudDetection(payment);
                throw new PaymentException("Fraud detected", "FRAUD_DETECTED");
            }

            // Process with gateway (using interface)
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            String gatewayReference = paymentGateway.processPayment(
                    payment.getAmount(),
                    payment.getCurrency(),
                    request.getPaymentMethodToken()
            );

            // Mark as completed
            payment.markAsCompleted(gatewayReference);
            payment = paymentRepository.save(payment);

            // Post-processing
            notificationService.sendPaymentConfirmation(payment);

            log.info("Payment processed successfully: {}", payment.getId());
            return mapToResponse(payment);

        } catch (Exception e) {
            handlePaymentFailure(payment, e);
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

}
