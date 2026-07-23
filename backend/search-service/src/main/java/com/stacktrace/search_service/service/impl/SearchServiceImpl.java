package com.stacktrace.search_service.service.impl;

import com.stacktrace.search_service.SearchType;
import com.stacktrace.search_service.client.PostClient;
import com.stacktrace.search_service.client.UserClient;
import com.stacktrace.search_service.dto.response.PostSearchResponse;
import com.stacktrace.search_service.dto.response.SearchResponse;
import com.stacktrace.search_service.dto.response.UserSearchResponse;
import com.stacktrace.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final PostClient postClient;
    private final UserClient userClient;

    @Override
    public SearchResponse search(String keyword, SearchType type) {

        List<PostSearchResponse> posts = Collections.emptyList();
        List<UserSearchResponse> users = Collections.emptyList();

        switch (type) {

            case POST:
                posts = postClient.searchPosts(keyword);
                break;

            case USER:
                users = userClient.searchUsers(keyword);
                break;

            case ALL:
            default:
                posts = postClient.searchPosts(keyword);
                users = userClient.searchUsers(keyword);
        }

        return SearchResponse.builder()
                .posts(posts)
                .users(users)
                .build();
    }
}