package com.stacktrace.post_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacktrace.post_service.client.UserClient;
import com.stacktrace.post_service.dto.response.AuthorSummary;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=" + PostServiceApplicationTests.SECRET,
        "internal.api-token=test-internal-token-123"
})
@AutoConfigureMockMvc
@Testcontainers
class PostServiceApplicationTests {

    static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @MockitoBean
    UserClient userClient;

    @BeforeEach
    void stubUsers() {
        when(userClient.findAuthors(any())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(Function.identity(),
                    id -> new AuthorSummary(id, "User " + id, "user" + id, null)));
        });
    }

    static String bearer(long userId) {
        String jwt = Jwts.builder().issuer("stacktrace").subject(String.valueOf(userId))
                .claim("userId", userId).claim("typ", "access")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        return "Bearer " + jwt;
    }

    private JsonNode json(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return om.readTree(r.andReturn().getResponse().getContentAsString());
    }

    @Test
    void postLifecycleOwnershipEngagementAndSearch() throws Exception {
        String body = """
                {"title":"Mastering Spring Boot Caching","content":"# Intro\\n\\nRedis makes **Spring** apps fast.\\n\\n```java\\nclass A {}\\n```","tags":["Java","springboot"]}
                """;
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        JsonNode draft = json(mvc.perform(post("/api/posts").header("Authorization", bearer(1))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.slug").value("mastering-spring-boot-caching"))
                .andExpect(jsonPath("$.excerpt").value("Intro Redis makes Spring apps fast."))
                .andExpect(jsonPath("$.tags[0]").value("java"))
                .andExpect(jsonPath("$.author.username").value("user1")));
        long id = draft.get("id").asLong();
        String slug = draft.get("slug").asText();

        // drafts are invisible to everyone but the author
        mvc.perform(get("/api/posts/" + slug)).andExpect(status().isNotFound());
        mvc.perform(get("/api/posts/" + slug).header("Authorization", bearer(2))).andExpect(status().isNotFound());
        mvc.perform(get("/api/posts/" + slug).header("Authorization", bearer(1))).andExpect(status().isOk());

        // only the author can edit/publish/delete
        mvc.perform(post("/api/posts/" + id + "/publish").header("Authorization", bearer(2)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/posts/" + id + "/publish").header("Authorization", bearer(1)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        verify(userClient, timeout(2000)).addPoints(1L, 10);
        mvc.perform(post("/api/posts/" + id + "/publish").header("Authorization", bearer(1)))
                .andExpect(status().isConflict());

        // editing a published post keeps its URL
        mvc.perform(put("/api/posts/" + id).header("Authorization", bearer(1)).contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("Mastering", "Really Mastering")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.title").value("Really Mastering Spring Boot Caching"));

        mvc.perform(get("/api/posts")).andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].content").doesNotExist());
        mvc.perform(get("/api/posts?tag=springboot")).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/posts?tag=rust")).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/posts?size=5000")).andExpect(jsonPath("$.size").value(50));
        mvc.perform(get("/api/tags")).andExpect(jsonPath("$[0].postCount").value(1));

        // likes are idempotent and counted once
        mvc.perform(post("/api/posts/" + id + "/like").header("Authorization", bearer(2)));
        mvc.perform(post("/api/posts/" + id + "/like").header("Authorization", bearer(2)))
                .andExpect(jsonPath("$.likeCount").value(1)).andExpect(jsonPath("$.liked").value(true));
        verify(userClient, timeout(2000).times(1)).addPoints(1L, 1);
        mvc.perform(get("/api/posts/" + id + "/engagement")).andExpect(jsonPath("$.liked").value(false));
        mvc.perform(post("/api/posts/" + id + "/bookmark").header("Authorization", bearer(2)))
                .andExpect(jsonPath("$.bookmarked").value(true));
        mvc.perform(get("/api/posts/bookmarks").header("Authorization", bearer(2)))
                .andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(get("/api/posts/trending")).andExpect(jsonPath("$.content[0].likeCount").value(1));

        // following feed uses user-service's following ids
        when(userClient.followingIds(2L)).thenReturn(List.of(1L));
        mvc.perform(get("/api/posts/feed/following").header("Authorization", bearer(2)))
                .andExpect(jsonPath("$.totalElements").value(1));

        // internal API: token required; FTS on content + partial title match
        mvc.perform(get("/internal/posts/search?keyword=redis")).andExpect(status().isForbidden());
        mvc.perform(get("/internal/posts/search?keyword=redis").header("X-Internal-Token", "test-internal-token-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/internal/posts/search?keyword=Mast").header("X-Internal-Token", "test-internal-token-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/internal/posts/search?keyword=kubernetes").header("X-Internal-Token", "test-internal-token-123"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(post("/internal/posts/" + id + "/comment-count").header("X-Internal-Token", "test-internal-token-123")
                .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":1}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/" + slug)).andExpect(jsonPath("$.commentCount").value(1));

        mvc.perform(delete("/api/posts/" + id).header("Authorization", bearer(2))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/posts/" + id).header("Authorization", bearer(1))).andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/" + slug)).andExpect(status().isNotFound());
    }

    @Test
    void validationErrorsUseSharedShape() throws Exception {
        mvc.perform(post("/api/posts").header("Authorization", bearer(3)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"x\",\"tags\":[\"bad tag!\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
        verify(userClient, never()).addPoints(eq(3L), anyInt());
    }
}
