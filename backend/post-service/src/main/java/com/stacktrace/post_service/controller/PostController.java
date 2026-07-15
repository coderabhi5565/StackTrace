package com.stacktrace.post_service.controller;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
         @Valid @RequestBody CreatePostRequest request
    ) {

        PostResponse response = postService.createPost(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(
            @PathVariable String slug
    ) {

        PostResponse response = postService.getPostBySlug(slug);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPublishedPosts(
            Pageable pageable
    ) {

        Page<PostResponse> response =
                postService.getAllPublishedPosts(pageable);

        return ResponseEntity.ok(response);
    }
}