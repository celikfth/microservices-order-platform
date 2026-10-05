package com.fatih.microservices.payment.processor;

public class PaymentResult {

    private final boolean successful;
    private final String transactionId;
    private final String failureReason;

    public PaymentResult(
            boolean successful,
            String transactionId,
            String failureReason) {

        this.successful = successful;
        this.transactionId = transactionId;
        this.failureReason = failureReason;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}