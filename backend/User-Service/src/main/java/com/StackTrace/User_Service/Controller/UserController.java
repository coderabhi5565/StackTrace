package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.UserService;
import com.StackTrace.User_Service.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService us;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id){
        return ResponseEntity.ok(us.getUser(id));
    }

        @PutMapping("/me/profile")
        public ResponseEntity<UserProfileResponse> updateProfile(
                @RequestBody UpdateProfileRequest request) {

            UserProfileResponse response =
                    us.update(request);

            return ResponseEntity.ok(response);
        }

        @PutMapping("/points")
        public ResponseEntity<UserProfileResponse> updatepoints(@Valid @RequestBody UpdatePointsRequest u){
        return ResponseEntity.ok(us.updatepoints(u));
        }

    @PostMapping("/{targetId}/follow")
    public ResponseEntity<ApiResponse> followUser(
            @PathVariable Long targetId) {

        return ResponseEntity.ok(
                us.followUser(targetId)
        );
    }

    @DeleteMapping("/{targetId}/follow")
    public ResponseEntity<ApiResponse> unfollowUser(
            @PathVariable Long targetId) {

        return ResponseEntity.ok(
                us.unfollowUser(targetId)
        );
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserSummaryResponse>> getFollowers(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                us.getFollowers(id)
        );
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserSummaryResponse>> getFollowing(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                us.getFollowing(id)
        );
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>>
    getLeaderboard() {

        return ResponseEntity.ok(
                us.getLeaderboard()
        );
    }
}
