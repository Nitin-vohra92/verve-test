package com.example.verve_test.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StatsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(StatsConsumer.class);
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "request-stats", groupId = "stats-group")
    public void consume(String message) {
        try {
            Map<String, Object> stats = objectMapper.readValue(message, Map.class);
            logger.info("Unique requests in the last minute: {}", stats.get("count"));
        } catch (Exception e) {
            logger.error("Failed to process stats", e);
        }
    }
}