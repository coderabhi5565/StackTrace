package com.stacktrace.comment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String content;

    private Long postId;

    private Long parentCommentId;
}