package com.paymentsolutions.services.implementations;


import com.paymentsolutions.exception.PaymentGatewayException;
import com.paymentsolutions.services.IPaymentGateway;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stripe implementation of IPaymentGateway.
 */
@Component
@Slf4j
public class StripePaymentGateway implements IPaymentGateway {

    @Value("${payment.gateway.stripe.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        log.info("Stripe payment gateway initialized");
    }

    @Override
    public String processPayment(BigDecimal amount, String currency, String paymentMethodToken) {
        log.info("Processing Stripe payment: amount={}, currency={}", amount, currency);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(convertToMinorUnits(amount, currency))
                    .setCurrency(currency.toLowerCase())
                    .setPaymentMethod(paymentMethodToken)
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            if ("succeeded".equals(intent.getStatus())) {
                log.info("Stripe payment successful: {}", intent.getId());
                return intent.getId();
            } else {
                throw new PaymentGatewayException(
                        "Payment not successful: " + intent.getStatus(),
                        "STRIPE_PAYMENT_FAILED"
                );
            }

        } catch (StripeException e) {
            log.error("Stripe payment error: {}", e.getMessage(), e);
            throw new PaymentGatewayException(
                    "Stripe payment failed: " + e.getMessage(),
                    e.getCode(),
                    e
            );
        }
    }

    @Override
    public String refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing Stripe refund: transactionId={}, amount={}", transactionId, amount);

        try {
            RefundCreateParams.Builder builder = RefundCreateParams.builder()
                    .setPaymentIntent(transactionId);

            if (amount != null) {
                builder.setAmount(convertToMinorUnits(amount, "USD")); // Currency should be from original payment
            }

            RefundCreateParams params = builder.build();
            Refund refund = Refund.create(params);

            log.info("Stripe refund successful: {}", refund.getId());
            return refund.getId();

        } catch (StripeException e) {
            log.error("Stripe refund error: {}", e.getMessage(), e);
            throw new PaymentGatewayException(
                    "Stripe refund failed: " + e.getMessage(),
                    e.getCode(),
                    e
            );
        }
    }

    @Override
    public String checkTransactionStatus(String transactionId) {
        log.info("Checking Stripe transaction status: {}", transactionId);

        try {
            PaymentIntent intent = PaymentIntent.retrieve(transactionId);
            return intent.getStatus();
        } catch (StripeException e) {
            log.error("Stripe status check error: {}", e.getMessage(), e);
            throw new PaymentGatewayException(
                    "Failed to check transaction status: " + e.getMessage(),
                    e.getCode(),
                    e
            );
        }
    }

    @Override
    public String getGatewayName() {
        return "STRIPE";
    }

    /**
     * Convert amount to minor units (cents)
     *
     * @param amount Amount in major units
     * @param currency Currency code
     * @return Amount in minor units
     */
    private Long convertToMinorUnits(BigDecimal amount, String currency) {
        // Most currencies use 2 decimal places (cents)
        // Some currencies like JPY use 0 decimal places
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
}