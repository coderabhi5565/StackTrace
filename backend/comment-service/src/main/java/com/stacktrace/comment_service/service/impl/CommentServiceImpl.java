package com.stacktrace.comment_service.service.impl;

import com.stacktrace.comment_service.client.PostClient;
import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.entity.Comment;
import com.stacktrace.comment_service.exception.CommentNotFoundException;
import com.stacktrace.comment_service.exception.InvalidCommentRequestException;
import com.stacktrace.comment_service.exception.PostNotFoundException;
import com.stacktrace.comment_service.exception.UnauthorizedCommentAccessException;
import com.stacktrace.comment_service.mapper.CommentMapper;
import com.stacktrace.comment_service.repository.CommentRepository;
import com.stacktrace.comment_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostClient postClient;

    @Override
    public CommentResponse createComment(CreateCommentRequest request, Long userId) {

        if (!postClient.exists(request.getPostId())) {
            throw new PostNotFoundException("Post not found.");
        }
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository
                    .findByIdAndDeletedAtIsNull(request.getParentCommentId())
                    .orElseThrow(() ->
                            new CommentNotFoundException("Parent comment not found."));

            if (!parentComment.getPostId().equals(request.getPostId())) {
                throw new InvalidCommentRequestException(
                        "Parent comment does not belong to this post."
                );
            }
        }

        Comment comment = commentMapper.toEntity(
                request,
                userId,
                parentComment
        );

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toResponse(savedComment);
    }

    @Override
    public CommentResponse updateComment(Long commentId,
                                         UpdateCommentRequest request,
                                         Long userId) {

        Comment comment = commentRepository
                .findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() ->
                        new CommentNotFoundException("Comment not found."));

        if (!comment.getAuthorId().equals(userId)) {
            throw new UnauthorizedCommentAccessException(
                    "You are not allowed to update this comment."
            );
        }
        commentMapper.updateEntity(comment, request);
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toResponse(updatedComment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {

    }

    @Override
    public List<CommentResponse> getCommentsByPost(Long postId) {
        return List.of();
    }

    @Override
    public List<CommentResponse> getReplies(Long commentId) {
        return List.of();
    }
}