package com.fatih.microservices.payment.processor;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProcessorFactory {

    private final Map<PaymentMethod, PaymentProcessor> processors;

    public PaymentProcessorFactory(
            List<PaymentProcessor> paymentProcessors) {

        this.processors =
                new EnumMap<>(PaymentMethod.class);

        for (PaymentProcessor processor : paymentProcessors) {

            processors.put(
                    processor.getPaymentMethod(),
                    processor
            );
        }
    }

    public PaymentProcessor getProcessor(
            PaymentMethod paymentMethod) {

        PaymentProcessor processor =
                processors.get(paymentMethod);

        if (processor == null) {

            throw new IllegalArgumentException(
                    "Unsupported payment method: "
                            + paymentMethod
            );
        }

        return processor;
    }
}