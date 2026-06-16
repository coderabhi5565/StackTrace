package com.StackTrace.User_Service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequest {
    private String bio,avatarUrl,location;
}
