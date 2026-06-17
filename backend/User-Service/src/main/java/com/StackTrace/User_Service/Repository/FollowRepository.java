package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(
            Long followerId,
            Long followingId
    );

    Optional<Follow> findByFollowerIdAndFollowingId(
            Long followerId,
            Long followingId
    );

    List<Follow> findByFollowerId(Long followerId);

    List<Follow> findByFollowingId(Long followingId);

    void deleteByFollowerIdAndFollowingId(
            Long followerId,
            Long followingId
    );
}