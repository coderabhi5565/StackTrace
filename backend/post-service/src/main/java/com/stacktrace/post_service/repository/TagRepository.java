package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.dto.response.TagResponse;
import com.stacktrace.post_service.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByNameIn(Collection<String> names);

    @Query("""
            select new com.stacktrace.post_service.dto.response.TagResponse(t.name, count(p))
            from Post p join p.tags t
            where p.status = com.stacktrace.post_service.enums.PostStatus.PUBLISHED and p.deletedAt is null
            group by t.name
            order by count(p) desc, t.name
            """)
    List<TagResponse> findPopular(Pageable pageable);
}
