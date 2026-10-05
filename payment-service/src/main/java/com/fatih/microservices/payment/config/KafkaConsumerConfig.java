package com.fatih.microservices.payment.config;

import com.fatih.microservices.payment.exception.PaymentProcessingException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,

                        (record, exception) ->
                                new TopicPartition(
                                        record.topic()
                                                + ".DLT",

                                        record.partition()
                                )
                );


        FixedBackOff backOff =
                new FixedBackOff(
                        2000L,
                        2L
                );


        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        backOff
                );


        errorHandler.addRetryableExceptions(
                PaymentProcessingException.class
        );


        return errorHandler;
    }
}