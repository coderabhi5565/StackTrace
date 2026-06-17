package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.FollowRepository;
import com.StackTrace.User_Service.Repository.UserRepository;
import com.StackTrace.User_Service.dto.*;
import com.StackTrace.User_Service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder bp;
    private final UserRepository up;
    private final JwtService jwtService;
    private final FollowRepository fp;

    public RegisterResponse register(RegisterRequest rq){
        if(up.existsByEmail(rq.getEmail())){
            throw new RuntimeException("Email Already Registered");
        }
        String hp = bp.encode(rq.getPassword());
        User u = User.builder().name(rq.getName()).email(rq.getEmail()).username(rq.getUsername()).password(bp.encode(rq.getPassword())).build();
        User ret = up.save(u);
        RegisterResponse rp = new RegisterResponse();
        rp.setId(ret.getId());
        rp.setName(ret.getName());
        rp.setEmail(ret.getEmail());
        rp.setUsername(ret.getUsername());
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

    public UserProfileResponse getUser(Long id) {
        User u = up.findById(id).get();
        if(u==null){
            throw new RuntimeException("User Does Not Exist");
        }
        UserProfileResponse ret = UserProfileResponse.builder()
                .id(id)
                .name(u.getName())
                .username(u.getUsername())
                .bio(u.getBio())
                .avatarUrl(u.getAvatarUrl())
                .points(u.getPoints())
                .location(u.getLocation())
                .build();
        return ret;
    }

    public UserProfileResponse update(UpdateProfileRequest ur) {
        String email = getCurrentUserEmail();
        User u = up.findByEmail(email).get();
        if(ur.getBio()!=null){
            u.setBio(ur.getBio());
        }
        if(ur.getAvatarUrl()!=null){
            u.setAvatarUrl(ur.getAvatarUrl());
        }
        if(ur.getLocation()!=null){
            u.setLocation(ur.getLocation());
        }
        up.save(u);
        UserProfileResponse ret = UserProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .username(u.getUsername())
                .bio(u.getBio())
                .avatarUrl(u.getAvatarUrl())
                .points(u.getPoints())
                .location(u.getLocation())
                .build();
        return ret;
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (String) authentication.getPrincipal();
    }

    public UserProfileResponse updatepoints(UpdatePointsRequest ups) {
        String email = getCurrentUserEmail();
        User u = up.findByEmail(email).get();
        u.setPoints(u.getPoints() + ups.getPoints());
        up.save(u);
        UserProfileResponse ret = UserProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .username(u.getUsername())
                .bio(u.getBio())
                .avatarUrl(u.getAvatarUrl())
                .points(u.getPoints())
                .location(u.getLocation())
                .build();
        return ret;
    }
}
