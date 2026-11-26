package com.stacktrace.post_service.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stacktrace.post_service.dto.response.*;
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
import java.util.List;

/**
 * Redis cache for hot reads. Each cache has its own TTL and a typed JSON serializer (no class
 * metadata stored in Redis). Writes evict the affected entries. If Redis is unavailable, cache
 * errors are only logged and requests fall through to the database.
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String POST_BY_SLUG = "postBySlug";
    public static final String LATEST_FEED = "latestFeed";
    public static final String TRENDING = "trending";
    public static final String TAGS = "popularTags";
    public static final String AUTHORS = "authors";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private static RedisCacheConfiguration typed(JavaType type, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith("post-service:")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(MAPPER, type)));
    }

    @Bean
    RedisCacheManagerBuilderCustomizer postCaches() {
        TypeFactory tf = MAPPER.getTypeFactory();
        JavaType summaryPage = tf.constructParametricType(PageResponse.class, PostSummaryResponse.class);
        return builder -> builder
                .withCacheConfiguration(POST_BY_SLUG, typed(tf.constructType(PostResponse.class), Duration.ofMinutes(5)))
                .withCacheConfiguration(LATEST_FEED, typed(summaryPage, Duration.ofSeconds(30)))
                .withCacheConfiguration(TRENDING, typed(summaryPage, Duration.ofMinutes(2)))
                .withCacheConfiguration(TAGS, typed(tf.constructCollectionType(List.class, TagResponse.class), Duration.ofMinutes(5)))
                .withCacheConfiguration(AUTHORS, typed(tf.constructType(AuthorSummary.class), Duration.ofMinutes(10)));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler(true);
    }
}
