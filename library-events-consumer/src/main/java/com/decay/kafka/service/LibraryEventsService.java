package com.decay.kafka.service;

import com.decay.kafka.entity.LibraryEvent;
import com.decay.kafka.repository.LibraryEventsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsService {

    private final LibraryEventsRepository libraryEventsRepository;
    private final ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord)  {

        try {
            LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
            log.info("libraryEvent : {} ", libraryEvent);
            if (libraryEvent.getLibraryEventId() !=null && libraryEvent.getLibraryEventId() == 999) {
                throw new RecoverableDataAccessException("Temporary Network Issue");
            }
            performRepoOperations(libraryEvent);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw e;
        } catch (Exception ex) {
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }
    }

    private void performRepoOperations(LibraryEvent libraryEvent) {
        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                this.saveLibraryEvent(libraryEvent);
                break;
            case UPDATE:
                if (validate(libraryEvent)) {
                    this.saveLibraryEvent(libraryEvent);
                } else {
                    throw new IllegalArgumentException("Not a valid library Event");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid event Type");
        }
    }

    private boolean validate(LibraryEvent libraryEvent) {
         return Optional.ofNullable(libraryEvent.getLibraryEventId())
                 .map(id -> libraryEventsRepository.findById(id).isPresent())
                .orElseThrow(() -> new IllegalArgumentException("Library Event Id is missing"));
    }

    public void saveLibraryEvent(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
    }

}
