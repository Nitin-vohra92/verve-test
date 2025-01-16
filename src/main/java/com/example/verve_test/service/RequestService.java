package com.example.verve_test.service;

import com.example.verve_test.component.StatsWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    private final ReactiveRedisTemplate<String, String> customReactiveRedisTemplate;
    private final WebClient webClient;
    private final StatsWriter statsWriter;

    @PostConstruct
    public void testRedisConnection() {
        customReactiveRedisTemplate.opsForValue()
                .set("test", "test")
                .doOnSuccess(result -> logger.info("Redis connection successful"))
                .doOnError(error -> logger.error("Redis connection failed", error))
                .subscribe();
    }

    // Scheduler to run every minute
    @Scheduled(cron = "0 * * * * *")
    public void processPreviousMinute() {
        String previousMinute = LocalDateTime.now()
                .minus(1, ChronoUnit.MINUTES) // Get the previous minute
                .truncatedTo(ChronoUnit.MINUTES)
                .format(DateTimeFormatter.ISO_DATE_TIME);

        String redisKey = "requests:" + previousMinute;

        customReactiveRedisTemplate.opsForSet()
                .members(redisKey)
                .collectList()
                .flatMap(requests -> {
                    int uniqueCount = requests.size();

                    if (uniqueCount > 0) {
                        statsWriter.writeStats(uniqueCount, previousMinute);

                        log.info("Processed {} unique requests for minute {}", uniqueCount, previousMinute);
                    }

                    // Clean up Redis key after processing
                    return customReactiveRedisTemplate.delete(redisKey);
                })
                .doOnError(e -> log.error("Error processing records for {}", previousMinute, e))
                .subscribe();
    }

    public Mono<Boolean> processRequest(Integer id, String endpoint) {
        String currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                .format(DateTimeFormatter.ISO_DATE_TIME);
        String redisKey = "requests:" + currentMinute;

        return customReactiveRedisTemplate.opsForSet()
                .add(redisKey, id.toString())
                .flatMap(added -> {
                    // Set expiry for the Redis key
                    return customReactiveRedisTemplate.expire(redisKey, Duration.ofMinutes(2))
                            .then(Mono.just(true));
                })
                .flatMap(success -> {
                    return customReactiveRedisTemplate.opsForSet().size(redisKey)
                            .flatMap(count -> {
                                if (endpoint != null && !endpoint.isEmpty()) {
                                    return makePostHttpRequest(endpoint, count.intValue())
                                            .thenReturn(true);
                                }
                                log.info("Current minute count: {}", count);
                                return Mono.just(true);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error processing request", e);
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> makePostHttpRequest(String endpoint, int count) {
        return webClient.post()
                .uri(endpoint)
                .bodyValue(Map.of("uniqueRequests", count))
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    logger.info("HTTP Response Status: {}", response.getStatusCode());
                    return true;
                })
                .onErrorResume(e -> {
                    logger.error("HTTP request failed", e);
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> makeGetHttpRequest(String endpoint, int count) {
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    logger.info("HTTP Response Status: {}", response.getStatusCode());
                    return true;
                })
                .onErrorResume(e -> {
                    logger.error("HTTP request failed", e);
                    return Mono.just(false);
                });
    }
}
