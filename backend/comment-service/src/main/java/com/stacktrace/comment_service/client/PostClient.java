package com.stacktrace.comment_service.client;

import com.stacktrace.comment_service.dto.response.AuthorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "post-service",
        url = "${post-service.url}"
)
public interface PostClient {

    @GetMapping("/internal/posts/{postId}/exists")
    boolean exists(@PathVariable Long postId);

    @GetMapping("/internal/posts/{postId}/author")
    AuthorResponse getAuthorId(
            @PathVariable Long postId
    );
}