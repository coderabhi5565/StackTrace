package com.stacktrace.post_service.scheduler;

import com.stacktrace.post_service.client.UserClient;
import com.stacktrace.post_service.config.CacheConfig;
import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.repository.PostRepository;
import com.stacktrace.post_service.service.impl.PostServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostPublishScheduler {

    private final PostRepository postRepository;
    private final UserClient userClient;
    private final CacheManager cacheManager;

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void publishScheduledPosts() {
        List<Post> posts = postRepository.findByStatusAndScheduledPublishAtLessThanEqual(
                PostStatus.SCHEDULED, Instant.now());
        for (Post post : posts) {
            PostServiceImpl.markPublishedAndReward(post, userClient);
        }
        if (!posts.isEmpty()) {
            for (String name : List.of(CacheConfig.LATEST_FEED, CacheConfig.TRENDING)) {
                Cache cache = cacheManager.getCache(name);
                if (cache != null) {
                    try {
                        cache.clear();
                    } catch (RuntimeException ignored) {
                        // entries expire with their TTL
                    }
                }
            }
        }
    }
}
