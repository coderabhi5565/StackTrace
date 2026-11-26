package com.stacktrace.comment_service.controller;

import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.dto.response.PageResponse;
import com.stacktrace.comment_service.security.CurrentUserUtil;
import com.stacktrace.comment_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserUtil currentUserUtil;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(request, currentUserUtil.getCurrentUserId());
    }

    @PutMapping("/{commentId}")
    public CommentResponse updateComment(@PathVariable Long commentId,
                                         @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.updateComment(commentId, request, currentUserUtil.getCurrentUserId());
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, currentUserUtil.getCurrentUserId());
    }

    /** Top-level comments (newest first) with nested replies. */
    @GetMapping("/post/{postId}")
    public PageResponse<CommentResponse> getCommentsByPost(@PathVariable Long postId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return commentService.getCommentsByPost(postId, page, size);
    }

    @GetMapping("/{commentId}/replies")
    public PageResponse<CommentResponse> getReplies(@PathVariable Long commentId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return commentService.getReplies(commentId, page, size);
    }
}
