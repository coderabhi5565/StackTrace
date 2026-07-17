package com.stacktrace.post_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500)
    private String coverImageUrl;

    @NotEmpty(message = "At least one tag is required")
    private Set<Long> tagIds;
}