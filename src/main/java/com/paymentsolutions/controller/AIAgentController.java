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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "AI-powered assistant endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
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
}
