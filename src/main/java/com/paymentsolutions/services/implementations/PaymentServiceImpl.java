package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.PaymentRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.model.PaymentStatus;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements IPaymentRequestService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.gateway.url:http://localhost:4003}")
    private String gatewayUrl;

    @Override
    @Transactional
    public PaymentResponse createPaymentRequest(PaymentRequest request, UUID merchantId) {
        log.info("🎫 Merchant {} creating payment request for {} {}",
                merchantId, request.getAmount(), request.getCurrency());

        // Generate unique payment reference
        String paymentReference = generateReference("PAY");

        // Generate idempotency key
        String idempotencyKey = merchantId + "_" + request.getCustomerEmail() + "_"
                + request.getAmount() + "_" + System.currentTimeMillis();

        // Check for duplicate
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new PaymentException("Duplicate payment request", "DUPLICATE_REQUEST");
        }

        // Create payment record in PENDING status
        Payment payment = Payment.builder()
                .paymentReference(paymentReference)
                .merchantId(merchantId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .narration(request.getDescription())
                .channel(request.getChannel() != null ? request.getChannel() : "CARD")
                .status(PaymentStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .webhookSent(false)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        // Generate payment link for customer
        String paymentLink = generatePaymentLink(paymentReference);

        log.info("✅ Payment request created: {}", paymentReference);

        return PaymentResponse.builder()
                .paymentReference(paymentReference)
                .paymentLink(paymentLink)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .customerEmail(payment.getCustomerEmail())
                .expiresAt(LocalDateTime.now().plusHours(24)) // Link expires in 24h
                .createdAt(payment.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentRequest(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentException("Payment request not found", "NOT_FOUND"));

        return PaymentResponse.builder()
                .paymentReference(payment.getPaymentReference())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .customerEmail(payment.getCustomerEmail())
                .customerName(payment.getCustomerName())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    @Override
    public String generatePaymentLink(String paymentReference) {
        return gatewayUrl + "/checkout/" + paymentReference;
    }

    @Override
    @Transactional
    public PaymentResponse cancelPaymentRequest(String paymentReference, UUID merchantId) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentException("Payment not found", "NOT_FOUND"));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new PaymentException("Unauthorized", "FORBIDDEN");
        }

        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new PaymentException("Can only cancel pending payments", "INVALID_STATUS");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(UUID merchantId, String status, Pageable pageable) {
        log.info("📋 Fetching payments for merchant: {}, status: {}", merchantId, status);

        Page<Payment> payments;

        if (status != null && !status.isEmpty()) {
            // Filter by status
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            payments = paymentRepository.findByMerchantIdAndStatus(merchantId, paymentStatus, pageable);
        } else {
            // Get all payments
            payments = paymentRepository.findByMerchantId(merchantId, pageable);
        }

        log.info("✅ Found {} payments", payments.getTotalElements());

        return payments.map(this::mapToResponse);
    }

    @Override
    public PaymentResponse getPaymentById(UUID paymentId, UUID merchantId) {

        var payment = paymentRepository
                .findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        // 🔐 Security check (VERY IMPORTANT)
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("Unauthorized access to payment");
        }

        return mapToResponse(payment);
    }

    private String generateReference(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .merchantId(payment.getMerchantId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .customerEmail(payment.getCustomerEmail())
                .customerName(payment.getCustomerName())
                .transactionReference(payment.getTransactionReference())
                .gatewayReference(payment.getGatewayReference())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}