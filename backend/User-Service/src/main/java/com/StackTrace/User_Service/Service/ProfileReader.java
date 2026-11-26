package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.FollowRepository;
import com.StackTrace.User_Service.Repository.SkillRepository;
import com.StackTrace.User_Service.Repository.UserRepository;
import com.StackTrace.User_Service.config.CacheConfig;
import com.StackTrace.User_Service.dto.SkillResponse;
import com.StackTrace.User_Service.dto.UserProfileResponse;
import com.StackTrace.User_Service.exception.UserNotFoundException;
import com.StackTrace.User_Service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Builds full profiles (counts, rank, skills, badges: ~6 queries) and caches them per user id.
 * A separate bean so calls from UserService go through the caching proxy.
 */
@Component
@RequiredArgsConstructor
public class ProfileReader {

    private final UserRepository up;
    private final FollowRepository fp;
    private final SkillRepository sp;
    private final BadgeService badgeService;
    private final CacheManager cacheManager;

    @Cacheable(value = CacheConfig.PROFILES, key = "#id")
    @Transactional(readOnly = true)
    public UserProfileResponse profile(Long id) {
        User u = up.findById(id).orElseThrow(() -> new UserNotFoundException("User Does Not Exist"));
        return UserProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .username(u.getUsername())
                .bio(u.getBio())
                .avatarUrl(u.getAvatarUrl())
                .points(u.getPoints())
                .location(u.getLocation())
                .joinedAt(u.getCreatedAt() == null ? null : u.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .rank(up.countWithMorePoints(u.getPoints()) + 1)
                .followersCount(fp.countByFollowingId(u.getId()))
                .followingCount(fp.countByFollowerId(u.getId()))
                .skills(sp.findByUserId(u.getId()).stream()
                        .map(s -> SkillResponse.builder().id(s.getId()).skillName(s.getSkillName()).build())
                        .toList())
                .badges(badgeService.getUserBadges(u.getId()))
                .build();
    }

    /**
     * Drops cached profiles after a write: now, and again after commit so a concurrent read
     * cannot re-cache pre-commit data. Best effort; entries also expire after 5 minutes.
     */
    public void evict(Long... ids) {
        evictNow(ids);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictNow(ids);
                }
            });
        }
    }

    private void evictNow(Long... ids) {
        Cache cache = cacheManager.getCache(CacheConfig.PROFILES);
        if (cache == null) {
            return;
        }
        for (Long id : ids) {
            try {
                cache.evict(id);
            } catch (RuntimeException ignored) {
                // expires with TTL
            }
        }
    }
}
