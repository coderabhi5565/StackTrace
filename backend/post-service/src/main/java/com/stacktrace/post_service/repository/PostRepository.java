package com.stacktrace.post_service.repository;

import com.stacktrace.post_service.entity.Post;
import com.stacktrace.post_service.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsBySlug(String slug);

    Optional<Post> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    Page<Post> findByStatusAndDeletedAtIsNull(PostStatus status, Pageable pageable);

    @Query(value = """
            select p from Post p join p.tags t
            where t.name = :tag and p.status = com.stacktrace.post_service.enums.PostStatus.PUBLISHED
              and p.deletedAt is null
            """,
            countQuery = """
            select count(p) from Post p join p.tags t
            where t.name = :tag and p.status = com.stacktrace.post_service.enums.PostStatus.PUBLISHED
              and p.deletedAt is null
            """)
    Page<Post> findPublishedByTag(@Param("tag") String tag, Pageable pageable);

    Page<Post> findByAuthorIdAndDeletedAtIsNull(Long authorId, Pageable pageable);

    Page<Post> findByAuthorIdAndStatusAndDeletedAtIsNull(Long authorId, PostStatus status, Pageable pageable);

    Page<Post> findByAuthorIdInAndStatusAndDeletedAtIsNull(Collection<Long> authorIds, PostStatus status,
                                                           Pageable pageable);

    List<Post> findByStatusAndScheduledPublishAtLessThanEqual(PostStatus status, Instant time);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    @Query(value = """
            select p from Bookmark b join Post p on p.id = b.postId
            where b.userId = :userId and p.deletedAt is null
              and p.status = com.stacktrace.post_service.enums.PostStatus.PUBLISHED
            order by b.createdAt desc
            """,
            countQuery = """
            select count(b) from Bookmark b join Post p on p.id = b.postId
            where b.userId = :userId and p.deletedAt is null
              and p.status = com.stacktrace.post_service.enums.PostStatus.PUBLISHED
            """)
    Page<Post> findBookmarked(@Param("userId") Long userId, Pageable pageable);

    /**
     * Trending: score = (likes*2 + comments*3 + 1) / (hours since publish + 2)^1.5, over the last 30 days.
     */
    @Query(value = """
            SELECT p.* FROM posts p
            WHERE p.status = 'PUBLISHED' AND p.deleted_at IS NULL
              AND p.published_at > now() - interval '30 days'
            ORDER BY (p.like_count * 2 + p.comment_count * 3 + 1)
                     / power(extract(epoch FROM (now() - p.published_at)) / 3600 + 2, 1.5) DESC,
                     p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM posts p
            WHERE p.status = 'PUBLISHED' AND p.deleted_at IS NULL
              AND p.published_at > now() - interval '30 days'
            """,
            nativeQuery = true)
    Page<Post> findTrending(Pageable pageable);

    /**
     * Full-text search over the generated search_vector column (title A, excerpt B, content C),
     * with a title substring fallback so partial words ("spri") still match.
     */
    @Query(value = """
            SELECT p.* FROM posts p
            WHERE p.status = 'PUBLISHED' AND p.deleted_at IS NULL
              AND (p.search_vector @@ websearch_to_tsquery('english', :q)
                   OR p.title ILIKE CONCAT('%', :like, '%') ESCAPE '\\')
              AND (CAST(:tag AS text) IS NULL OR EXISTS (
                    SELECT 1 FROM post_tag pt JOIN tags t ON t.id = pt.tag_id
                    WHERE pt.post_id = p.id AND t.name = CAST(:tag AS text)))
            ORDER BY ts_rank(p.search_vector, websearch_to_tsquery('english', :q)) DESC, p.published_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM posts p
            WHERE p.status = 'PUBLISHED' AND p.deleted_at IS NULL
              AND (p.search_vector @@ websearch_to_tsquery('english', :q)
                   OR p.title ILIKE CONCAT('%', :like, '%') ESCAPE '\\')
              AND (CAST(:tag AS text) IS NULL OR EXISTS (
                    SELECT 1 FROM post_tag pt JOIN tags t ON t.id = pt.tag_id
                    WHERE pt.post_id = p.id AND t.name = CAST(:tag AS text)))
            """,
            nativeQuery = true)
    Page<Post> search(@Param("q") String query, @Param("like") String escapedLike, @Param("tag") String tag,
                      Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.likeCount = case when p.likeCount + :d < 0 then 0 else p.likeCount + :d end where p.id = :id")
    int addLikes(@Param("id") Long id, @Param("d") int delta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.commentCount = case when p.commentCount + :d < 0 then 0 else p.commentCount + :d end where p.id = :id")
    int addComments(@Param("id") Long id, @Param("d") int delta);
}
