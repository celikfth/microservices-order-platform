package com.fatih.microservices.order.kafka;

import tools.jackson.databind.ObjectMapper;
import com.fatih.microservices.order.event.PaymentCompletedEvent;
import com.fatih.microservices.order.event.PaymentFailedEvent;
import com.fatih.microservices.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public PaymentEventConsumer(
            ObjectMapper objectMapper,
            OrderService orderService) {

        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "payment-completed",
            groupId = "order-service"
    )
    public void consumePaymentCompleted(
            String message) throws Exception {

        PaymentCompletedEvent event =
                objectMapper.readValue(
                        message,
                        PaymentCompletedEvent.class
                );

        orderService.handlePaymentCompleted(event);
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-service"
    )
    public void consumePaymentFailed(
            String message) throws Exception {

        PaymentFailedEvent event =
                objectMapper.readValue(
                        message,
                        PaymentFailedEvent.class
                );

        orderService.handlePaymentFailed(event);
    }
}