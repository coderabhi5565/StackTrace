package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    Page<Follow> findByFollowingId(
            Long id,
            Pageable pageable
    );

    void deleteByFollowerIdAndFollowingId(
            Long followerId,
            Long followingId
    );
}