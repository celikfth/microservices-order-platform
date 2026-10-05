package com.fatih.microservices.payment.processor;

import com.fatih.microservices.payment.model.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PayPalPaymentProcessor
        implements PaymentProcessor {

    @Override
    public PaymentResult process(Payment payment) {

        System.out.println(
                "Processing payment with PayPal..."
        );

        String transactionId =
                "PAYPAL-" + UUID.randomUUID();

        return new PaymentResult(
                true,
                transactionId,
                null
        );
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.PAYPAL;
    }
}