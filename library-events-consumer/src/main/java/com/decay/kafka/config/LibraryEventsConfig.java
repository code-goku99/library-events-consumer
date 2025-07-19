package com.decay.kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
//@EnableKafka -> not need in latest version of kafka
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConfig {

    @Value("${kafka.retry-topic}")
    private String retryTopic;

    @Value("${kafka.dlt-topic}")
    private String deadLetterTopic;


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<Integer, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    int currentAttempt = 1;
                    Headers headers = record.headers();
                    Header retryHeader = headers.lastHeader("x-retry-attempt");
                    if (retryHeader != null) {
                        log.info("Retry Header :{}", retryHeader.value());
                        currentAttempt = Integer.parseInt(new String(retryHeader.value())) + 1;
                    }
                    log.info("currentAttempt:{}", currentAttempt);
                    if (currentAttempt < 2 && exception.getCause() instanceof RecoverableDataAccessException) {
                        log.info("Inside retry Topic with currentAttempt:{}", currentAttempt);
                        headers.remove("x-retry-attempt");
                        headers.add("x-retry-attempt", String.valueOf(currentAttempt).getBytes());
                        return new TopicPartition(retryTopic, record.partition());
                    } else {
                        log.info("Inside dead letter Topic");
                        return new TopicPartition(deadLetterTopic, record.partition());
                    }
                });

        // Retry 3 times with 1 second interval
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 1));

        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class); // Don't retry on known bad data

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
