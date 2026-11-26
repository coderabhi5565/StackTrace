package com.stacktrace.post_service.dto.response;

/** Live, per-viewer counters for a post (never cached). */
public record EngagementResponse(Long postId, int likeCount, int commentCount, boolean liked, boolean bookmarked) {
}
