package com.danifgx.contratacionpublica.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Configuration class for Jackson serialization/deserialization.
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates a custom module for handling LocalDateTime with offset information.
     * This allows parsing date-time strings with offsets directly into LocalDateTime objects.
     */
    @Bean
    public Module localDateTimeModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeWithOffsetDeserializer());
        return module;
    }

    /**
     * Custom deserializer for LocalDateTime that can handle offset information.
     * It first tries to parse the string as a LocalDateTime, and if that fails,
     * it tries to parse it as an OffsetDateTime and then converts to LocalDateTime.
     */
    public static class LocalDateTimeWithOffsetDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateTimeStr = p.getText();
            try {
                // First try to parse as LocalDateTime
                return LocalDateTime.parse(dateTimeStr);
            } catch (DateTimeParseException e) {
                try {
                    // If that fails, try to parse as OffsetDateTime and convert to LocalDateTime
                    return OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
                } catch (DateTimeParseException e2) {
                    throw new IOException("Failed to parse date-time string: " + dateTimeStr, e2);
                }
            }
        }
    }
}