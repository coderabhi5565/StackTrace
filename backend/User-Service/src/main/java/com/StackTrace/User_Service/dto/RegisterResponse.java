package com.StackTrace.User_Service.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
}
