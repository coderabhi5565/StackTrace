package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsBySlug(String slug);

    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatusAndDeletedAtIsNull(
            PostStatus status,
            Pageable pageable
    );

    Page<Post> findByAuthorIdAndDeletedAtIsNull(
            Long authorId,
            Pageable pageable
    );

    List<Post> findByStatusAndScheduledPublishAtLessThanEqual(
            PostStatus status,
            Instant time
    );

    boolean existsByIdAndDeletedAtIsNull(Long id);

    @Query("""
SELECT p
FROM Post p
WHERE p.status = com.stacktrace.post_service.enums.PostStatus.PUBLISHED
AND p.deletedAt IS NULL
AND (
LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
""")
    List<Post> searchPosts(@Param("keyword") String keyword);
}