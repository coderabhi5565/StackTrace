package com.StackTrace.User_Service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardResponse {

    private int rank;

    private String username;

    private String name;

    private int points;
}
