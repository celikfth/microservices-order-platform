package com.fatih.microservices.payment.kafka;

import com.fatih.microservices.payment.event.OrderCreatedEvent;
import com.fatih.microservices.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final PaymentService paymentService;

    public OrderCreatedConsumer(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "payment-service"
    )
    public void consume(
            OrderCreatedEvent event) {

        log.info(
                "ORDER_CREATED received: orderId={}, amount={}",
                event.getOrderId(),
                event.getTotalAmount()
        );

        paymentService.processOrderPayment(event);
    }
}