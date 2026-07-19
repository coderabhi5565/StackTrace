package com.stacktrace.comment_service.service;

import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(CreateCommentRequest request, Long userId);
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId);
    void deleteComment(Long commentId, Long userId);
    List<CommentResponse> getCommentsByPost(Long postId);
    List<CommentResponse> getReplies(Long commentId);

}