package com.stacktrace.post_service.service;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.request.UpdatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponse createPost(CreatePostRequest request);

    PostResponse getPostBySlug(String slug);

    Page<PostResponse> getAllPublishedPosts(Pageable pageable);
    PostResponse updatePost(
            Long postId,
            UpdatePostRequest request
    );
    void deletePost(Long postId);
}