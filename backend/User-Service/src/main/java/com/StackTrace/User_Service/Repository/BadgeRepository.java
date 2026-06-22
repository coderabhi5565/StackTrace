package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository
        extends JpaRepository<Badge, Long> {

    Optional<Badge> findByName(
            String name
    );
}