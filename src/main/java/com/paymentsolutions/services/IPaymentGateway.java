package com.paymentsolutions.services;



import com.paymentsolutions.dto.request.ProcessPaymentRequest;
import com.paymentsolutions.dto.response.PaymentGatewayResponse;
import com.paymentsolutions.model.Payment;

import java.math.BigDecimal;

/**
 * Interface for payment gateway implementations.
 * Provides abstraction for different payment providers (Stripe, PayPal, etc.)
 */
public interface IPaymentGateway {

    PaymentGatewayResponse processPayment(Payment payment, ProcessPaymentRequest request);
    PaymentGatewayResponse refund(Payment payment);
    PaymentGatewayResponse verifyPayment(String gatewayReference);
}
