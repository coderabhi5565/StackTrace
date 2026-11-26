package com.stacktrace.post_service.security.util;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        return currentUserIdIfPresent().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Authentication required"));
    }

    /** For endpoints that are public but personalise output when a valid token is present. */
    public static Optional<Long> currentUserIdIfPresent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long id) {
            return Optional.of(id);
        }
        return Optional.empty();
    }
}
