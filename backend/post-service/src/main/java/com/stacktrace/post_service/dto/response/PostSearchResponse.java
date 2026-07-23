package com.stacktrace.post_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSearchResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String coverImageUrl;

}