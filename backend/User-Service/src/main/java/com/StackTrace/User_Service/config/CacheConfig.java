package com.StackTrace.User_Service.config;

import com.StackTrace.User_Service.dto.LeaderboardResponse;
import com.StackTrace.User_Service.dto.PagedResponse;
import com.StackTrace.User_Service.dto.UserProfileResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/** Redis cache for profiles and the leaderboard; Redis failures fall through to the database. */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String PROFILES = "profiles";
    public static final String LEADERBOARD = "leaderboard";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static RedisCacheConfiguration typed(JavaType type, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith("user-service:")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(MAPPER, type)));
    }

    @Bean
    RedisCacheManagerBuilderCustomizer userCaches() {
        var tf = MAPPER.getTypeFactory();
        return builder -> builder
                .withCacheConfiguration(PROFILES, typed(tf.constructType(UserProfileResponse.class), Duration.ofMinutes(5)))
                .withCacheConfiguration(LEADERBOARD, typed(
                        tf.constructParametricType(PagedResponse.class, LeaderboardResponse.class), Duration.ofSeconds(60)));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler(true);
    }
}
