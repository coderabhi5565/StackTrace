package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.UserService;
import com.StackTrace.User_Service.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService us;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(us.register(r));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest l) {
        return ResponseEntity.ok(us.login(l));
    }

    /** Exchanges a refresh token for a new access + refresh token pair (rotation). */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest r) {
        return ResponseEntity.ok(us.refresh(r.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest r) {
        us.logout(r.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
