package com.stacktrace.search_service.client;

import com.stacktrace.search_service.dto.response.PostSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "post-service",
        url = "${post-service.url}"
)
public interface PostClient {

    @GetMapping("/internal/posts/search")
    List<PostSearchResponse> searchPosts(
            @RequestParam String keyword
    );
}