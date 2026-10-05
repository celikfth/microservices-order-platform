package com.fatih.microservices.payment.processor;

import com.fatih.microservices.payment.exception.PaymentProcessingException;
import com.fatih.microservices.payment.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class StripePaymentProcessor implements PaymentProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(StripePaymentProcessor.class);

    @Override
    public PaymentResult process(Payment payment) {

        log.info(
                "Processing payment with Stripe. orderId={}, amount={}",
                payment.getOrderId(),
                payment.getAmount()
        );


        // ------------------------------------------------
        // 1. Simulate TECHNICAL failure
        // ------------------------------------------------
        //
        // If the amount is exactly $999.00,
        // pretend Stripe is temporarily unavailable.
        //
        // This is a RETRYABLE error.
        //
        // Expected flow:
        //
        // Attempt #1
        //      ↓
        // PaymentProcessingException
        //      ↓
        // Retry #1
        //      ↓
        // PaymentProcessingException
        //      ↓
        // Retry #2
        //      ↓
        // PaymentProcessingException
        //      ↓
        // DLT
        //
        // ------------------------------------------------

        if (payment.getAmount()
                .compareTo(new BigDecimal("999.00")) == 0) {

            log.error(
                    "Stripe technical failure simulated for orderId={}",
                    payment.getOrderId()
            );

            throw new PaymentProcessingException(
                    "Stripe temporarily unavailable"
            );
        }


        // ------------------------------------------------
        // 2. Simulate BUSINESS failure
        // ------------------------------------------------
        //
        // Amount > $1000
        //
        // Pretend Stripe declined the payment.
        //
        // IMPORTANT:
        // This is NOT a technical error.
        //
        // Therefore:
        //
        // NO exception
        // NO retry
        // NO DLT
        //
        // PaymentResult(false) is returned.
        //
        // PaymentService will change:
        //
        // PROCESSING
        //      ↓
        // FAILED
        //
        // and publish PAYMENT_FAILED.
        //
        // ------------------------------------------------

        if (payment.getAmount()
                .compareTo(new BigDecimal("1000.00")) > 0) {

            log.warn(
                    "Stripe payment declined for orderId={}, amount={}",
                    payment.getOrderId(),
                    payment.getAmount()
            );

            return new PaymentResult(
                    false,
                    null,
                    "Payment declined by Stripe"
            );
        }


        // ------------------------------------------------
        // 3. Successful payment
        // ------------------------------------------------

        String transactionId =
                "STRIPE-" + UUID.randomUUID();


        log.info(
                "Stripe payment successful. orderId={}, transactionId={}",
                payment.getOrderId(),
                transactionId
        );


        return new PaymentResult(
                true,
                transactionId,
                null
        );
    }


    // ------------------------------------------------
    // Payment method supported by this processor
    // ------------------------------------------------

    @Override
    public PaymentMethod getPaymentMethod() {

        return PaymentMethod.STRIPE;
    }
}