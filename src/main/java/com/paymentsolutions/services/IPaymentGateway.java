package com.paymentsolutions.services;



import java.math.BigDecimal;

/**
 * Interface for payment gateway implementations.
 * Provides abstraction for different payment providers (Stripe, PayPal, etc.)
 */
public interface IPaymentGateway {

    /**
     * Process a payment transaction
     *
     * @param amount Amount to charge
     * @param currency Currency code (USD, EUR, etc.)
     * @param paymentMethodToken Token representing payment method
     * @return Gateway transaction reference/ID
     * @throws PaymentGatewayException if payment processing fails
     */
    String processPayment(BigDecimal amount, String currency, String paymentMethodToken);

    /**
     * Refund a completed payment
     *
     * @param transactionId Original transaction ID from gateway
     * @param amount Amount to refund (null for full refund)
     * @return Refund transaction reference/ID
     * @throws PaymentGatewayException if refund fails
     */
    String refundPayment(String transactionId, BigDecimal amount);

    /**
     * Check status of a transaction
     *
     * @param transactionId Transaction ID to check
     * @return Transaction status
     * @throws PaymentGatewayException if status check fails
     */
    String checkTransactionStatus(String transactionId);

    /**
     * Get the name of this payment gateway
     *
     * @return Gateway name (e.g., "STRIPE", "PAYPAL")
     */
    String getGatewayName();
}
