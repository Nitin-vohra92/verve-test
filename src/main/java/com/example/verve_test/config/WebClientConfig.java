package com.example.verve_test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import reactor.netty.resources.ConnectionProvider;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    @Primary
    public WebClient webClient() {
        // Configure a Connection Provider to manage large number of concurrent connections
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(10000) // Max number of concurrent connections
                .pendingAcquireMaxCount(10000) // Max pending acquire requests
                .pendingAcquireTimeout(Duration.ofMillis(1000)) // Timeout for pending acquire requests
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofMillis(5000)); // Timeout for responses

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
