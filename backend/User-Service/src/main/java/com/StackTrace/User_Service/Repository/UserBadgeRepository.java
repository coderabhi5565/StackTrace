package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository
        extends JpaRepository<UserBadge, Long> {

    boolean existsByUser_IdAndBadge_Id(
            Long userId,
            Long badgeId
    );

    List<UserBadge> findByUser_Id(
            Long userId
    );
}