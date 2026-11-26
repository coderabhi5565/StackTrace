package com.StackTrace.User_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name, username, bio, avatarUrl, location;
    private int points;
    private long rank;
    private long followersCount;
    private long followingCount;
    private LocalDateTime joinedAt;
    private List<SkillResponse> skills;
    private List<BadgeResponse> badges;
}
