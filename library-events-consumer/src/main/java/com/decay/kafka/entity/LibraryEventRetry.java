package com.decay.kafka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEventRetry {

    @Id
    @GeneratedValue
    private Integer id;
    private LibraryEvent libraryEvent;
    private boolean isRetry;
}
