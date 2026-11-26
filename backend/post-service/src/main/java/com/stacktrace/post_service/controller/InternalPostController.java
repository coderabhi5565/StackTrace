package com.stacktrace.post_service.controller;

import com.stacktrace.post_service.dto.request.CountDeltaRequest;
import com.stacktrace.post_service.dto.response.AuthorResponse;
import com.stacktrace.post_service.dto.response.PageResponse;
import com.stacktrace.post_service.dto.response.PostSummaryResponse;
import com.stacktrace.post_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Service-to-service API (X-Internal-Token). Never routed by the gateway. */
@RestController
@RequestMapping("/internal/posts")
@RequiredArgsConstructor
public class InternalPostController {

    private final PostService postService;

    /** True only for existing, published, non-deleted posts (the ones that can be commented on). */
    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return postService.existsById(id);
    }

    @GetMapping("/search")
    public PageResponse<PostSummaryResponse> searchPosts(@RequestParam String keyword,
                                                         @RequestParam(required = false) String tag,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return postService.searchPosts(keyword, tag, page, size);
    }

    @GetMapping("/{postId}/author")
    public AuthorResponse getAuthorId(@PathVariable Long postId) {
        return AuthorResponse.builder().authorId(postService.getAuthorId(postId)).build();
    }

    @PostMapping("/{postId}/comment-count")
    public ResponseEntity<Void> adjustCommentCount(@PathVariable Long postId,
                                                   @Valid @RequestBody CountDeltaRequest request) {
        postService.adjustCommentCount(postId, request.getDelta());
        return ResponseEntity.noContent().build();
    }
}
