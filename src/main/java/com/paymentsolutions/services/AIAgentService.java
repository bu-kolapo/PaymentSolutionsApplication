package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.AIQueryRequest;
import com.paymentsolutions.dto.response.AIResponse;

import java.util.UUID;

/**
 * Service interface for AI-powered assistant operations.
 */
public interface AIAgentService {

    /**
     * Process a natural language query using AI
     *
     * @param request Query details
     * @param merchantId UUID of the merchant
     * @return AI-generated response
     */
    AIResponse processQuery(AIQueryRequest request, UUID merchantId);

    /**
     * Generate insights from payment data
     *
     * @param merchantId UUID of the merchant
     * @param period Analysis period in days
     * @return AI-generated insights
     */
    AIResponse generateInsights(UUID merchantId, int period);

    /**
     * Get recommendations for merchant
     *
     * @param merchantId UUID of the merchant
     * @return List of AI recommendations
     */
    java.util.List<String> getRecommendations(UUID merchantId);

    /**
     * Analyze transaction patterns
     *
     * @param merchantId UUID of the merchant
     * @return Pattern analysis response
     */
    AIResponse analyzePatterns(UUID merchantId);
}
