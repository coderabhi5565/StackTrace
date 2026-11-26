package com.notification_service.StackTrace;

import com.notification_service.StackTrace.client.UserClient;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=" + StackTraceApplicationTests.SECRET,
        "internal.api-token=test-internal-token-123"
})
@AutoConfigureMockMvc
@Testcontainers
class StackTraceApplicationTests {

    static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mvc;
    @MockitoBean
    UserClient userClient;

    static String bearer(long userId) {
        return "Bearer " + Jwts.builder().issuer("stacktrace").claim("userId", userId).claim("typ", "access")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
    }

    @Test
    void notificationsAreScopedToTheRecipient() throws Exception {
        String req = "{\"recipientId\":1,\"senderId\":2,\"type\":\"COMMENT\",\"message\":\"commented on your post.\",\"referenceId\":10}";
        mvc.perform(post("/internal/notifications").contentType(MediaType.APPLICATION_JSON).content(req))
                .andExpect(status().isForbidden());
        String body = mvc.perform(post("/internal/notifications").header("X-Internal-Token", "test-internal-token-123")
                        .contentType(MediaType.APPLICATION_JSON).content(req))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
        // the old API trusted ?recipientId=; it is now ignored
        mvc.perform(get("/api/notifications?recipientId=1").header("Authorization", bearer(2)))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/notifications").header("Authorization", bearer(1)))
                .andExpect(jsonPath("$.content[0].message").value("commented on your post."));
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", bearer(1)))
                .andExpect(jsonPath("$.unread").value(1));

        mvc.perform(put("/api/notifications/" + id + "/read").header("Authorization", bearer(2)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/notifications/" + id).header("Authorization", bearer(2)))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/notifications/read-all").header("Authorization", bearer(1)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", bearer(1)))
                .andExpect(jsonPath("$.unread").value(0));
        mvc.perform(delete("/api/notifications/" + id).header("Authorization", bearer(1)))
                .andExpect(status().isNoContent());
    }
}
