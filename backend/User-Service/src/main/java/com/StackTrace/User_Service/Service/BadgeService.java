package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.dto.BadgeResponse;

import java.util.List;

public interface BadgeService {

    void awardBadge(
            Long userId,
            Long badgeId
    );

    List<BadgeResponse> getUserBadges(
            Long userId
    );
}
