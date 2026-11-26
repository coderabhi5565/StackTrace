package com.stacktrace.post_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Internal: author id of a post (used by comment-service for notifications). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {

    private Long authorId;

}
