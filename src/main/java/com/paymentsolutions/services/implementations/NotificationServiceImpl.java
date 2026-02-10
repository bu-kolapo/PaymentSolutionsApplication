package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.EmailRequest;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.services.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation of INotificationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    @Override
    @Async
    public void sendPaymentConfirmation(Payment payment) {
        log.info("Sending payment confirmation for: {}", payment.getId());
        // Implementation: Send email/SMS notification
        // This would integrate with email service (SendGrid, AWS SES, etc.)
    }

    @Override
    @Async
    public void sendPaymentFailureNotification(Payment payment) {
        log.info("Sending payment failure notification for: {}", payment.getId());
        // Implementation: Send failure notification
    }

    @Override
    @Async
    public void sendRefundConfirmation(Payment refund) {
        log.info("Sending refund confirmation for: {}", refund.getId());
        // Implementation: Send refund notification
    }

    @Override
    @Async
    public void sendFraudAlert(Payment payment) {
        log.warn("Sending fraud alert for payment: {}", payment.getId());
        // Implementation: Send fraud alert to merchant
    }

    @Override
    @Async
    public void sendEmail(EmailRequest request) {
        log.info("Sending email to: {}", request.getTo());
        // Implementation: Send custom email
    }

    @Override
    @Async
    public void sendSms(String phoneNumber, String message) {
        log.info("Sending SMS to: {}", phoneNumber);
        // Implementation: Send SMS via Twilio or similar
    }
}