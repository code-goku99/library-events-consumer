package com.decay.kafka.consumer;

import com.decay.kafka.entity.LibraryEvent;
import com.decay.kafka.entity.LibraryEventRetry;
import com.decay.kafka.service.LibraryEventsRetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventDLQListerner {

    private final LibraryEventsRetryService libraryEventsRetryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"${kafka.dlt-topic}"}, groupId = "lib-events-dlt-gid")
    public void retryConsumerFailureRecords(ConsumerRecord<Integer, String> consumerRecord) {
        try{
            log.info("Retry Consumer Message {}", consumerRecord);
            libraryEventsRetryService.saveLibraryEvent(buildLibraryEvent(consumerRecord));
        } catch (Exception e) {
            log.error("Exception occurred ex:{}", e.getMessage());
        }

    }

    private LibraryEventRetry buildLibraryEvent(ConsumerRecord<Integer, String> consumerRecord)
            throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        return LibraryEventRetry.builder()
                .libraryEvent(libraryEvent)
                .isRetry(false)
                .topicName(consumerRecord.topic())
                .kafkaPartition(consumerRecord.partition())
                .kafkaOffset(consumerRecord.offset())
                .build();
    }
}
