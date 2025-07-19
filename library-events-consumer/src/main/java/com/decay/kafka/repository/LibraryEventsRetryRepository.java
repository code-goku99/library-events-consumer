package com.decay.kafka.repository;

import com.decay.kafka.entity.LibraryEvent;
import com.decay.kafka.entity.LibraryEventRetry;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventsRetryRepository extends CrudRepository<LibraryEventRetry,Integer> {
}
