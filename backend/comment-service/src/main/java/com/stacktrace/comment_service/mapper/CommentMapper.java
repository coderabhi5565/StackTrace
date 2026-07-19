package com.stacktrace.comment_service.mapper;

import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public Comment toEntity(CreateCommentRequest request,
                            Long authorId,
                            Comment parentComment) {

        return Comment.builder()
                .content(request.getContent())
                .authorId(authorId)
                .postId(request.getPostId())
                .parentComment(parentComment)
                .build();
    }

    public CommentResponse toResponse(Comment comment) {

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .postId(comment.getPostId())
                .parentCommentId(
                        comment.getParentComment() != null
                                ? comment.getParentComment().getId()
                                : null
                )
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public void updateEntity(Comment comment,
                             UpdateCommentRequest request) {

        comment.setContent(request.getContent());
    }
}