package com.stacktrace.comment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "post-service",
        url = "${post-service.url}"
)
public interface PostClient {

    @GetMapping("/internal/posts/{id}/exists")
    boolean exists(@PathVariable Long id);
}