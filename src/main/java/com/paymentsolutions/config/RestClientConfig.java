package com.paymentsolutions.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.client.RestTemplate;

/**
 * Configuration for REST client operations
 * Primarily used for Claude AI API integration
 */
@Configuration
@Slf4j
public class RestClientConfig {


    /**
     * RestTemplate bean configured for external API calls
     * Used by AIAgentService to call Claude API
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("🔧 Configuring RestTemplate for AI Agent service");

        return builder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }
}