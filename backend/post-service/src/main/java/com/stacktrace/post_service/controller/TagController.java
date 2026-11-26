package com.stacktrace.post_service.controller;

import com.stacktrace.post_service.dto.response.TagResponse;
import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final PostService postService;

    /** Most used tags across published posts. */
    @GetMapping
    public List<TagResponse> popular(@RequestParam(defaultValue = "20") int limit) {
        return postService.getPopularTags(limit);
    }
}
