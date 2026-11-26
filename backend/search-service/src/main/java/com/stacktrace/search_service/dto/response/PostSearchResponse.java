package com.stacktrace.search_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/** Mirrors post-service's PostSummaryResponse. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostSearchResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String coverImageUrl;
    private UserSearchResponse author;
    private List<String> tags;
    private int readingTime;
    private int likeCount;
    private int commentCount;
    private Instant publishedAt;
}
