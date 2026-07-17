package com.stacktrace.post_service.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResponse {

    private String imageUrl;
}