package com.stacktrace.post_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacktrace.post_service.client.UserClient;
import com.stacktrace.post_service.dto.response.AuthorSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the Redis cache round-trips our DTOs and that writes evict stale entries. */
@SpringBootTest(properties = {
        "jwt.secret=" + PostServiceApplicationTests.SECRET,
        "internal.api-token=test-internal-token-123"
})
@AutoConfigureMockMvc
@Testcontainers
class PostCachingTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    StringRedisTemplate redisTemplate;
    @MockitoBean
    UserClient userClient;

    @Test
    void cachedReadsRoundTripAndWritesEvict() throws Exception {
        when(userClient.findAuthors(any())).thenReturn(Map.of(9L, new AuthorSummary(9L, "Nine", "nine", null)));
        String body = mvc.perform(post("/api/posts").header("Authorization", PostServiceApplicationTests.bearer(9))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Cache me\",\"content\":\"hello world\",\"tags\":[\"redis\"],\"publish\":true}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = om.readTree(body).get("id").asLong();

        for (int i = 0; i < 2; i++) { // second call is served from Redis
            mvc.perform(get("/api/posts/cache-me")).andExpect(jsonPath("$.title").value("Cache me"))
                    .andExpect(jsonPath("$.author.username").value("nine"))
                    .andExpect(jsonPath("$.publishedAt").isString());
            mvc.perform(get("/api/posts")).andExpect(jsonPath("$.content[0].tags[0]").value("redis"));
            mvc.perform(get("/api/tags")).andExpect(jsonPath("$[0].name").value("redis"));
        }
        assertThat(redisTemplate.keys("post-service:postBySlug::*")).isNotEmpty();
        assertThat(redisTemplate.keys("post-service:latestFeed::*")).isNotEmpty();

        mvc.perform(put("/api/posts/" + id).header("Authorization", PostServiceApplicationTests.bearer(9))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Cache me v2\",\"content\":\"hello again\",\"tags\":[\"redis\"]}")).andExpect(status().isOk());
        mvc.perform(get("/api/posts/cache-me")).andExpect(jsonPath("$.title").value("Cache me v2"));
        mvc.perform(get("/api/posts")).andExpect(jsonPath("$.content[0].title").value("Cache me v2"));

        mvc.perform(delete("/api/posts/" + id).header("Authorization", PostServiceApplicationTests.bearer(9)));
        mvc.perform(get("/api/posts/cache-me")).andExpect(status().isNotFound());
    }
}
