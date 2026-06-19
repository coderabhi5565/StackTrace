package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByUserIdAndSkillName(
            Long userId,
            String skillName
    );

    List<Skill> findByUserId(Long userId);

    Optional<Skill> findById(Long id);
}