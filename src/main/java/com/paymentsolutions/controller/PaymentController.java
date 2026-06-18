package com.paymentsolutions.controller;


import com.paymentsolutions.dto.request.PaymentRequest;
import com.paymentsolutions.dto.request.ProcessPaymentRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.services.IPaymentOrchestrator;
import com.paymentsolutions.services.IPaymentRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for payment operations.
 * Uses IPaymentService interface for loose coupling.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:4003")
public class PaymentController {

        private final IPaymentRequestService paymentRequestService;
        private final IPaymentOrchestrator paymentOrchestrator;

        /**
         * MERCHANT ENDPOINT
         * Merchant creates payment request
         * Returns payment link for customer
         */
        @PostMapping("/requests")
        public ResponseEntity<PaymentResponse> createPaymentRequest(
                @Valid @RequestBody PaymentRequest request,
                @RequestHeader("X-Merchant-Id") UUID merchantId // Get from JWT in real impl
        ) {
            log.info("🎫 Merchant {} creating payment request", merchantId);

            PaymentResponse response = paymentRequestService.createPaymentRequest(request, merchantId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * PUBLIC ENDPOINT (No auth required)
         * Customer pays for existing payment request
         * This is called from checkout page when customer submits payment
         */
        @PostMapping("/{paymentReference}/pay")
        public ResponseEntity<PaymentResponse> processCustomerPayment(
                @PathVariable String paymentReference,
                @Valid @RequestBody ProcessPaymentRequest request
        ) {
            log.info("💳 Customer paying for: {}", paymentReference);

            PaymentResponse response = paymentOrchestrator.processCustomerPayment(paymentReference, request);

            return ResponseEntity.ok(response);
        }

        /**
         * PUBLIC ENDPOINT
         * Get payment request details
         * Used by checkout page to display payment info
         */
        @GetMapping("/requests/{paymentReference}")
        public ResponseEntity<PaymentResponse> getPaymentRequest(
                @PathVariable String paymentReference
        ) {
            PaymentResponse response = paymentRequestService.getPaymentRequest(paymentReference);
            return ResponseEntity.ok(response);
        }

        /**
         * MERCHANT ENDPOINT
         * Cancel pending payment request
         */
        @PostMapping("/requests/{paymentReference}/cancel")
        public ResponseEntity<PaymentResponse> cancelPaymentRequest(
                @PathVariable String paymentReference,
                @RequestHeader("X-Merchant-Id") UUID merchantId
        ) {
            PaymentResponse response = paymentRequestService.cancelPaymentRequest(paymentReference, merchantId);
            return ResponseEntity.ok(response);
        }

        /**
         * MERCHANT ENDPOINT
         * Reverse/refund completed payment
         */
        @PostMapping("/{paymentId}/reverse")
        public ResponseEntity<PaymentResponse> reversePayment(
                @PathVariable UUID paymentId,
                @RequestParam String reason,
                @RequestHeader("X-Merchant-Id") UUID merchantId
        ) {
            log.info("🔄 Merchant {} reversing payment {}", merchantId, paymentId);

            PaymentResponse response = paymentOrchestrator.reversePayment(paymentId, reason);

            return ResponseEntity.ok(response);
        }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-Merchant-Id") UUID merchantId
    ) {
        log.info("📋 GET /api/v1/payments - merchant: {}, status: {}", merchantId, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentResponse> payments = paymentRequestService.getPayments(merchantId, status, pageable);

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID paymentId,
            @RequestHeader("X-Merchant-Id") UUID merchantId
    ) {
        log.info("🔍 Fetching payment {}", paymentId);

        PaymentResponse response = paymentRequestService.getPaymentById(paymentId, merchantId);

        return ResponseEntity.ok(response);
    }
}
