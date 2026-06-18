package com.StackTrace.User_Service.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequest {
    @Size(max = 500,
            message = "Bio cannot exceed 500 characters")
    private String bio;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 100)
    private String location;
}
