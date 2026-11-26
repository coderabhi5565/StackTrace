package com.stacktrace.comment_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacktrace.comment_service.client.NotificationClient;
import com.stacktrace.comment_service.client.PostClient;
import com.stacktrace.comment_service.client.UserClient;
import com.stacktrace.comment_service.dto.response.AuthorResponse;
import com.stacktrace.comment_service.dto.response.AuthorSummary;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=" + CommentServiceApplicationTests.SECRET,
        "internal.api-token=test-internal-token-123"
})
@AutoConfigureMockMvc
@Testcontainers
class CommentServiceApplicationTests {

    static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @MockitoBean
    PostClient postClient;
    @MockitoBean
    UserClient userClient;
    @MockitoBean
    NotificationClient notificationClient;

    @BeforeEach
    void stubs() {
        when(postClient.exists(10L)).thenReturn(true);
        when(postClient.exists(99L)).thenReturn(false);
        when(postClient.exists(11L)).thenReturn(true);
        when(postClient.getAuthorId(11L)).thenReturn(new AuthorResponse(1L));
        when(postClient.getAuthorId(10L)).thenReturn(new AuthorResponse(1L));
        when(userClient.batch(any())).thenAnswer(inv -> ((Collection<Long>) inv.getArgument(0)).stream()
                .map(id -> new AuthorSummary(id, "User " + id, "user" + id, null)).toList());
    }

    static String bearer(long userId) {
        return "Bearer " + Jwts.builder().issuer("stacktrace").claim("userId", userId).claim("typ", "access")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
    }

    private long comment(long user, String json) throws Exception {
        String body = mvc.perform(post("/api/comments").header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("id").asLong();
    }

    @Test
    void nestedThreadsOwnershipAndTombstones() throws Exception {
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\":10,\"content\":\"hi\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/comments").header("Authorization", bearer(2)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\":99,\"content\":\"hi\"}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/comments").header("Authorization", bearer(2)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"no post\"}")).andExpect(status().isBadRequest());

        long root = comment(2, "{\"postId\":10,\"content\":\"Great post\"}");
        long reply = comment(3, "{\"postId\":10,\"parentCommentId\":" + root + ",\"content\":\"Agreed\"}");
        long nested = comment(2, "{\"postId\":10,\"parentCommentId\":" + reply + ",\"content\":\"Thanks!\"}");
        comment(4, "{\"postId\":10,\"content\":\"Second thread\"}");

        // side effects are async and best effort
        verify(postClient, timeout(2000).times(4)).adjustCommentCount(eq(10L), any());
        verify(notificationClient, timeout(2000).atLeast(2)).createNotification(any());

        mvc.perform(get("/api/comments/post/10?size=1"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].content").value("Second thread"));
        mvc.perform(get("/api/comments/post/10?page=1&size=1"))
                .andExpect(jsonPath("$.content[0].id").value(root))
                .andExpect(jsonPath("$.content[0].author.username").value("user2"))
                .andExpect(jsonPath("$.content[0].replies[0].id").value(reply))
                .andExpect(jsonPath("$.content[0].replies[0].replies[0].id").value(nested));

        // only the owner may edit/delete
        mvc.perform(put("/api/comments/" + reply).header("Authorization", bearer(2))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"hijack\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/comments/" + reply).header("Authorization", bearer(3))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Strongly agreed\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value("Strongly agreed"));
        mvc.perform(delete("/api/comments/" + root).header("Authorization", bearer(3))).andExpect(status().isForbidden());

        // deleting a comment with replies leaves a tombstone so the thread stays readable
        mvc.perform(delete("/api/comments/" + root).header("Authorization", bearer(2))).andExpect(status().isNoContent());
        mvc.perform(get("/api/comments/post/10?page=1&size=1"))
                .andExpect(jsonPath("$.content[0].deleted").value(true))
                .andExpect(jsonPath("$.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.content[0].replies[0].content").value("Strongly agreed"));

        // once the whole thread is gone it disappears
        mvc.perform(delete("/api/comments/" + nested).header("Authorization", bearer(2)));
        mvc.perform(delete("/api/comments/" + reply).header("Authorization", bearer(3)));
        mvc.perform(get("/api/comments/post/10")).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void notificationOutageDoesNotBreakCommenting() throws Exception {
        doThrow(new RuntimeException("down")).when(notificationClient).createNotification(any());
        comment(5, "{\"postId\":11,\"content\":\"still works\"}");
    }
}
