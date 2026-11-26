package com.stacktrace.comment_service.repository;

import com.stacktrace.comment_service.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Top-level comments of a post. Deleted roots stay (as tombstones) only while their thread has live replies.
     */
    @Query(value = """
            select c from Comment c
            where c.postId = :postId and c.parentComment is null
              and (c.deletedAt is null or exists (
                    select 1 from Comment r where r.rootCommentId = c.id and r.deletedAt is null))
            order by c.createdAt desc, c.id desc
            """,
            countQuery = """
            select count(c) from Comment c
            where c.postId = :postId and c.parentComment is null
              and (c.deletedAt is null or exists (
                    select 1 from Comment r where r.rootCommentId = c.id and r.deletedAt is null))
            """)
    Page<Comment> findThreads(@Param("postId") Long postId, Pageable pageable);

    /** Every reply (any depth) of the given threads, oldest first, in a single query. */
    @Query("select c from Comment c where c.rootCommentId in :rootIds order by c.createdAt asc, c.id asc")
    List<Comment> findAllInThreads(@Param("rootIds") Collection<Long> rootIds);

    Page<Comment> findByParentCommentIdAndDeletedAtIsNull(Long parentCommentId, Pageable pageable);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    long countByPostIdAndDeletedAtIsNull(Long postId);
}
