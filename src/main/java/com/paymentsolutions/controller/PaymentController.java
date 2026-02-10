package com.paymentsolutions.controller;


import com.paymentsolutions.dto.request.PaymentRequest;
import com.paymentsolutions.dto.request.RefundRequest;
import com.paymentsolutions.dto.response.PaymentResponse;
import com.paymentsolutions.exception.ResourceNotFoundException;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for payment operations.
 * Uses IPaymentService interface for loose coupling.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create new payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse response = paymentService.processPayment(request, user.getMerchantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal User user)throws ResourceNotFoundException {
        PaymentResponse response = paymentService.getPayment(paymentId, user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all payments")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(required = false) String status,
            Pageable pageable,
            @AuthenticationPrincipal User user) {
        Page<PaymentResponse> payments = paymentService.getPayments(
                user.getMerchantId(), status, pageable);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal User user) throws ResourceNotFoundException {
        PaymentResponse response = paymentService.refundPayment(
                paymentId, request, user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel payment")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal User user) throws ResourceNotFoundException {
        PaymentResponse response = paymentService.cancelPayment(paymentId, user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get payment statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User user) {
        Map<String, Object> stats = paymentService.getPaymentStatistics(user.getMerchantId(), days);
        return ResponseEntity.ok(stats);
    }
}