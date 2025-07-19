package com.decay.kafka.entity;

import com.decay.kafka.LibraryEventConverter;
import jakarta.persistence.*;
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
    @Lob
    @Convert(converter = LibraryEventConverter.class)
    private LibraryEvent libraryEvent;
    private boolean isRetry;
    private String topicName;
    private Integer kafkaPartition;
    private Long kafkaOffset;
    private String errorDetails;

}
