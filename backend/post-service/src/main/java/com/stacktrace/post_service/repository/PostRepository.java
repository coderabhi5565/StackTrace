package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsBySlug(String slug);

    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatusAndDeletedAtIsNull(
            PostStatus status,
            Pageable pageable
    );
}