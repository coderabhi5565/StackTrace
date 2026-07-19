package com.stacktrace.post_service.controller;

import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/posts")
@RequiredArgsConstructor
public class InternalPostController {

    private final PostService postService;

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return postService.existsById(id);
    }

}