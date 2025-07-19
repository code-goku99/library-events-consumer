package com.decay.kafka.service;

import com.decay.kafka.entity.LibraryEventRetry;
import com.decay.kafka.repository.LibraryEventsRetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsRetryService {

    private final LibraryEventsRetryRepository libraryEventsRetryRepository;

    public void saveLibraryEvent(LibraryEventRetry libraryEvent) {
        libraryEventsRetryRepository.save(libraryEvent);
    }

}
