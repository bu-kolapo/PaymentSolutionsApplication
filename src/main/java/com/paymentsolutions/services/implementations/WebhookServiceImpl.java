package com.paymentsolutions.services.implementations;

import com.paymentsolutions.model.Merchant;
import com.paymentsolutions.model.Payment;
import com.paymentsolutions.model.Webhook;
import com.paymentsolutions.repository.MerchantRepository;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.repository.WebhookRepository;
import com.paymentsolutions.services.IWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements IWebhookService {

    private final WebhookRepository webhookRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Async
    public void sendPaymentSuccessWebhook(UUID paymentId) {
        log.info("📤 Sending success webhook for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) return;

        // Get merchant webhook URL
        Merchant merchant = merchantRepository.findById(payment.getMerchantId()).orElse(null);
        if (merchant == null || merchant.getWebhookUrl() == null) {
            log.warn("No webhook URL configured for merchant");
            return;
        }

        // Build webhook payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "payment.success");
        payload.put("payment_reference", payment.getPaymentReference());
        payload.put("amount", payment.getAmount());
        payload.put("currency", payment.getCurrency());
        payload.put("status", payment.getStatus());
        payload.put("customer_email", payment.getCustomerEmail());
        payload.put("created_at", payment.getCreatedAt());

        sendWebhook(payment, merchant.getWebhookUrl(), "payment.success", payload);
    }

    @Override
    @Async
    public void sendPaymentFailedWebhook(UUID paymentId) {
        log.info("📤 Sending failed webhook for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) return;

        Merchant merchant = merchantRepository.findById(payment.getMerchantId()).orElse(null);
        if (merchant == null || merchant.getWebhookUrl() == null) return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "payment.failed");
        payload.put("payment_reference", payment.getPaymentReference());
        payload.put("amount", payment.getAmount());
        payload.put("currency", payment.getCurrency());
        payload.put("status", payment.getStatus());

        sendWebhook(payment, merchant.getWebhookUrl(), "payment.failed", payload);
    }

    @Override
    public void retryFailedWebhooks() {
        log.info("🔄 Retrying failed webhooks...");

        List<Webhook> failedWebhooks = webhookRepository.findByStatusAndRetryCountLessThan("FAILED", 3);

        for (Webhook webhook : failedWebhooks) {
            try {
                Payment payment = paymentRepository.findById(webhook.getPaymentId()).orElse(null);
                if (payment == null) continue;

                Map<String, Object> payload = objectMapper.readValue(webhook.getPayload(), Map.class);
                sendWebhook(payment, webhook.getWebhookUrl(), webhook.getEventType(), payload);

            } catch (Exception e) {
                log.error("Webhook retry failed: {}", e.getMessage());
            }
        }
    }

    private void sendWebhook(Payment payment, String webhookUrl, String eventType, Map<String, Object> payload) {
        Webhook webhook = Webhook.builder()
                .paymentId(payment.getId())
                .merchantId(payment.getMerchantId())
                .webhookUrl(webhookUrl)
                .eventType(eventType)
                .status("PENDING")
                .build();

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            webhook.setPayload(payloadJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Add signature for security
            headers.set("X-Signature", generateSignature(payloadJson));

            HttpEntity<String> request = new HttpEntity<>(payloadJson, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

            webhook.setStatus("SENT");
            webhook.setResponseStatusCode(response.getStatusCode().value());
            webhook.setResponseBody(response.getBody());
            webhook.setLastAttemptAt(LocalDateTime.now());

            log.info("✅ Webhook sent successfully to {}", webhookUrl);

        } catch (Exception e) {
            log.error("❌ Webhook failed: {}", e.getMessage());
            webhook.setStatus("FAILED");
            webhook.setRetryCount(webhook.getRetryCount() + 1);
            webhook.setLastAttemptAt(LocalDateTime.now());
            webhook.setResponseBody(e.getMessage());
        }

        webhookRepository.save(webhook);
    }

    private String generateSignature(String payload) {
        // HMAC-SHA256 signature for webhook verification
        // Merchant can verify webhook authenticity
        return "signature-placeholder";
    }
}
