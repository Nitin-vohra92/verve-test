package com.example.verve_test.component;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "app.stats.output", havingValue = "file", matchIfMissing = true)
@Slf4j
public class FileStatsWriter implements StatsWriter {
    private static final String DEFAULT_STATS_DIR = "./stats";
    private final String statsDirectory;

    public FileStatsWriter(@Value("${app.stats.directory:#{null}}") String configuredDirectory) {
        this.statsDirectory = configuredDirectory != null ? configuredDirectory : DEFAULT_STATS_DIR;
        createStatsDirectory();
    }

    private void createStatsDirectory() {
        try {
            Files.createDirectories(Paths.get(statsDirectory));
        } catch (IOException e) {
            log.error("Failed to create stats directory: {}", statsDirectory, e);
        }
    }

    @Override
    public void writeStats(int count, String timestamp) {
        // First, log to application log
        log.info("Unique requests count for minute {}: {}", timestamp, count);

        // Then write to stats file
        LocalDate today = LocalDate.now();
        String fileName = String.format("stats_%s.txt", today.format(DateTimeFormatter.ISO_DATE));
        Path filePath = Paths.get(statsDirectory, fileName);

        String statsLine = String.format("%s,count=%d%n", timestamp, count);

        try {
            Files.writeString(filePath, statsLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.debug("Successfully wrote stats to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to write stats to file: {}", filePath, e);
        }
    }
}
