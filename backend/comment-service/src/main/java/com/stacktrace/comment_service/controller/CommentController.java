package com.stacktrace.comment_service.controller;

import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.service.CommentService;
import com.stacktrace.comment_service.security.CurrentUserUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserUtil currentUserUtil;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @Valid @RequestBody CreateCommentRequest request) {

        Long userId = currentUserUtil.getCurrentUserId();

        return commentService.createComment(request, userId);
    }

    @PutMapping("/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        Long userId = currentUserUtil.getCurrentUserId();

        return commentService.updateComment(commentId, request, userId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {

        Long userId = currentUserUtil.getCurrentUserId();

        commentService.deleteComment(commentId, userId);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> getCommentsByPost(
            @PathVariable Long postId) {

        return commentService.getCommentsByPost(postId);
    }

    @GetMapping("/{commentId}/replies")
    public List<CommentResponse> getReplies(
            @PathVariable Long commentId) {

        return commentService.getReplies(commentId);
    }

}