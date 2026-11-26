package com.stacktrace.comment_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Every outgoing service-to-service call carries the shared internal token. */
@Configuration
public class FeignConfig {

    @Bean
    RequestInterceptor internalTokenInterceptor(@Value("${internal.api-token}") String token) {
        if (token == null || token.length() < 16) {
            throw new IllegalStateException("INTERNAL_API_TOKEN must be at least 16 characters");
        }
        return template -> template.header("X-Internal-Token", token);
    }
}
