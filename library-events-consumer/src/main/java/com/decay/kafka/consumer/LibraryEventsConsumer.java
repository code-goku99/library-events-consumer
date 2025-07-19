package com.decay.kafka.consumer;

import com.decay.kafka.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumer {

    private final LibraryEventsService libraryEventsService;


    @KafkaListener(topics = {"${kafka.topic}"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        log.info("Consumer Message {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);

    }

    @KafkaListener(topics = {"${kafka.retry-topic}"}, groupId = "lib-events-retry-gid")
    public void retryConsumerFailureRecords(ConsumerRecord<Integer, String> consumerRecord) {
        log.info("Retry Consumer Message {}", consumerRecord);
       // consumerRecord.headers().forEach(header -> System.out.println(header.key() + " value"+ new String(header.value())));
        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
