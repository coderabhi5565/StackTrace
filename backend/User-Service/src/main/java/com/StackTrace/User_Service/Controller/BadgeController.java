package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.BadgeService;
import com.StackTrace.User_Service.dto.BadgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @PostMapping(
            "/users/{userId}/{badgeId}"
    )
    public ResponseEntity<String> awardBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId
    ) {

        badgeService.awardBadge(
                userId,
                badgeId
        );

        return ResponseEntity.ok(
                "Badge awarded successfully"
        );
    }

    @GetMapping(
            "/users/{userId}"
    )
    public ResponseEntity<List<BadgeResponse>>
    getUserBadges(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                badgeService.getUserBadges(
                        userId
                )
        );
    }
}
