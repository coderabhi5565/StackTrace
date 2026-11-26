package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Modifying
    @Query("delete from Bookmark b where b.userId = :userId and b.postId = :postId")
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
