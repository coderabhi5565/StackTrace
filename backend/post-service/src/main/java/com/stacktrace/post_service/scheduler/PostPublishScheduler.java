package com.stacktrace.post_service.scheduler;

import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostPublishScheduler {

    private final PostRepository postRepository;

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void publishScheduledPosts() {

        List<Post> posts =
                postRepository.findByStatusAndScheduledPublishAtLessThanEqual(
                        PostStatus.DRAFT,
                        Instant.now()
                );

        for (Post post : posts) {
            publish(post);
            postRepository.save(post);
        }
    }

    private void publish(Post post) {
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(Instant.now());
        post.setScheduledPublishAt(null);
    }
}