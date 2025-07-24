package com.decay.kafka.service;

import com.decay.kafka.entity.LibraryEvent;
import com.decay.kafka.entity.LibraryEventRetry;
import com.decay.kafka.repository.LibraryEventsRetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsRetryScheduler {

    private final LibraryEventsRetryRepository repository;
    private final LibraryEventsService libraryEventsService;

  //  @Scheduled(fixedRate = 100000)
    public void retryFailedRecords() {

        List<LibraryEventRetry> libraryEventRetries = repository.findByIsRetry(true);
        // add try catch
        libraryEventRetries.forEach(libraryEventRetry -> {
            var consumerRecord = buildConsumerRecord(libraryEventRetry);
            libraryEventsService.processLibraryEvent(consumerRecord);
            libraryEventRetry.setRetry(false);
        });
        Optional.of(libraryEventRetries)
                .filter(not(CollectionUtils::isEmpty))
                .ifPresent(repository::saveAll);
    }
    private ConsumerRecord<Integer, String> buildConsumerRecord(LibraryEventRetry eventRetry) {
        return new ConsumerRecord<>(eventRetry.getTopicName(), eventRetry.getKafkaPartition(),eventRetry.getKafkaOffset(),
                null, eventRetry.getErrorDetails());
    }
}
