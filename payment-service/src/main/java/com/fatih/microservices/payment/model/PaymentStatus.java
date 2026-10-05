package com.fatih.microservices.payment.model;

public enum PaymentStatus {

    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED
}