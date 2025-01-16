package com.example.verve_test.component;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.stats.output", havingValue = "kafka")
@RequiredArgsConstructor
public class KafkaStatsWriter implements StatsWriter {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(KafkaStatsWriter.class);

    @Override
    public void writeStats(int count, String timestamp) {
        Map<String, Object> message = new HashMap<>();
        message.put("count", count);
        message.put("timestamp", timestamp);

        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("request-stats", json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send to Kafka", ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to send to Kafka", e);
        }
    }
}

