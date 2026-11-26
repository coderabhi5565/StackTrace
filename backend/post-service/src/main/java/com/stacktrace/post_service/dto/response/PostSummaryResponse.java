package com.stacktrace.post_service.dto.response;

import com.stacktrace.post_service.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/** Card-sized representation used by every list endpoint (no content body). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse implements Serializable {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String coverImageUrl;
    private AuthorSummary author;
    private PostStatus status;
    private List<String> tags;
    private int readingTime;
    private int likeCount;
    private int commentCount;
    private Instant scheduledAt;
    private Instant publishedAt;
    private Instant createdAt;
}
