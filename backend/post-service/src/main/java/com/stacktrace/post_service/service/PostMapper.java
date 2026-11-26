package com.stacktrace.post_service.service;

import com.stacktrace.post_service.client.UserClient;
import com.stacktrace.post_service.dto.response.*;
import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Maps posts to DTOs, resolving all authors of a page with a single batch call. */
@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserClient userClient;

    public PostResponse toResponse(Post post) {
        AuthorSummary author = userClient.findAuthors(List.of(post.getAuthorId())).get(post.getAuthorId());
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .content(post.getContent())
                .coverImageUrl(post.getCoverImageUrl())
                .authorId(post.getAuthorId())
                .author(author)
                .status(post.getStatus())
                .tags(tagNames(post))
                .readingTime(post.getReadingTime())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .scheduledAt(post.getScheduledPublishAt())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public PageResponse<PostSummaryResponse> toSummaryPage(Page<Post> page) {
        Map<Long, AuthorSummary> authors = userClient.findAuthors(
                page.getContent().stream().map(Post::getAuthorId).toList());
        return PageResponse.of(page.map(p -> toSummary(p, authors.get(p.getAuthorId()))));
    }

    private PostSummaryResponse toSummary(Post post, AuthorSummary author) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .coverImageUrl(post.getCoverImageUrl())
                .author(author)
                .status(post.getStatus())
                .tags(tagNames(post))
                .readingTime(post.getReadingTime())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .scheduledAt(post.getScheduledPublishAt())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private static List<String> tagNames(Post post) {
        return post.getTags().stream().map(Tag::getName).sorted().toList();
    }
}
