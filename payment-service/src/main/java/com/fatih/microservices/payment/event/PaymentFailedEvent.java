package com.fatih.microservices.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentFailedEvent {

    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime failedAt;

    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            String reason,
            LocalDateTime failedAt) {

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.reason = reason;
        this.failedAt = failedAt;
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

    public String getReason() {
        return reason;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }
}