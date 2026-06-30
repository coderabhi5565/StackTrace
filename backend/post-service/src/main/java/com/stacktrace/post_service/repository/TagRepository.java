package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Long, Tag> {
}
