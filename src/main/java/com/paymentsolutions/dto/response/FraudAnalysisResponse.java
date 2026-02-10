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
@AllArgsConstructor
@NoArgsConstructor
public class FraudAnalysisResponse {

      private UUID paymentId;

      private double riskScore;

      private String riskLevel;

      private List<String> flags;

      private Map<String, Object> details;

      private LocalDateTime analyzedAt;

      private String recommendation;
}
