package com.stacktrace.post_service.service;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.request.UpdatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.stacktrace.post_service.dto.response.PostSearchResponse;

import java.time.Instant;
import java.util.List;

public interface PostService {

    PostResponse createPost(CreatePostRequest request);

    PostResponse getPostBySlug(String slug);

    Page<PostResponse> getAllPublishedPosts(Pageable pageable);
    PostResponse updatePost(
            Long postId,
            UpdatePostRequest request
    );
    void deletePost(Long postId);
    void publishPost(Long postId);

    Page<PostResponse> getMyPosts(Pageable pageable);
    void schedulePublish(Long postId, Instant publishAt);
    void cancelScheduledPublish(Long postId);

    boolean existsById(Long id);

    List<PostSearchResponse> searchPosts(String keyword);
}