package com.fatih.microservices.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentCompletedEvent {

    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;

    public PaymentCompletedEvent() {
    }

    public PaymentCompletedEvent(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            String transactionId,
            LocalDateTime paidAt) {

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.paidAt = paidAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}