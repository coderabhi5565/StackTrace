package com.stacktrace.post_service.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Only StackTrace services (holding INTERNAL_API_TOKEN) may call /internal/**. */
@Component
public class InternalApiFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Token";

    private final byte[] expected;

    public InternalApiFilter(@Value("${internal.api-token}") String token) {
        if (token == null || token.length() < 16) {
            throw new IllegalStateException("INTERNAL_API_TOKEN must be at least 16 characters");
        }
        this.expected = token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (provided == null || !MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Internal endpoint\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
