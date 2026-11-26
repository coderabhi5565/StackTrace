package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.BadgeService;
import com.StackTrace.User_Service.Service.UserService;
import com.StackTrace.User_Service.dto.PagedResponse;
import com.StackTrace.User_Service.dto.PointsDeltaRequest;
import com.StackTrace.User_Service.dto.UserProfileResponse;
import com.StackTrace.User_Service.dto.UserSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Service-to-service API. Protected by InternalApiFilter (X-Internal-Token) and never routed by the gateway.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private static final int MAX_BATCH = 100;

    private final UserService us;
    private final BadgeService badgeService;

    @GetMapping("/users/batch")
    public List<UserSummaryResponse> batch(@RequestParam Set<Long> ids) {
        if (ids.size() > MAX_BATCH) {
            throw new IllegalArgumentException("At most " + MAX_BATCH + " ids per request");
        }
        return us.findByIds(ids);
    }

    @GetMapping("/users/search")
    public PagedResponse<UserSummaryResponse> search(@RequestParam String keyword,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return us.searchUsers(keyword, page, size);
    }

    @GetMapping("/users/{id}/following-ids")
    public List<Long> followingIds(@PathVariable Long id) {
        return us.getFollowingIds(id);
    }

    @PostMapping("/users/{id}/points")
    public UserProfileResponse addPoints(@PathVariable Long id, @Valid @RequestBody PointsDeltaRequest request) {
        return us.addPoints(id, request.getDelta());
    }

    @PostMapping("/badges/users/{userId}/{badgeId}")
    public ResponseEntity<Void> awardBadge(@PathVariable Long userId, @PathVariable Long badgeId) {
        badgeService.awardBadge(userId, badgeId);
        return ResponseEntity.noContent().build();
    }
}
