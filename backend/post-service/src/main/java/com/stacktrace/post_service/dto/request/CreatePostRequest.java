package com.stacktrace.post_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CreatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 250, message = "Title cannot exceed 250 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500, message = "Cover image URL cannot exceed 500 characters")
    private String coverImageUrl;

    private Set<Long> tagIds = new HashSet<>();

}