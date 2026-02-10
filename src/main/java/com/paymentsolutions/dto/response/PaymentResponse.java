package com.paymentsolutions.dto.response;

import com.paymentsolutions.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {


    private UUID id;

    private UUID merchantId;

    private UUID customerId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private String paymentMethod;

    private String transactionReference;

    private String gatewayReference;

    private String description;

    private String customerEmail;

    private String customerName;

    private LocalDateTime transactionDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;





}
