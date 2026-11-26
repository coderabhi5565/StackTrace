package com.stacktrace.post_service.service.impl;

import com.stacktrace.post_service.client.UserClient;
import com.stacktrace.post_service.config.AfterCommit;
import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.request.UpdatePostRequest;
import com.stacktrace.post_service.dto.response.PageResponse;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.dto.response.PostSummaryResponse;
import com.stacktrace.post_service.dto.response.TagResponse;
import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.entity.Tag;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.exception.PostAlreadyPublishedException;
import com.stacktrace.post_service.exception.PostNotFoundException;
import com.stacktrace.post_service.exception.PostNotScheduledException;
import com.stacktrace.post_service.repository.PostRepository;
import com.stacktrace.post_service.repository.TagRepository;
import com.stacktrace.post_service.security.util.SecurityUtils;
import com.stacktrace.post_service.service.MarkdownText;
import com.stacktrace.post_service.service.PostMapper;
import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    public static final int MAX_PAGE_SIZE = 50;
    static final int POINTS_FOR_PUBLISH = 10;

    private static final Sort NEWEST_PUBLISHED = Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"));
    private static final Sort NEWEST_UPDATED = Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"));

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostMapper mapper;
    private final UserClient userClient;

    // ------------------------------------------------------------------ writes

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        Long authorId = SecurityUtils.getCurrentUserId();

        Post post = new Post();
        post.setTitle(request.getTitle().trim());
        post.setSlug(generateUniqueSlug(request.getTitle()));
        post.setAuthorId(authorId);
        post.setStatus(PostStatus.DRAFT);
        applyContent(post, request.getContent(), request.getExcerpt(), request.getCoverImageUrl(), request.getTags());

        if (request.isPublish()) {
            markPublished(post);
        }
        return mapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse updatePost(Long postId, UpdatePostRequest request) {
        Post post = getActivePost(postId);
        checkOwnership(post);

        String title = request.getTitle().trim();
        // published URLs are stable; drafts follow their title
        if (post.getStatus() != PostStatus.PUBLISHED && !post.getTitle().equals(title)) {
            post.setSlug(generateUniqueSlug(title));
        }
        post.setTitle(title);
        applyContent(post, request.getContent(), request.getExcerpt(), request.getCoverImageUrl(), request.getTags());
        return mapper.toResponse(postRepository.save(post));
    }

    @Override
    public void deletePost(Long postId) {
        Post post = getActivePost(postId);
        checkOwnership(post);
        post.setDeletedAt(Instant.now());
        postRepository.save(post);
    }

    @Override
    public PostResponse publishPost(Long postId) {
        Post post = getActivePost(postId);
        checkOwnership(post);
        checkNotPublished(post);
        markPublished(post);
        return mapper.toResponse(postRepository.save(post));
    }

    @Override
    public void schedulePublish(Long postId, Instant publishAt) {
        Post post = getActivePost(postId);
        checkOwnership(post);
        checkNotPublished(post);
        if (publishAt == null || !publishAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Publish time must be in the future.");
        }
        post.setStatus(PostStatus.SCHEDULED);
        post.setScheduledPublishAt(publishAt);
        postRepository.save(post);
    }

    @Override
    public void cancelScheduledPublish(Long postId) {
        Post post = getActivePost(postId);
        checkOwnership(post);
        if (post.getStatus() != PostStatus.SCHEDULED || post.getScheduledPublishAt() == null) {
            throw new PostNotScheduledException("Post is not scheduled for publishing.");
        }
        post.setStatus(PostStatus.DRAFT);
        post.setScheduledPublishAt(null);
        postRepository.save(post);
    }

    @Override
    public void adjustCommentCount(Long postId, int delta) {
        postRepository.addComments(postId, delta);
    }

    /** Sets status/publishedAt and awards points once the transaction commits. */
    public static void markPublishedAndReward(Post post, UserClient userClient) {
        boolean firstPublish = post.getPublishedAt() == null;
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(Instant.now());
        post.setScheduledPublishAt(null);
        if (firstPublish) {
            Long author = post.getAuthorId();
            AfterCommit.runAsync(() -> userClient.addPoints(author, POINTS_FOR_PUBLISH));
        }
    }

    private void markPublished(Post post) {
        markPublishedAndReward(post, userClient);
    }

    private void applyContent(Post post, String content, String excerpt, String coverImageUrl, List<String> tagNames) {
        post.setContent(content);
        post.setExcerpt(excerpt == null || excerpt.isBlank()
                ? MarkdownText.excerpt(content, 200)
                : excerpt.trim());
        post.setReadingTime(MarkdownText.readingTimeMinutes(content));
        post.setCoverImageUrl(coverImageUrl == null || coverImageUrl.isBlank() ? null : coverImageUrl.trim());
        post.setTags(new LinkedHashSet<>(resolveTags(tagNames)));
    }

    /** Finds tags by (lower-cased) name and creates the ones that do not exist yet. */
    private List<Tag> resolveTags(List<String> rawNames) {
        Set<String> names = new LinkedHashSet<>();
        for (String raw : rawNames) {
            names.add(raw.trim().toLowerCase(Locale.ROOT));
        }
        List<Tag> existing = new ArrayList<>(tagRepository.findByNameIn(names));
        Set<String> found = new HashSet<>();
        existing.forEach(t -> found.add(t.getName()));
        for (String name : names) {
            if (!found.contains(name)) {
                existing.add(tagRepository.save(new Tag(name)));
            }
        }
        return existing;
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = toSlug(title);
        String slug = baseSlug;
        int count = 1;
        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }
        return slug;
    }

    static String toSlug(String title) {
        String ascii = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = ascii
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.length() > 80) {
            slug = slug.substring(0, 80).replaceAll("-$", "");
        }
        return slug.isEmpty() ? "post" : slug;
    }

    // ------------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));
        return mapper.toResponse(visibleToViewer(post));
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        return mapper.toResponse(visibleToViewer(getActivePost(id)));
    }

    /** Published posts are public; drafts/scheduled posts exist only for their author (404 otherwise). */
    private Post visibleToViewer(Post post) {
        if (post.getStatus() == PostStatus.PUBLISHED) {
            return post;
        }
        Optional<Long> viewer = SecurityUtils.currentUserIdIfPresent();
        if (viewer.isPresent() && viewer.get().equals(post.getAuthorId())) {
            return post;
        }
        throw new PostNotFoundException("Post not found.");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getAllPublishedPosts(int page, int size, String tag) {
        PageRequest pageable = pageable(page, size, NEWEST_PUBLISHED);
        Page<Post> posts = (tag == null || tag.isBlank())
                ? postRepository.findByStatusAndDeletedAtIsNull(PostStatus.PUBLISHED, pageable)
                : postRepository.findPublishedByTag(tag.trim().toLowerCase(Locale.ROOT), pageable);
        return mapper.toSummaryPage(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getTrendingPosts(int page, int size) {
        return mapper.toSummaryPage(postRepository.findTrending(pageable(page, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getFollowingFeed(int page, int size) {
        Long me = SecurityUtils.getCurrentUserId();
        List<Long> following = userClient.followingIds(me);
        PageRequest pageable = pageable(page, size, NEWEST_PUBLISHED);
        if (following.isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        return mapper.toSummaryPage(postRepository.findByAuthorIdInAndStatusAndDeletedAtIsNull(
                following, PostStatus.PUBLISHED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getPostsByAuthor(Long authorId, int page, int size) {
        return mapper.toSummaryPage(postRepository.findByAuthorIdAndStatusAndDeletedAtIsNull(
                authorId, PostStatus.PUBLISHED, pageable(page, size, NEWEST_PUBLISHED)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getMyPosts(PostStatus status, int page, int size) {
        Long me = SecurityUtils.getCurrentUserId();
        PageRequest pageable = pageable(page, size, NEWEST_UPDATED);
        Page<Post> posts = status == null
                ? postRepository.findByAuthorIdAndDeletedAtIsNull(me, pageable)
                : postRepository.findByAuthorIdAndStatusAndDeletedAtIsNull(me, status, pageable);
        return mapper.toSummaryPage(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return postRepository.findByIdAndDeletedAtIsNull(id)
                .map(p -> p.getStatus() == PostStatus.PUBLISHED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> searchPosts(String keyword, String tag, int page, int size) {
        String q = keyword == null ? "" : keyword.trim();
        if (q.isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }
        if (q.length() > 200) {
            q = q.substring(0, 200);
        }
        String like = q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String normalizedTag = tag == null || tag.isBlank() ? null : tag.trim().toLowerCase(Locale.ROOT);
        return mapper.toSummaryPage(postRepository.search(q, like, normalizedTag, pageable(page, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAuthorId(Long postId) {
        return getActivePost(postId).getAuthorId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getPopularTags(int limit) {
        return tagRepository.findPopular(PageRequest.of(0, Math.min(Math.max(limit, 1), 50)));
    }

    // ------------------------------------------------------------------ helpers

    private Post getActivePost(Long postId) {
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));
    }

    private void checkOwnership(Post post) {
        if (!post.getAuthorId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You are not allowed to modify this post.");
        }
    }

    private void checkNotPublished(Post post) {
        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new PostAlreadyPublishedException("Post is already published.");
        }
    }

    public static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }

    static PageRequest pageable(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);
    }
}
