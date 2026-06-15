package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.UserRepository;
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
}
