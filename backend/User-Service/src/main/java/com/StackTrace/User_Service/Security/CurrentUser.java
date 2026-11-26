package com.StackTrace.User_Service.Security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        return idIfPresent().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Authentication required"));
    }

    public static Optional<Long> idIfPresent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long id) {
            return Optional.of(id);
        }
        return Optional.empty();
    }
}
