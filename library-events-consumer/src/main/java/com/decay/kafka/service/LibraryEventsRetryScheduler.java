package com.decay.kafka.service;

import com.decay.kafka.repository.LibraryEventsRetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsRetryScheduler {

    private final LibraryEventsRetryRepository repository;

    @Scheduled(fixedRate = 10000)
    public void retryFailedRecords() {

    }
}
