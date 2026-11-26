package com.stacktrace.search_service.service.impl;

import com.stacktrace.search_service.SearchType;
import com.stacktrace.search_service.client.PostClient;
import com.stacktrace.search_service.client.UserClient;
import com.stacktrace.search_service.dto.response.PageResponse;
import com.stacktrace.search_service.dto.response.PostSearchResponse;
import com.stacktrace.search_service.dto.response.SearchResponse;
import com.stacktrace.search_service.dto.response.UserSearchResponse;
import com.stacktrace.search_service.exception.ServiceUnavailableException;
import com.stacktrace.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Fans out to post-service (Postgres full-text search) and user-service (pg_trgm) in parallel.
 * A failing section degrades to empty instead of failing the whole search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    public static final int MAX_PAGE_SIZE = 50;
    private static final long TIMEOUT_MS = 4000;
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final PostClient postClient;
    private final UserClient userClient;

    @Override
    public SearchResponse search(String keyword, SearchType type, String tag, int page, int size) {
        String q = keyword.trim();
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        CompletableFuture<PageResponse<PostSearchResponse>> posts = type == SearchType.USER
                ? CompletableFuture.completedFuture(PageResponse.empty(p, s))
                : async(() -> postClient.searchPosts(q, blankToNull(tag), p, s));
        CompletableFuture<PageResponse<UserSearchResponse>> users = type == SearchType.POST
                ? CompletableFuture.completedFuture(PageResponse.empty(p, s))
                : async(() -> userClient.searchUsers(q, p, s));

        List<String> degraded = new ArrayList<>();
        PageResponse<PostSearchResponse> postPage = await(posts, "posts", degraded, p, s);
        PageResponse<UserSearchResponse> userPage = await(users, "users", degraded, p, s);

        int expected = type == SearchType.ALL ? 2 : 1;
        if (degraded.size() == expected) {
            throw new ServiceUnavailableException("Search is temporarily unavailable");
        }
        return new SearchResponse(postPage, userPage, degraded);
    }

    private static <T> CompletableFuture<T> async(Supplier<T> call) {
        return CompletableFuture.supplyAsync(call, EXECUTOR);
    }

    private static <T> PageResponse<T> await(CompletableFuture<PageResponse<T>> f, String name,
                                             List<String> degraded, int page, int size) {
        try {
            return f.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.warn("Search section '{}' failed: {}", name, e.getMessage());
        }
        f.cancel(true);
        degraded.add(name);
        return PageResponse.empty(page, size);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
