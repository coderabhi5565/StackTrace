package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.UserService;
import com.StackTrace.User_Service.dto.LoginRequest;
import com.StackTrace.User_Service.dto.LoginResponse;
import com.StackTrace.User_Service.dto.RegisterRequest;
import com.StackTrace.User_Service.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService us;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest r){
        RegisterResponse rp = us.register(r);
       return ResponseEntity.ok(rp);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest l){
        return ResponseEntity.ok(us.login(l));
    }
}
