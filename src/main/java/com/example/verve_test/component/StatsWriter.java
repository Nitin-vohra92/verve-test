package com.example.verve_test.component;

import org.springframework.stereotype.Component;

@Component
public interface StatsWriter {
    void writeStats(int count, String timestamp);
}