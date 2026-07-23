package com.stacktrace.post_service.controller;

import com.stacktrace.post_service.dto.response.PostSearchResponse;
import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/posts")
@RequiredArgsConstructor
public class InternalPostController {

    private final PostService postService;

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return postService.existsById(id);
    }

    @GetMapping("/internal/posts/search")
    public ResponseEntity<List<PostSearchResponse>> searchPosts(
            @RequestParam String keyword
    ){
        return ResponseEntity.ok(postService.searchPosts(keyword));
    }
}