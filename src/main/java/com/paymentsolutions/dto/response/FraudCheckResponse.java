package com.paymentsolutions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckResponse {
      private int riskScore; // 0-100
      private String decision; // ALLOW, BLOCK, REVIEW
      private String reason;
      private String[] rulesTriggered;
}