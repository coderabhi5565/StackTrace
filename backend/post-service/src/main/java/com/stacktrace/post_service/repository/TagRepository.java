package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Long, Tag> {
    List<Tag> findAllById(Set<Long> tagIds);
}
