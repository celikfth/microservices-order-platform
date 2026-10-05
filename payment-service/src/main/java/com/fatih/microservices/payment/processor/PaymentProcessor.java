package com.fatih.microservices.payment.processor;

import com.fatih.microservices.payment.model.Payment;

public interface PaymentProcessor {

    PaymentResult process(Payment payment);

    PaymentMethod getPaymentMethod();
}