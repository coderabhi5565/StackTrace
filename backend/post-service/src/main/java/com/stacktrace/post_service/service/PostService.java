package com.stacktrace.post_service.service;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.request.UpdatePostRequest;
import com.stacktrace.post_service.dto.response.PageResponse;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.dto.response.PostSummaryResponse;
import com.stacktrace.post_service.dto.response.TagResponse;
import com.stacktrace.post_service.enums.PostStatus;

import java.time.Instant;
import java.util.List;

public interface PostService {

    PostResponse createPost(CreatePostRequest request);

    PostResponse getPostBySlug(String slug);

    PostResponse getPostById(Long id);

    PageResponse<PostSummaryResponse> getAllPublishedPosts(int page, int size, String tag);

    PageResponse<PostSummaryResponse> getTrendingPosts(int page, int size);

    PageResponse<PostSummaryResponse> getFollowingFeed(int page, int size);

    PageResponse<PostSummaryResponse> getPostsByAuthor(Long authorId, int page, int size);

    PostResponse updatePost(Long postId, UpdatePostRequest request);

    void deletePost(Long postId);

    PostResponse publishPost(Long postId);

    PageResponse<PostSummaryResponse> getMyPosts(PostStatus status, int page, int size);

    void schedulePublish(Long postId, Instant publishAt);

    void cancelScheduledPublish(Long postId);

    boolean existsById(Long id);

    PageResponse<PostSummaryResponse> searchPosts(String keyword, String tag, int page, int size);

    Long getAuthorId(Long postId);

    void adjustCommentCount(Long postId, int delta);

    List<TagResponse> getPopularTags(int limit);
}
