package com.StackTrace.User_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    private int rank;

    private Long id;

    private String username;

    private String name;

    private String avatarUrl;

    private int points;
}
