package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Long, Post> {
}
