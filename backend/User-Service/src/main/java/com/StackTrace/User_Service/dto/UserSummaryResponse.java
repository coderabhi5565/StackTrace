package com.StackTrace.User_Service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {

    private Long id;

    private String name;

    private String username;

    private String avatarUrl;
}