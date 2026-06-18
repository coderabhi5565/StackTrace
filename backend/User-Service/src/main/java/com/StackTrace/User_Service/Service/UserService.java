package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.FollowRepository;
import com.StackTrace.User_Service.Repository.UserRepository;
import com.StackTrace.User_Service.dto.*;
import com.StackTrace.User_Service.exception.*;
import com.StackTrace.User_Service.model.Follow;
import com.StackTrace.User_Service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder bp;
    private final UserRepository up;
    private final JwtService jwtService;
    private final FollowRepository fp;

    public RegisterResponse register(RegisterRequest rq){
        if(up.existsByEmail(rq.getEmail())){
            throw new EmailAlreadyExistsException("Email Already Registered");
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
            throw new UserNotFoundException("User Does Not Exist");
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

    public ApiResponse followUser(Long targetId) {
        String email = getCurrentUserEmail();
        User currentUser = up.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        User targetUser = up.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Target user not found"));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new SelfFollowException("You cannot follow yourself");
        }

        if (fp.existsByFollowerIdAndFollowingId(
                currentUser.getId(),
                targetUser.getId())) {

            throw new AlreadyFollowingException("Already following this user");
        }

        Follow follow = Follow.builder()
                .followerId(currentUser.getId())
                .followingId(targetUser.getId())
                .build();

        fp.save(follow);

        return ApiResponse.builder()
                .success(true)
                .message("User followed successfully")
                .build();
    }

    public ApiResponse unfollowUser(Long targetId) {
        String email = getCurrentUserEmail();
        User currentUser = up.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        up.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("Target user not found"));

        Follow follow = fp.findByFollowerIdAndFollowingId(
                        currentUser.getId(),
                        targetId)
                .orElseThrow(() ->
                        new BusinessException("Follow relationship not found"));

        fp.delete(follow);

        return ApiResponse.builder()
                .success(true)
                .message("User unfollowed successfully")
                .build();
    }

    public List<UserSummaryResponse> getFollowers(Long id) {
        up.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Follow> follows = fp.findByFollowingId(id);

        List<Long> followerIds = follows.stream()
                .map(Follow::getFollowerId)
                .toList();

        List<User> users = up.findByIdIn(followerIds);

        return users.stream()
                .map(user -> UserSummaryResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .toList();
    }


    public List<UserSummaryResponse> getFollowing(Long id){
        up.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Follow> follows = fp.findByFollowerId(id);

        List<Long> followingIds = follows.stream()
                .map(Follow::getFollowingId)
                .toList();

        List<User> users = up.findByIdIn(followingIds);

        return users.stream()
                .map(user -> UserSummaryResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .toList();
    }

    public List<LeaderboardResponse> getLeaderboard() {

        List<User> users =
                up.findTop10ByOrderByPointsDesc();

        List<LeaderboardResponse> leaderboard =
                new ArrayList<>();

        int rank = 1;

        for(User user : users) {

            leaderboard.add(
                    LeaderboardResponse.builder()
                            .rank(rank++)
                            .username(user.getUsername())
                            .name(user.getName())
                            .points(user.getPoints())
                            .build()
            );
        }

        return leaderboard;
    }
}
