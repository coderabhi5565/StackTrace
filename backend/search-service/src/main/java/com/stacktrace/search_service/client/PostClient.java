package com.stacktrace.search_service.client;

import com.stacktrace.search_service.dto.response.PageResponse;
import com.stacktrace.search_service.dto.response.PostSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "post-service", url = "${post-service.url}")
public interface PostClient {

    @GetMapping("/internal/posts/search")
    PageResponse<PostSearchResponse> searchPosts(@RequestParam("keyword") String keyword,
                                                 @RequestParam(value = "tag", required = false) String tag,
                                                 @RequestParam("page") int page,
                                                 @RequestParam("size") int size);
}
