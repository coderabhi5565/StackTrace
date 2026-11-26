package com.stacktrace.post_service.service;

import com.stacktrace.post_service.client.UserClient;
import com.stacktrace.post_service.config.AfterCommit;
import com.stacktrace.post_service.dto.response.EngagementResponse;
import com.stacktrace.post_service.dto.response.PageResponse;
import com.stacktrace.post_service.dto.response.PostSummaryResponse;
import com.stacktrace.post_service.entity.Bookmark;
import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.entity.PostLike;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.exception.PostNotFoundException;
import com.stacktrace.post_service.repository.BookmarkRepository;
import com.stacktrace.post_service.repository.PostLikeRepository;
import com.stacktrace.post_service.repository.PostRepository;
import com.stacktrace.post_service.security.util.SecurityUtils;
import com.stacktrace.post_service.service.impl.PostServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Likes and bookmarks. Both operations are idempotent. */
@Service
@RequiredArgsConstructor
public class EngagementService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostMapper mapper;
    private final UserClient userClient;

    @Transactional
    public EngagementResponse like(Long postId) {
        Long me = SecurityUtils.getCurrentUserId();
        Post post = publishedPost(postId);
        if (!likeRepository.existsByPostIdAndUserId(postId, me)) {
            likeRepository.save(new PostLike(postId, me));
            postRepository.addLikes(postId, 1);
            if (!post.getAuthorId().equals(me)) {
                AfterCommit.runAsync(() -> userClient.addPoints(post.getAuthorId(), 1));
            }
        }
        return engagement(postId);
    }

    @Transactional
    public EngagementResponse unlike(Long postId) {
        Long me = SecurityUtils.getCurrentUserId();
        Post post = publishedPost(postId);
        if (likeRepository.deleteByPostIdAndUserId(postId, me) > 0) {
            postRepository.addLikes(postId, -1);
            if (!post.getAuthorId().equals(me)) {
                AfterCommit.runAsync(() -> userClient.addPoints(post.getAuthorId(), -1));
            }
        }
        return engagement(postId);
    }

    @Transactional
    public EngagementResponse bookmark(Long postId) {
        Long me = SecurityUtils.getCurrentUserId();
        publishedPost(postId);
        if (!bookmarkRepository.existsByUserIdAndPostId(me, postId)) {
            bookmarkRepository.save(new Bookmark(me, postId));
        }
        return engagement(postId);
    }

    @Transactional
    public EngagementResponse removeBookmark(Long postId) {
        Long me = SecurityUtils.getCurrentUserId();
        bookmarkRepository.deleteByUserIdAndPostId(me, postId);
        return engagement(postId);
    }

    /** Fresh counters plus the viewer's own like/bookmark state (anonymous viewers get false/false). */
    @Transactional(readOnly = true)
    public EngagementResponse engagement(Long postId) {
        // re-read to see counter updates made by bulk queries in this transaction
        Post post = postRepository.findById(postId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));
        Optional<Long> viewer = SecurityUtils.currentUserIdIfPresent();
        boolean liked = viewer.map(v -> likeRepository.existsByPostIdAndUserId(postId, v)).orElse(false);
        boolean bookmarked = viewer.map(v -> bookmarkRepository.existsByUserIdAndPostId(v, postId)).orElse(false);
        return new EngagementResponse(postId, post.getLikeCount(), post.getCommentCount(), liked, bookmarked);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> myBookmarks(int page, int size) {
        Long me = SecurityUtils.getCurrentUserId();
        return mapper.toSummaryPage(postRepository.findBookmarked(me, PostServiceImpl.pageable(page, size)));
    }

    private Post publishedPost(Long postId) {
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .filter(p -> p.getStatus() == PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));
    }
}
