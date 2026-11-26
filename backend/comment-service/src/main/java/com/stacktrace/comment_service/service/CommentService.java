package com.stacktrace.comment_service.service;

import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.dto.response.PageResponse;

public interface CommentService {

    CommentResponse createComment(CreateCommentRequest request, Long userId);

    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId);

    void deleteComment(Long commentId, Long userId);

    /** Page of top-level comments, each with its full reply tree. */
    PageResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size);

    PageResponse<CommentResponse> getReplies(Long commentId, int page, int size);
}
