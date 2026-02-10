package com.paymentsolutions.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for AI agent interactions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    private String message;

    private String conversationId;

    private LocalDateTime timestamp;

    private List<SuggestedAction> suggestedActions;

    private Map<String, Object> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedAction {
        private String id;
        private String type;
        private String label;
        private String description;
        private Map<String, Object> parameters;
    }
}