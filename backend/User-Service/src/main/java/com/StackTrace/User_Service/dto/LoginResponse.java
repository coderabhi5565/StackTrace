package com.StackTrace.User_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private Long id;
    /** Short-lived access token (JWT). Kept as "token" for backwards compatibility. */
    private String token;
    private String refreshToken;
    /** Access token lifetime in seconds. */
    private long expiresIn;
    private String name;
    private String username;
    private String email;
}
