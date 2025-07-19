package com.decay.kafka.config;

import com.decay.kafka.entity.LibraryEvent;
import com.decay.kafka.entity.LibraryEventRetry;
import com.decay.kafka.repository.LibraryEventsRetryRepository;
import com.decay.kafka.service.LibraryEventsRetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
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

    private final LibraryEventsRetryService libraryEventsRetryService;
    private final ObjectMapper objectMapper;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<Integer, String> kafkaTemplate) {

        ConsumerRecordRecoverer recoverer = ( consumerRecord, exception) -> {
            var consumerRec = (ConsumerRecord<Integer, String>) consumerRecord;
            if (exception.getCause() instanceof RecoverableDataAccessException) {
                libraryEventsRetryService.saveLibraryEvent(buildLibraryEvent(consumerRec, exception, true));
            } else {
                libraryEventsRetryService.saveLibraryEvent(buildLibraryEvent(consumerRec, exception, false));
            }
        };

        // Retry 3 times with 1 second interval
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 1));

        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class); // Don't retry on known bad data

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    private LibraryEventRetry buildLibraryEvent(ConsumerRecord<Integer, String> consumerRecord,
                                                Exception exception, boolean isRetry)
             {
                 LibraryEvent libraryEvent = null;
                 try {
                     libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
                 } catch (Exception e) {
                     log.error("");
                 }
                 return LibraryEventRetry.builder()
                .libraryEvent(libraryEvent)
                .isRetry(isRetry)
                .topicName(consumerRecord.topic())
                .kafkaPartition(consumerRecord.partition())
                .kafkaOffset(consumerRecord.offset())
                .errorDetails(exception.getMessage())
                .build();
    }
}
