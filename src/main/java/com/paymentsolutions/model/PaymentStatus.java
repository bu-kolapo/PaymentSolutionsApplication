package com.paymentsolutions.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    FRAUD_DETECTED,
    CANCELLED
}