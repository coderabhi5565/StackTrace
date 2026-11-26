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

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me() {
        return ResponseEntity.ok(us.me());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(us.getUser(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(us.getUserByUsername(username));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(us.update(request));
    }

    @PostMapping("/{targetId}/follow")
    public ResponseEntity<ApiResponse> followUser(@PathVariable Long targetId) {
        return ResponseEntity.ok(us.followUser(targetId));
    }

    @DeleteMapping("/{targetId}/follow")
    public ResponseEntity<ApiResponse> unfollowUser(@PathVariable Long targetId) {
        return ResponseEntity.ok(us.unfollowUser(targetId));
    }

    @GetMapping("/{id}/follow-status")
    public ResponseEntity<FollowStatusResponse> followStatus(@PathVariable Long id) {
        return ResponseEntity.ok(us.followStatus(id));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getFollowers(
            @PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(us.getFollowers(id, page, size));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getFollowing(
            @PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(us.getFollowing(id, page, size));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<PagedResponse<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(us.getLeaderboard(page, size));
    }

    @PostMapping("/me/skills")
    public ResponseEntity<SkillResponse> addSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(us.addSkill(request));
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<SkillResponse>> getSkills(@PathVariable Long id) {
        return ResponseEntity.ok(us.getSkills(id));
    }

    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<ApiResponse> deleteSkill(@PathVariable Long skillId) {
        return ResponseEntity.ok(us.deleteSkill(skillId));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(us.searchUsers(keyword, page, size));
    }
}
