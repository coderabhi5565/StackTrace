package com.StackTrace.User_Service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.StackTrace.User_Service.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    List<User> findByIdIn(List<Long> ids);
    List<User> findTop10ByOrderByPointsDesc();
}
