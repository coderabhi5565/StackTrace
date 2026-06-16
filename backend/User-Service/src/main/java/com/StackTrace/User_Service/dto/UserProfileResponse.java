package com.StackTrace.User_Service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String name, username, bio,  avatarUrl, location;
    private int points;
    private LocalDateTime date;
}
