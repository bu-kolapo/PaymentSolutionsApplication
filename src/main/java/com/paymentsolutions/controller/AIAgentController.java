package com.paymentsolutions.controller;


import com.paymentsolutions.dto.request.AIQueryRequest;
import com.paymentsolutions.dto.response.AIResponse;
import com.paymentsolutions.model.User;
import com.paymentsolutions.services.AIAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "AI-powered assistant endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
@Slf4j
public class AIAgentController {

    private final AIAgentService aiAgentService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant")
    public ResponseEntity<AIResponse> chat(
            @Valid @RequestBody AIQueryRequest request,
            @AuthenticationPrincipal User user) {
        AIResponse response = aiAgentService.processQuery(request, user.getMerchantId());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/ai/insights?period=7
     * 📊 Get AI-generated insights about your performance
     */
    @GetMapping("/insights")
    @Operation(summary = "Generate AI insights for merchant")
    public ResponseEntity<AIResponse> getInsights(
            @RequestParam(defaultValue = "30") int period,
            @AuthenticationPrincipal com.paymentsolutions.model.User user
    ) {
        log.info("📊 Generating AI insights for merchant: {} (period: {} days)",
                user.getMerchantId(), period);

        try {
            AIResponse response = aiAgentService.generateInsights(user.getMerchantId(), period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error generating insights:", e);
            return ResponseEntity.status(500)
                    .body(AIResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * GET /api/v1/ai/recommendations
     * 💡 Get AI recommendations to improve your payments
     */
    @GetMapping("/recommendations")
    @Operation(summary = "Get AI recommendations")
    public ResponseEntity<List<String>> getRecommendations(
            @AuthenticationPrincipal com.paymentsolutions.model.User user
    ) {
        log.info("💡 Getting AI recommendations for merchant: {}", user.getMerchantId());

        try {
            List<String> recommendations = aiAgentService.getRecommendations(user.getMerchantId());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("❌ Error getting recommendations:", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/v1/ai/patterns
     * 📈 Analyze transaction patterns and trends
     */
    @GetMapping("/patterns")
    @Operation(summary = "Analyze transaction patterns")
    public ResponseEntity<AIResponse> analyzePatterns(
            @AuthenticationPrincipal com.paymentsolutions.model.User user
    ) {
        log.info("📈 Analyzing patterns for merchant: {}", user.getMerchantId());

        try {
            AIResponse response = aiAgentService.analyzePatterns(user.getMerchantId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error analyzing patterns:", e);
            return ResponseEntity.status(500)
                    .body(AIResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }
}
