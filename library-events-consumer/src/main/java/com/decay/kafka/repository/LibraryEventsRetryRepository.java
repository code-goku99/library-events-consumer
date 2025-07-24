package com.decay.kafka.repository;

import com.decay.kafka.entity.LibraryEventRetry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LibraryEventsRetryRepository extends CrudRepository<LibraryEventRetry,Integer> {

    public List<LibraryEventRetry> findByIsRetry(boolean isRetry);
}
