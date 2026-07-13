package com.stacktrace.post_service.service.impl;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.entity.Tag;
import com.stacktrace.post_service.enums.PostStatus;
import com.stacktrace.post_service.repository.PostRepository;
import com.stacktrace.post_service.repository.TagRepository;
import com.stacktrace.post_service.security.util.SecurityUtils;
import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Set<String> tagNames = savedPost.getTags()
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        return PostResponse.builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .slug(savedPost.getSlug())
                .content(savedPost.getContent())
                .coverImageUrl(savedPost.getCoverImageUrl())
                .authorId(savedPost.getAuthorId())
                .status(savedPost.getStatus())
                .tags(tagNames)
                .scheduledAt(savedPost.getScheduledAt())
                .publishedAt(savedPost.getPublishedAt())
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(savedPost.getUpdatedAt())
                .build();
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
}