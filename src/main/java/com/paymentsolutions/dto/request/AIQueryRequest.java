package com.paymentsolutions.dto.request;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for AI agent queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String conversationId;

    private String context;
}
