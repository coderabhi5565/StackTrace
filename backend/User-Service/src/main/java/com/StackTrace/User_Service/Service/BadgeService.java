package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.dto.BadgeResponse;
import com.StackTrace.User_Service.model.User;

import java.util.List;

public interface BadgeService {

    void awardBadge(Long userId, Long badgeId);

    List<BadgeResponse> getUserBadges(Long userId);

    /** Awards every points-threshold badge the user now qualifies for but does not yet hold. */
    void awardThresholdBadges(User user);
}
