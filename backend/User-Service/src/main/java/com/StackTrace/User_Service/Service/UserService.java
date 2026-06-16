package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.UserRepository;
import com.StackTrace.User_Service.dto.LoginRequest;
import com.StackTrace.User_Service.dto.LoginResponse;
import com.StackTrace.User_Service.dto.RegisterRequest;
import com.StackTrace.User_Service.dto.RegisterResponse;
import com.StackTrace.User_Service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder bp;
    private final UserRepository up;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest rq){
        if(up.existsByEmail(rq.getEmail())){
            throw new RuntimeException("Email Already Registered");
        }
        String hp = bp.encode(rq.getPassword());
        User u = User.builder().name(rq.getName()).email(rq.getEmail()).username(rq.getUsername()).password(rq.getPassword()).build();
        User ret = up.save(u);
        RegisterResponse rp = new RegisterResponse();
        rp.setId(ret.getId());
        rp.setName(ret.getName());
        rp.setEmail(ret.getEmail());
        rp.setEmail(ret.getName());
        return rp;
    }

    public LoginResponse login(LoginRequest l) {
        if (!up.existsByEmail(l.getEmail())) {
            throw new RuntimeException("Invalid Credentials");
        }
        User user = up.findByEmail(l.getEmail()).get();
        if (!bp.matches(l.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }
        String token = jwtService.generateToken(l.getEmail());
        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

    }
}
