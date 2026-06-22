package com.StackTrace.User_Service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private LocalDateTime earnedAt;
}