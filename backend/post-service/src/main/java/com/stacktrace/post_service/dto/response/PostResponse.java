package com.stacktrace.post_service.dto.response;

import com.stacktrace.post_service.enums.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
@Builder
public class PostResponse {

    private Long id;

    private String title;

    private String slug;

    private String content;

    private String coverImageUrl;

    private Long authorId;

    private PostStatus status;

    private Set<String> tags;

    private Instant scheduledAt;

    private Instant publishedAt;

    private Instant createdAt;

    private Instant updatedAt;

}