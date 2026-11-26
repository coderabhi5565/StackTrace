package com.StackTrace.User_Service.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 1, max = 100, message = "Name must be 1-100 characters")
    private String name;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    @Size(max = 255)
    @Pattern(regexp = "^(https://.*)?$", message = "Avatar URL must use https")
    private String avatarUrl;

    @Size(max = 100)
    private String location;
}
