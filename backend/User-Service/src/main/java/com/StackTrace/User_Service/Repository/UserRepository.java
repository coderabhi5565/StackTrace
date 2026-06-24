package com.StackTrace.User_Service.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.StackTrace.User_Service.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    List<User> findByIdIn(List<Long> ids);
    Page<User>
    findAllByOrderByPointsDesc(
            Pageable pageable
    );

    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String keyword, String keyword1, Pageable pageable);
}
