package com.StackTrace.User_Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-test-secret-test-secret-0123456789",
        "internal.api-token=test-internal-token-123"
})
@AutoConfigureMockMvc
@Testcontainers
class UserServiceApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private JsonNode register(String username) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"%s Name","username":"%s","email":"%s@dev.io","password":"password123"}
                """.formatted(username, username, username))).andExpect(status().isCreated());
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s@DEV.io","password":"password123"}
                """.formatted(username))).andExpect(status().isOk()).andReturn();
        return om.readTree(r.getResponse().getContentAsString());
    }

    @Test
    void authProfileFollowAndLeaderboardFlow() throws Exception {
        JsonNode alice = register("alice");
        JsonNode bob = register("bob");
        String aliceToken = "Bearer " + alice.get("token").asText();

        // duplicate username (case-insensitive) is a 409, not a 500
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"X","username":"ALICE","email":"other@dev.io","password":"password123"}
                """)).andExpect(status().isConflict());

        // bad credentials -> 401 with the shared error shape
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"alice@dev.io","password":"wrong-password"}
                """)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));

        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.badges[0].name").value("Newcomer"));

        long bobId = bob.get("id").asLong();
        mvc.perform(post("/api/users/" + bobId + "/follow").header("Authorization", aliceToken))
                .andExpect(status().isOk());
        mvc.perform(post("/api/users/" + bobId + "/follow").header("Authorization", aliceToken))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/users/username/bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followersCount").value(1));
        mvc.perform(get("/api/users/" + bobId + "/followers"))
                .andExpect(jsonPath("$.content[0].username").value("alice"));
        mvc.perform(get("/api/users/" + bobId + "/follow-status").header("Authorization", aliceToken))
                .andExpect(jsonPath("$.following").value(true));

        // points are internal-only and award threshold badges
        mvc.perform(post("/internal/users/" + bobId + "/points").contentType(MediaType.APPLICATION_JSON)
                .content("{\"delta\":60}")).andExpect(status().isForbidden());
        mvc.perform(post("/internal/users/" + bobId + "/points").header("X-Internal-Token", "test-internal-token-123")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"delta\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(60))
                .andExpect(jsonPath("$.badges.length()").value(2));

        mvc.perform(get("/api/users/leaderboard?size=1000"))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.content[0].username").value("bob"))
                .andExpect(jsonPath("$.content[0].rank").value(1));

        mvc.perform(get("/api/users/search?keyword=ali"))
                .andExpect(jsonPath("$.content[0].username").value("alice"));
        mvc.perform(get("/internal/users/batch?ids=" + bobId).header("X-Internal-Token", "test-internal-token-123"))
                .andExpect(jsonPath("$[0].username").value("bob"));
        mvc.perform(get("/api/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    void refreshTokensRotateAndDetectReuse() throws Exception {
        JsonNode carol = register("carol");
        String refresh1 = carol.get("refreshToken").asText();

        MvcResult r = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh1 + "\"}"))
                .andExpect(status().isOk()).andReturn();
        String refresh2 = om.readTree(r.getResponse().getContentAsString()).get("refreshToken").asText();
        assertThat(refresh2).isNotEqualTo(refresh1);

        // reusing the rotated token is rejected and revokes the whole family
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh1 + "\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh2 + "\"}")).andExpect(status().isUnauthorized());
    }
}
