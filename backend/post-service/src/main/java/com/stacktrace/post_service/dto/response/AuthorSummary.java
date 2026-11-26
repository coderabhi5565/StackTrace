package com.stacktrace.post_service.dto.response;

import java.io.Serializable;

/** Author details resolved from user-service. */
public record AuthorSummary(Long id, String name, String username, String avatarUrl) implements Serializable {

    public static AuthorSummary unknown(Long id) {
        return new AuthorSummary(id, "Unknown author", "unknown", null);
    }
}
