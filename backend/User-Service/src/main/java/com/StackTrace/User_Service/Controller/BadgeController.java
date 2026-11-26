package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.BadgeService;
import com.StackTrace.User_Service.dto.BadgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Read-only. Badges are awarded automatically (points thresholds) or via the internal API. */
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }
}
