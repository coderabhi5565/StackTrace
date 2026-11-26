package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.dto.UserSummaryResponse;
import com.StackTrace.User_Service.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    /** Followers of a user, newest first, as summaries in a single query (no N+1). */
    @Query(value = """
            select new com.StackTrace.User_Service.dto.UserSummaryResponse(u.id, u.name, u.username, u.avatarUrl)
            from Follow f join User u on u.id = f.followerId
            where f.followingId = :userId
            order by f.createdAt desc, f.id desc
            """,
            countQuery = "select count(f) from Follow f where f.followingId = :userId")
    Page<UserSummaryResponse> findFollowers(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
            select new com.StackTrace.User_Service.dto.UserSummaryResponse(u.id, u.name, u.username, u.avatarUrl)
            from Follow f join User u on u.id = f.followingId
            where f.followerId = :userId
            order by f.createdAt desc, f.id desc
            """,
            countQuery = "select count(f) from Follow f where f.followerId = :userId")
    Page<UserSummaryResponse> findFollowing(@Param("userId") Long userId, Pageable pageable);

    @Query("select f.followingId from Follow f where f.followerId = :userId")
    List<Long> findFollowingIds(@Param("userId") Long userId);
}
