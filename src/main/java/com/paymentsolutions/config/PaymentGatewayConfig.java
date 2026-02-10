package com.paymentsolutions.config;


import com.paymentsolutions.services.IPaymentGateway;
import com.paymentsolutions.services.implementations.StripePaymentGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for payment gateway beans.
 */
@Configuration
public class PaymentGatewayConfig {

    /**
     * Primary payment gateway bean.
     * Can be switched to different implementations easily.
     */
    @Bean
    public IPaymentGateway primaryPaymentGateway(StripePaymentGateway stripeGateway) {
        return stripeGateway;
    }
}