package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.EmailRequest;
import com.paymentsolutions.model.Payment;

/**
 * Service interface for notification operations.
 * Handles email, SMS, and push notifications.
 */
public interface INotificationService {

    /**
     * Send payment confirmation email
     *
     * @param payment Payment details
     */
    void sendPaymentConfirmation(Payment payment);

    /**
     * Send payment failure notification
     *
     * @param payment Payment details
     */
    void sendPaymentFailureNotification(Payment payment);

    /**
     * Send refund confirmation
     *
     * @param refund Refund payment details
     */
    void sendRefundConfirmation(Payment refund);

    /**
     * Send fraud alert to merchant
     *
     * @param payment Payment flagged as fraudulent
     */
    void sendFraudAlert(Payment payment);

    /**
     * Send custom email
     *
     * @param request Email details (to, subject, body)
     */
    void sendEmail(EmailRequest request);

    /**
     * Send SMS notification
     *
     * @param phoneNumber Recipient phone number
     * @param message SMS message
     */
    void sendSms(String phoneNumber, String message);
}