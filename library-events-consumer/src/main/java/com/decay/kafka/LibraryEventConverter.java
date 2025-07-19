package com.decay.kafka;

import com.decay.kafka.entity.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LibraryEventConverter implements AttributeConverter<LibraryEvent, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(LibraryEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting LibraryEvent to JSON", e);
        }
    }

    @Override
    public LibraryEvent convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, LibraryEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading LibraryEvent from JSON", e);
        }
    }
}
