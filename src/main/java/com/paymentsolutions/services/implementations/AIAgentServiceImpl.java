package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.AIQueryRequest;
import com.paymentsolutions.dto.response.AIResponse;
import com.paymentsolutions.repository.PaymentRepository;
import com.paymentsolutions.services.AIAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of AIAgentService.
 * Integrates with Anthropic Claude API for AI-powered features.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIAgentServiceImpl implements AIAgentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.claude.api-key}")
    private String claudeApiKey;

    @Value("${ai.claude.model:claude-opus-4-5-20251101}")
    private String claudeModel;

    @Value("${ai.claude.max-tokens:1024}")
    private int maxTokens;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    // =====================================================
    // 1. processQuery — Chat with AI assistant Chat with AI (general Q&A about payments)
    // =====================================================

    @Override
    public AIResponse processQuery(AIQueryRequest request, UUID merchantId) {
        log.info("Processing AI query for merchant: {}", merchantId);

        // Build a context-aware prompt
        String systemPrompt = """
                You are a helpful payment analytics assistant for a merchant dashboard.
                You help merchants understand their payment data, trends, and provide actionable insights.
                Be concise, professional, and data-driven in your responses.
                If asked about specific data you don't have, acknowledge that and provide general advice.
                """;

        String userMessage = buildUserMessage(request, merchantId);

        String aiReply = callClaudeAPI(systemPrompt, userMessage);

        return AIResponse.builder()
                .message(aiReply)
                .conversationId(request.getConversationId() != null
                        ? request.getConversationId()
                        : UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // =====================================================
    // 2. generateInsights — AI-generated data insights,AI analytics (performance over N days)
    // =====================================================
    @Override
    public AIResponse generateInsights(UUID merchantId, int period) {
        log.info("Generating AI insights for merchant: {} over {} days", merchantId, period);

        // Fetch real metrics from database
        LocalDateTime since = LocalDateTime.now().minusDays(period);

        long totalTransactions = paymentRepository.countByMerchantIdSince(merchantId, since);
        BigDecimal totalRevenue = paymentRepository
                .getTotalRevenueForMerchantSince(merchantId, since)
                .orElse(BigDecimal.ZERO);
        long failedTransactions = paymentRepository
                .countByMerchantIdAndStatusSince(merchantId, "FAILED", since);

        double successRate = totalTransactions > 0
                ? ((double)(totalTransactions - failedTransactions) / totalTransactions) * 100
                : 0.0;

        // Build analytics prompt with real data
        String systemPrompt = """
                You are a payment analytics expert. Analyze the provided metrics and generate
                clear, actionable business insights. Focus on trends, anomalies, and recommendations.
                Format your response in clear sections: Summary, Key Findings, and Recommendations.
                """;

        String userMessage = String.format("""
                Analyze these payment metrics for the last %d days and provide insights:
                
                - Total Transactions: %d
                - Total Revenue: $%.2f
                - Failed Transactions: %d
                - Success Rate: %.1f%%
                
                Please provide:
                1. A brief summary of performance
                2. Key findings (positive and negative)
                3. Actionable recommendations to improve
                """,
                period,
                totalTransactions,
                totalRevenue,
                failedTransactions,
                successRate
        );

        String insights = callClaudeAPI(systemPrompt, userMessage);

        return AIResponse.builder()
                .message(insights)
                .conversationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                        "totalTransactions", totalTransactions,
                        "totalRevenue", totalRevenue,
                        "failedTransactions", failedTransactions,
                        "successRate", String.format("%.1f%%", successRate),
                        "period", period + " days"
                ))
                .build();
    }

    // =====================================================
    // 3. getRecommendations — Personalized recommendations, 5 actionable recommendations
    // =====================================================
    @Override
    public List<String> getRecommendations(UUID merchantId) {
        log.info("Getting AI recommendations for merchant: {}", merchantId);

        // Fetch last 30 days metrics
        LocalDateTime since = LocalDateTime.now().minusDays(30);

        long totalTransactions = paymentRepository.countByMerchantIdSince(merchantId, since);
        long failedTransactions = paymentRepository
                .countByMerchantIdAndStatusSince(merchantId, "FAILED", since);

        double failureRate = totalTransactions > 0
                ? ((double) failedTransactions / totalTransactions) * 100
                : 0.0;

        String systemPrompt = """
                You are a payment optimization expert. Based on the metrics provided,
                return EXACTLY 5 short, actionable recommendations.
                Format: Return only a numbered list (1. 2. 3. 4. 5.), one recommendation per line.
                Each recommendation should be one sentence, practical and specific.
                Do not include any other text, headers, or explanation.
                """;

        String userMessage = String.format("""
                Generate 5 recommendations based on these metrics:
                - Total Transactions (30 days): %d
                - Failed Transactions: %d
                - Failure Rate: %.1f%%
                """,
                totalTransactions, failedTransactions, failureRate
        );

        String rawResponse = callClaudeAPI(systemPrompt, userMessage);

        return parseRecommendations(rawResponse);
    }

    // =====================================================
    // 4. analyzePatterns — Detect transaction ,patterns Detect transaction trends (7-day vs 30-day)
    // =====================================================
    @Override
    public AIResponse analyzePatterns(UUID merchantId) {
        log.info("Analyzing transaction patterns for merchant: {}", merchantId);

        // Fetch metrics for last 7 and 30 days to detect trends
        LocalDateTime last7Days  = LocalDateTime.now().minusDays(7);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        long txLast7  = paymentRepository.countByMerchantIdSince(merchantId, last7Days);
        long txLast30 = paymentRepository.countByMerchantIdSince(merchantId, last30Days);

        BigDecimal revLast7  = paymentRepository
                .getTotalRevenueForMerchantSince(merchantId, last7Days)
                .orElse(BigDecimal.ZERO);
        BigDecimal revLast30 = paymentRepository
                .getTotalRevenueForMerchantSince(merchantId, last30Days)
                .orElse(BigDecimal.ZERO);

        // Weekly averages for trend analysis
        double weeklyAvgLast30 = txLast30 / 4.0;
        String trend = txLast7 > weeklyAvgLast30 ? "INCREASING" :
                txLast7 < weeklyAvgLast30 ? "DECREASING" : "STABLE";

        String systemPrompt = """
                You are a transaction pattern analyst. Analyze the provided data
                and identify meaningful patterns, trends, and anomalies.
                Structure your response as: Trend Analysis, Pattern Observations, Risk Signals, Next Steps.
                """;

        String userMessage = String.format("""
                Analyze these transaction patterns:
                
                Last 7 days:
                - Transactions: %d
                - Revenue: $%.2f
                
                Last 30 days:
                - Transactions: %d
                - Revenue: $%.2f
                - Weekly Average: %.1f transactions/week
                
                Current Trend: %s
                
                Provide a detailed pattern analysis.
                """,
                txLast7, revLast7,
                txLast30, revLast30,
                weeklyAvgLast30,
                trend
        );

        String analysis = callClaudeAPI(systemPrompt, userMessage);

        return AIResponse.builder()
                .message(analysis)
                .conversationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                        "transactionsLast7Days",  txLast7,
                        "transactionsLast30Days", txLast30,
                        "revenueLast7Days",        revLast7,
                        "revenueLast30Days",       revLast30,
                        "trend",                   trend
                ))
                .build();
    }

    // =====================================================
    // Private Helpers
    // =====================================================

    /**
     * Calls the Claude API with a system prompt and user message.
     */
    private String callClaudeAPI(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", claudeApiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> body = new HashMap<>();
            body.put("model", claudeModel);
            body.put("max_tokens", maxTokens);
            body.put("system", systemPrompt);
            body.put("messages", List.of(
                    Map.of("role", "user", "content", userMessage)
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    CLAUDE_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Parse response: response.content[0].text
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> content =
                        (List<Map<String, Object>>) response.getBody().get("content");

                if (content != null && !content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }

            log.warn("Empty response from Claude API");
            return "I was unable to generate a response at this time. Please try again.";

        } catch (Exception e) {
            log.error("Claude API call failed: {}", e.getMessage(), e);
            return "AI service is temporarily unavailable. Please try again later.";
        }
    }

    /**
     * Builds a context-enriched user message for the chat endpoint.
     */
    private String buildUserMessage(AIQueryRequest request, UUID merchantId) {
        StringBuilder sb = new StringBuilder();

        if (request.getContext() != null && !request.getContext().isEmpty()) {
            sb.append("Context: ").append(request.getContext()).append("\n\n");
        }

        sb.append("Question: ").append(request.getMessage());
        return sb.toString();
    }

    /**
     * Parses Claude's numbered list response into a clean List<String>.
     */
    private List<String> parseRecommendations(String rawResponse) {
        List<String> recommendations = new ArrayList<>();

        if (rawResponse == null || rawResponse.isBlank()) {
            recommendations.add("Monitor your transaction success rate regularly.");
            recommendations.add("Implement fraud detection rules for large transactions.");
            recommendations.add("Send payment confirmation emails to customers.");
            recommendations.add("Review failed payment reasons weekly.");
            recommendations.add("Consider offering multiple payment methods.");
            return recommendations;
        }

        // Split by newlines and clean up numbered prefixes like "1. ", "2. "
        String[] lines = rawResponse.split("\n");
        for (String line : lines) {
            String cleaned = line.trim()
                    .replaceAll("^\\d+\\.\\s*", "")   // remove "1. "
                    .replaceAll("^[-*]\\s*", "")       // remove "- " or "* "
                    .trim();

            if (!cleaned.isEmpty()) {
                recommendations.add(cleaned);
            }
        }

        // Always return exactly 5
        while (recommendations.size() < 5) {
            recommendations.add("Review your payment processing performance regularly.");
        }

        return recommendations.subList(0, Math.min(5, recommendations.size()));
    }
}