package com.fatih.microservices.payment.service;

import com.fatih.microservices.payment.event.OrderCreatedEvent;
import com.fatih.microservices.payment.event.PaymentCompletedEvent;
import com.fatih.microservices.payment.event.PaymentFailedEvent;
import com.fatih.microservices.payment.kafka.PaymentEventProducer;
import com.fatih.microservices.payment.model.Payment;
import com.fatih.microservices.payment.model.PaymentStatus;
import com.fatih.microservices.payment.processor.PaymentMethod;
import com.fatih.microservices.payment.processor.PaymentProcessor;
import com.fatih.microservices.payment.processor.PaymentProcessorFactory;
import com.fatih.microservices.payment.processor.PaymentResult;
import com.fatih.microservices.payment.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentProcessorFactory paymentProcessorFactory,
            PaymentEventProducer paymentEventProducer) {

        this.paymentRepository = paymentRepository;
        this.paymentProcessorFactory = paymentProcessorFactory;
        this.paymentEventProducer = paymentEventProducer;
    }

    public void processOrderPayment(OrderCreatedEvent event) {

        // ------------------------------------------------
        // 1. Check if a payment already exists
        // ------------------------------------------------

        Payment payment =
                paymentRepository
                        .findByOrderId(event.getOrderId())
                        .orElse(null);


        // ------------------------------------------------
        // 2. Idempotency check
        // ------------------------------------------------

        if (payment != null) {

            // Payment was already completed.
            // Do NOT charge the customer again.
            if (payment.getStatus() == PaymentStatus.COMPLETED) {

                log.info(
                        "Payment already COMPLETED for orderId={}. Skipping.",
                        event.getOrderId()
                );

                return;
            }

            // Business failure already happened.
            // Do not process the same payment again.
            if (payment.getStatus() == PaymentStatus.FAILED) {

                log.info(
                        "Payment already FAILED for orderId={}. Skipping.",
                        event.getOrderId()
                );

                return;
            }

            // If status is PENDING or PROCESSING,
            // continue processing.
            // This allows Kafka retry to work.

            log.info(
                    "Existing payment found for orderId={}, status={}. Retrying processing.",
                    event.getOrderId(),
                    payment.getStatus()
            );
        }


        // ------------------------------------------------
        // 3. Create payment if it doesn't exist
        // ------------------------------------------------

        if (payment == null) {

            payment = new Payment();

            payment.setOrderId(
                    event.getOrderId()
            );

            payment.setAmount(
                    event.getTotalAmount()
            );


            // Convert the payment method received from Kafka
            // into our PaymentMethod enum.

            PaymentMethod paymentMethod =
                    PaymentMethod.valueOf(
                            event.getPaymentMethod()
                                    .toUpperCase()
                    );

            payment.setPaymentMethod(
                    paymentMethod
            );

            payment.setStatus(
                    PaymentStatus.PENDING
            );


            payment =
                    paymentRepository.save(payment);


            log.info(
                    "New payment created. paymentId={}, orderId={}, method={}, amount={}",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getPaymentMethod(),
                    payment.getAmount()
            );
        }


        // ------------------------------------------------
        // 4. Factory selects the correct payment processor
        // ------------------------------------------------

        PaymentProcessor processor =
                paymentProcessorFactory.getProcessor(
                        payment.getPaymentMethod()
                );


        log.info(
                "Payment processor selected: {} for orderId={}",
                processor.getClass().getSimpleName(),
                payment.getOrderId()
        );


        // ------------------------------------------------
        // 5. Change payment status to PROCESSING
        // ------------------------------------------------

        payment.setStatus(
                PaymentStatus.PROCESSING
        );

        payment =
                paymentRepository.save(payment);


        // ------------------------------------------------
        // 6. Call payment provider
        // ------------------------------------------------
        //
        // IMPORTANT:
        //
        // If Stripe/PayPal throws PaymentProcessingException,
        // we DO NOT catch it here.
        //
        // The exception goes back to Kafka's
        // DefaultErrorHandler.
        //
        // Kafka will then:
        //
        // attempt
        //   ↓
        // retry #1
        //   ↓
        // retry #2
        //   ↓
        // DLT
        //
        // ------------------------------------------------

        PaymentResult result =
                processor.process(payment);


        // ------------------------------------------------
        // 7. Payment SUCCESS
        // ------------------------------------------------

        if (result.isSuccessful()) {

            payment.setStatus(
                    PaymentStatus.COMPLETED
            );

            payment.setTransactionId(
                    result.getTransactionId()
            );

            payment.setFailureReason(null);


            payment =
                    paymentRepository.save(payment);


            log.info(
                    "Payment COMPLETED. paymentId={}, orderId={}, transactionId={}",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getTransactionId()
            );


            // --------------------------------------------
            // Create PAYMENT_COMPLETED event
            // --------------------------------------------

            PaymentCompletedEvent completedEvent =
                    new PaymentCompletedEvent(
                            payment.getId(),
                            payment.getOrderId(),
                            payment.getAmount(),
                            payment.getTransactionId(),
                            payment.getUpdatedAt()
                    );


            // --------------------------------------------
            // Publish event to Kafka
            // --------------------------------------------

            paymentEventProducer.publishPaymentCompleted(
                    completedEvent
            );


            log.info(
                    "PAYMENT_COMPLETED event published for orderId={}",
                    payment.getOrderId()
            );

        } else {

            // ------------------------------------------------
            // 8. Business failure
            // ------------------------------------------------
            //
            // Example:
            //
            // Card declined
            // Insufficient funds
            //
            // These should NOT throw PaymentProcessingException.
            //
            // They are legitimate payment results and should
            // become FAILED.
            //
            // ------------------------------------------------

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            payment.setFailureReason(
                    result.getFailureReason()
            );


            payment =
                    paymentRepository.save(payment);


            log.info(
                    "Payment FAILED. paymentId={}, orderId={}, reason={}",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getFailureReason()
            );


            // --------------------------------------------
            // Create PAYMENT_FAILED event
            // --------------------------------------------

            PaymentFailedEvent failedEvent =
                    new PaymentFailedEvent(
                            payment.getId(),
                            payment.getOrderId(),
                            payment.getAmount(),
                            payment.getFailureReason(),
                            payment.getUpdatedAt()
                    );


            // --------------------------------------------
            // Publish event to Kafka
            // --------------------------------------------

            paymentEventProducer.publishPaymentFailed(
                    failedEvent
            );


            log.info(
                    "PAYMENT_FAILED event published for orderId={}",
                    payment.getOrderId()
            );
        }
    }
}