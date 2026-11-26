package com.stacktrace.post_service.dto.response;

import java.io.Serializable;

public record TagResponse(String name, Long postCount) implements Serializable {
}
