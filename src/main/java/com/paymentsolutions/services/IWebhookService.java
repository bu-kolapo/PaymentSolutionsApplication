package com.paymentsolutions.services;

import java.util.UUID;

public interface IWebhookService {

    /**
     * Send payment success webhook
     */
    void sendPaymentSuccessWebhook(UUID paymentId);

    /**
     * Send payment failed webhook
     */
    void sendPaymentFailedWebhook(UUID paymentId);

    /**
     * Retry failed webhooks
     */
    void retryFailedWebhooks();
}
