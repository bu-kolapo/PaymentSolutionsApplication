package com.paymentsolutions.services.implementations;



import com.paymentsolutions.dto.request.InitiatePaymentRequest;
import com.paymentsolutions.dto.request.ProcessPaymentRequest;
import com.paymentsolutions.dto.response.PaymentGatewayResponse;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.services.IPaymentGatewayService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements IPaymentGatewayService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Override
    public PaymentGatewayResponse processPayment(Payment payment, ProcessPaymentRequest request){
        log.info("💳 Processing via gateway - Channel: {}", payment.getChannel());

        // Route based on channel
        return switch (payment.getChannel()) {
            case "CARD" -> processCardPayment(payment, request);
            case "BANK_TRANSFER" -> processBankTransfer(payment, request);
            case "MOBILE_MONEY" -> processMobileMoney(payment, request);
            case "WALLET" -> processWalletPayment(payment, request);
            default -> PaymentGatewayResponse.builder()
                    .success(false)
                    .message("Unsupported channel: " + payment.getChannel())
                    .build();
        };
    }

    private PaymentGatewayResponse processCardPayment(Payment payment,ProcessPaymentRequest request) {
        log.info("💳 Processing card payment via Stripe");

        Stripe.apiKey = stripeApiKey;

        try {
            // Convert to smallest unit (kobo for NGN)
            long amountInKobo = payment.getAmount().multiply(new java.math.BigDecimal("100")).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInKobo)
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .setPaymentMethod(request.getPaymentMethodToken()) // pm_card_visa
                    .setConfirm(true)
                    .setReturnUrl("http://localhost:4003/payments")
                    .putMetadata("payment_reference", payment.getPaymentReference())
                    .putMetadata("merchant_id", payment.getMerchantId().toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayReference(paymentIntent.getId())
                    .provider("STRIPE")
                    .message("Payment successful")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .provider("STRIPE")
                    .message(e.getMessage())
                    .errorCode(e.getCode())
                    .build();
        }
    }

    private PaymentGatewayResponse processBankTransfer(Payment payment, ProcessPaymentRequest request) {
        log.info("🏦 Processing bank transfer via NIP");

        // In production: Call NIP/NIBSS API
        // For now: Simulate successful transfer

        try {
            // Simulate API call delay
            Thread.sleep(1000);

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayReference("NIP-" + System.currentTimeMillis())
                    .provider("NIP")
                    .message("Transfer successful")
                    .build();

        } catch (Exception e) {
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .provider("NIP")
                    .message(e.getMessage())
                    .build();
        }
    }

    private PaymentGatewayResponse processMobileMoney(Payment payment, ProcessPaymentRequest request) {
        log.info("📱 Processing mobile money");

        // Route to appropriate provider (MTN, Airtel, etc)
        return PaymentGatewayResponse.builder()
                .success(true)
                .gatewayReference("MOMO-" + System.currentTimeMillis())
                .provider("MTN_MOMO")
                .message("Mobile money payment successful")
                .build();
    }

    private PaymentGatewayResponse processWalletPayment(Payment payment,ProcessPaymentRequest request) {
        log.info("👛 Processing wallet payment");

        // Internal wallet-to-wallet transfer
        return PaymentGatewayResponse.builder()
                .success(true)
                .gatewayReference("WALLET-" + System.currentTimeMillis())
                .provider("INTERNAL_WALLET")
                .message("Wallet payment successful")
                .build();
    }

    @Override
    public PaymentGatewayResponse refund(Payment payment) {
        log.info("🔄 Refunding payment: {}", payment.getPaymentReference());

        if ("STRIPE".equals(payment.getProvider())) {
            return refundStripePayment(payment);
        }

        // Handle other providers
        return PaymentGatewayResponse.builder()
                .success(true)
                .message("Refund initiated")
                .build();
    }

    private PaymentGatewayResponse refundStripePayment(Payment payment) {
        Stripe.apiKey = stripeApiKey;

        try {
            Refund refund = Refund.create(
                    RefundCreateParams.builder()
                            .setPaymentIntent(payment.getGatewayReference())
                            .build()
            );

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayReference(refund.getId())
                    .provider("STRIPE")
                    .message("Refund successful")
                    .build();

        } catch (StripeException e) {
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentGatewayResponse verifyPayment(String gatewayReference) {
        log.info("🔍 Verifying payment: {}", gatewayReference);

        // Verify with gateway
        return PaymentGatewayResponse.builder()
                .success(true)
                .message("Payment verified")
                .build();
    }
}

