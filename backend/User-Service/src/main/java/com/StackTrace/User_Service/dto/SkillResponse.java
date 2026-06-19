package com.StackTrace.User_Service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillResponse {

    private Long id;

    private String skillName;
}