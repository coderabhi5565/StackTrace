package com.stacktrace.post_service.controller;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.request.SchedulePublishRequest;
import com.stacktrace.post_service.dto.request.UpdatePostRequest;
import com.stacktrace.post_service.dto.response.EngagementResponse;
import com.stacktrace.post_service.dto.response.PageResponse;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.dto.response.PostSummaryResponse;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.service.EngagementService;
import com.stacktrace.post_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final EngagementService engagementService;

    // ---------------------------------------------------------------- lists

    /** Latest published posts, optionally filtered by tag. */
    @GetMapping
    public PageResponse<PostSummaryResponse> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag) {
        return postService.getAllPublishedPosts(page, size, tag);
    }

    @GetMapping("/trending")
    public PageResponse<PostSummaryResponse> trending(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return postService.getTrendingPosts(page, size);
    }

    @GetMapping("/feed/following")
    public PageResponse<PostSummaryResponse> followingFeed(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return postService.getFollowingFeed(page, size);
    }

    @GetMapping("/user/{authorId}")
    public PageResponse<PostSummaryResponse> byAuthor(@PathVariable Long authorId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return postService.getPostsByAuthor(authorId, page, size);
    }

    /** The current user's posts (all statuses unless filtered). */
    @GetMapping("/me")
    public PageResponse<PostSummaryResponse> getMyPosts(@RequestParam(required = false) PostStatus status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return postService.getMyPosts(status, page, size);
    }

    @GetMapping("/bookmarks")
    public PageResponse<PostSummaryResponse> bookmarks(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return engagementService.myBookmarks(page, size);
    }

    // ---------------------------------------------------------------- single post

    @GetMapping("/{slug}")
    public PostResponse getPostBySlug(@PathVariable String slug) {
        return postService.getPostBySlug(slug);
    }

    @GetMapping("/id/{postId}")
    public PostResponse getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostRequest request) {
        return postService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/publish")
    public PostResponse publishPost(@PathVariable Long postId) {
        return postService.publishPost(postId);
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Void> schedulePublish(@PathVariable Long id,
                                                @Valid @RequestBody SchedulePublishRequest request) {
        postService.schedulePublish(id, request.getPublishAt());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel-schedule")
    public ResponseEntity<Void> cancelScheduledPublish(@PathVariable Long id) {
        postService.cancelScheduledPublish(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- engagement

    @GetMapping("/{postId:\\d+}/engagement")
    public EngagementResponse engagement(@PathVariable Long postId) {
        return engagementService.engagement(postId);
    }

    @PostMapping("/{postId}/like")
    public EngagementResponse like(@PathVariable Long postId) {
        return engagementService.like(postId);
    }

    @DeleteMapping("/{postId}/like")
    public EngagementResponse unlike(@PathVariable Long postId) {
        return engagementService.unlike(postId);
    }

    @PostMapping("/{postId}/bookmark")
    public EngagementResponse bookmark(@PathVariable Long postId) {
        return engagementService.bookmark(postId);
    }

    @DeleteMapping("/{postId}/bookmark")
    public EngagementResponse removeBookmark(@PathVariable Long postId) {
        return engagementService.removeBookmark(postId);
    }
}
