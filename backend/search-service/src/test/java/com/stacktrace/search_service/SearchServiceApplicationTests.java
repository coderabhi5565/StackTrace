package com.stacktrace.search_service;

import com.stacktrace.search_service.client.PostClient;
import com.stacktrace.search_service.client.UserClient;
import com.stacktrace.search_service.dto.response.PageResponse;
import com.stacktrace.search_service.dto.response.PostSearchResponse;
import com.stacktrace.search_service.dto.response.UserSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-test-secret-test-secret-0123456789",
        "internal.api-token=test-internal-token-123"
})
@AutoConfigureMockMvc
class SearchServiceApplicationTests {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    PostClient postClient;
    @MockitoBean
    UserClient userClient;

    private static PostSearchResponse post() {
        PostSearchResponse p = new PostSearchResponse();
        p.setId(1L);
        p.setTitle("Spring caching");
        p.setSlug("spring-caching");
        return p;
    }

    @Test
    void aggregatesBothSectionsInParallel() throws Exception {
        when(postClient.searchPosts(eq("spring"), isNull(), eq(0), eq(50)))
                .thenReturn(new PageResponse<>(List.of(post()), 0, 50, 1, 1, true));
        when(userClient.searchUsers("spring", 0, 50))
                .thenReturn(new PageResponse<>(List.of(new UserSearchResponse(7L, "springfan", "Spring Fan", null)), 0, 50, 1, 1, true));

        mvc.perform(get("/api/search?keyword=spring&size=500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.content[0].slug").value("spring-caching"))
                .andExpect(jsonPath("$.users.content[0].username").value("springfan"))
                .andExpect(jsonPath("$.degraded").isEmpty());
    }

    @Test
    void degradesWhenOneSectionFailsAndFailsWhenAllDo() throws Exception {
        when(postClient.searchPosts(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(post()), 0, 10, 1, 1, true));
        when(userClient.searchUsers(any(), anyInt(), anyInt())).thenThrow(new RuntimeException("down"));

        mvc.perform(get("/api/search?keyword=spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.totalElements").value(1))
                .andExpect(jsonPath("$.degraded[0]").value("users"));

        mvc.perform(get("/api/search?keyword=spring&type=USER")).andExpect(status().isServiceUnavailable());
        verify(postClient, times(1)).searchPosts(any(), any(), anyInt(), anyInt());
    }

    @Test
    void rejectsBadInput() throws Exception {
        mvc.perform(get("/api/search")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/search").param("keyword", "   ")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/search?keyword=x&type=NOPE")).andExpect(status().isBadRequest());
    }
}
