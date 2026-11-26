package com.stacktrace.post_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 250, message = "Title cannot exceed 250 characters")
    private String title;

    /** Markdown. Rendered (and sanitized) by the client. */
    @NotBlank(message = "Content is required")
    @Size(max = 100_000, message = "Content cannot exceed 100,000 characters")
    private String content;

    /** Optional summary; derived from the content when blank. */
    @Size(max = 300, message = "Excerpt cannot exceed 300 characters")
    private String excerpt;

    @Size(max = 500, message = "Cover image URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https://.*)?$", message = "Cover image URL must use https")
    private String coverImageUrl;

    @NotEmpty(message = "At least one tag is required")
    @Size(max = 5, message = "At most 5 tags")
    private List<@NotBlank @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9+#.-]{0,29}$",
            message = "Tags are 1-30 chars: letters, digits, + # . -") String> tags = new ArrayList<>();

    /** true = publish immediately, false/absent = save as draft. */
    private boolean publish;
}
