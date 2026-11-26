package com.stacktrace.comment_service.mapper;

import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.AuthorSummary;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CommentMapper {

    public Comment toEntity(CreateCommentRequest request, Long authorId, Comment parentComment) {
        return Comment.builder()
                .content(request.getContent().trim())
                .authorId(authorId)
                .postId(request.getPostId())
                .parentComment(parentComment)
                .rootCommentId(parentComment == null ? null : parentComment.threadId())
                .build();
    }

    public CommentResponse toResponse(Comment comment, Map<Long, AuthorSummary> authors) {
        boolean deleted = comment.getDeletedAt() != null;
        return CommentResponse.builder()
                .id(comment.getId())
                .content(deleted ? null : comment.getContent())
                .authorId(deleted ? null : comment.getAuthorId())
                .author(deleted ? null : authors.getOrDefault(comment.getAuthorId(),
                        AuthorSummary.unknown(comment.getAuthorId())))
                .postId(comment.getPostId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .deleted(deleted)
                .edited(!deleted && comment.getUpdatedAt() != null && comment.getCreatedAt() != null
                        && comment.getUpdatedAt().isAfter(comment.getCreatedAt().plusSeconds(1)))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public void updateEntity(Comment comment, UpdateCommentRequest request) {
        comment.setContent(request.getContent().trim());
    }
}
