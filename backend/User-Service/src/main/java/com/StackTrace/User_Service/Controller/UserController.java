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
            @PathVariable Long id, @RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                us.getFollowers(id,page,size)
        );
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserSummaryResponse>> getFollowing(
            @PathVariable Long id, @RequestParam(defaultValue = 0)int page, @RequestParam(defaultValue = 0) int size) {

        return ResponseEntity.ok(
                us.getFollowing(id,size,page)
        );
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>>
    getLeaderboard() {

        return ResponseEntity.ok(
                us.getLeaderboard()
        );
    }

    @PostMapping("/me/skills")
    public ResponseEntity<SkillResponse> addSkill(
            @Valid
            @RequestBody SkillRequest request) {

        return ResponseEntity.ok(
                us.addSkill(request)
        );
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<SkillResponse>>
    getSkills(@PathVariable Long id) {

        return ResponseEntity.ok(
                us.getSkills(id)
        );
    }

    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<ApiResponse>
    deleteSkill(@PathVariable Long skillId) {

        return ResponseEntity.ok(
                us.deleteSkill(skillId)
        );
    }
}
