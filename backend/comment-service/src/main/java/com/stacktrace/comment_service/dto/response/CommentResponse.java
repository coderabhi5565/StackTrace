package com.stacktrace.comment_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    /** null when deleted (tombstone kept so its replies stay in context). */
    private String content;
    private Long authorId;
    private AuthorSummary author;
    private Long postId;
    private Long parentCommentId;
    private boolean deleted;
    private boolean edited;
    private Instant createdAt;
    private Instant updatedAt;
    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();
}
