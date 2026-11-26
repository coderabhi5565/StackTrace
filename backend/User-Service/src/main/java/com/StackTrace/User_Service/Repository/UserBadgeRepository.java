package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    boolean existsByUser_IdAndBadge_Id(Long userId, Long badgeId);

    /** Fetch-joins the badge to avoid one query per row. */
    @Query("select ub from UserBadge ub join fetch ub.badge where ub.user.id = :userId order by ub.earnedAt asc")
    List<UserBadge> findByUserIdWithBadge(@Param("userId") Long userId);

    @Query("select ub.badge.id from UserBadge ub where ub.user.id = :userId")
    Set<Long> findBadgeIdsByUserId(@Param("userId") Long userId);
}
