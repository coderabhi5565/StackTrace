package com.stacktrace.search_service.dto.response;

import java.util.List;

/**
 * @param degraded sections that could not be fetched (e.g. ["users"]) — the rest is still returned
 */
public record SearchResponse(PageResponse<PostSearchResponse> posts,
                             PageResponse<UserSearchResponse> users,
                             List<String> degraded) {
}
