package com.stacktrace.comment_service.dto.response;

public record AuthorSummary(Long id, String name, String username, String avatarUrl) {

    public static AuthorSummary unknown(Long id) {
        return new AuthorSummary(id, "Unknown user", "unknown", null);
    }
}
