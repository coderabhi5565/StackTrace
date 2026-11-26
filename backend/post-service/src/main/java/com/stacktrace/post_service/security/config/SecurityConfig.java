package com.stacktrace.post_service.security.config;

import com.stacktrace.post_service.security.filter.InternalApiFilter;
import com.stacktrace.post_service.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalApiFilter internalApiFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/internal/**").permitAll() // guarded by InternalApiFilter
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/me", "/api/posts/bookmarks",
                                "/api/posts/feed/following").authenticated()
                        // drafts are visible to their author only; checked in the service
                        .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/tags/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> write(res, 401, "Unauthorized", "Authentication required", req.getRequestURI()))
                        .accessDeniedHandler((req, res, ex) -> write(res, 403, "Forbidden", "Access denied", req.getRequestURI())))
                .addFilterBefore(internalApiFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter f) {
        FilterRegistrationBean<JwtAuthenticationFilter> r = new FilterRegistrationBean<>(f);
        r.setEnabled(false);
        return r;
    }

    @Bean
    FilterRegistrationBean<InternalApiFilter> internalFilterRegistration(InternalApiFilter f) {
        FilterRegistrationBean<InternalApiFilter> r = new FilterRegistrationBean<>(f);
        r.setEnabled(false);
        return r;
    }

    private static void write(HttpServletResponse res, int status, String error, String message, String path)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status + ",\"error\":\""
                + error + "\",\"message\":\"" + message + "\",\"path\":\"" + path.replace("\"", "") + "\"}");
    }
}
