package com.stacktrace.post_service.service.impl;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.request.UpdatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.entity.Tag;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.exception.PostNotFoundException;
import com.stacktrace.post_service.exception.TagNotFoundException;
import com.stacktrace.post_service.repository.PostRepository;
import com.stacktrace.post_service.repository.TagRepository;
import com.stacktrace.post_service.security.util.SecurityUtils;
import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        Long authorId = SecurityUtils.getCurrentUserId();

        String slug = generateUniqueSlug(request.getTitle());

        List<Tag> tags = tagRepository.findAllById(request.getTagIds());

        if (tags.size() != request.getTagIds().size()) {
            throw new RuntimeException("One or more tags not found.");
        }
        Post post = new Post();

        post.setTitle(request.getTitle());
        post.setSlug(slug);
        post.setContent(request.getContent());
        post.setCoverImageUrl(request.getCoverImageUrl());

        post.setAuthorId(authorId);

        post.setStatus(PostStatus.DRAFT);

        post.setTags(new HashSet<>(tags));
        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
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

    private String toSlug(String title) {

        return title
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    @Override
    public PostResponse getPostBySlug(String slug) {

        Post post = postRepository
                .findBySlug(slug)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found."));

        return mapToResponse(post);

    }

    private PostResponse mapToResponse(Post post) {

        Set<String> tagNames = post.getTags()
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .coverImageUrl(post.getCoverImageUrl())
                .authorId(post.getAuthorId())
                .status(post.getStatus())
                .tags(tagNames)
                .scheduledAt(post.getScheduledAt())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Override
    public Page<PostResponse> getAllPublishedPosts(Pageable pageable) {

        Page<Post> posts = postRepository
                .findByStatusAndDeletedAtIsNull(
                        PostStatus.PUBLISHED,
                        pageable
                );

        return posts.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request) {

        Post post = getActivePost(postId);

        checkOwnership(post);

        checkDraft(post);

        List<Tag> tags = tagRepository.findAllById(request.getTagIds());

        if (tags.size() != request.getTagIds().size()) {
            throw new TagNotFoundException("One or more tags not found.");
        }

        if (!post.getTitle().equals(request.getTitle())) {
            post.setSlug(generateUniqueSlug(request.getTitle()));
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setTags(new HashSet<>(tags));

        Post updatedPost = postRepository.save(post);

        return mapToResponse(updatedPost);
    }

    private Post getActivePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found.")
                );

        if (post.getDeletedAt() != null) {
            throw new PostNotFoundException("Post not found.");
        }

        return post;
    }

    private void checkOwnership(Post post) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You are not allowed to modify this post."
            );
        }
    }

    private void checkDraft(Post post) {

        if (post.getStatus() != PostStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft posts can be edited."
            );
        }
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = getActivePost(postId);
        checkOwnership(post);
        post.setDeletedAt(Instant.now());

        postRepository.save(post);
    }
}