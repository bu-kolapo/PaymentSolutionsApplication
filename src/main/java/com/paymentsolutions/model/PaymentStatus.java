package com.paymentsolutions.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    FRAUD_DETECTED,
    CANCELLED
}