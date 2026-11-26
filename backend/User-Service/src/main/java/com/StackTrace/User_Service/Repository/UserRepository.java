package com.StackTrace.User_Service.Repository;

import com.StackTrace.User_Service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByIdIn(Collection<Long> ids);

    Page<User> findAllByOrderByPointsDescIdAsc(Pageable pageable);

    @Query("select count(u) from User u where u.points > :points")
    long countWithMorePoints(@Param("points") int points);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.points = case when u.points + :delta < 0 then 0 else u.points + :delta end where u.id = :id")
    int addPoints(@Param("id") Long id, @Param("delta") int delta);

    /**
     * Fuzzy user search backed by pg_trgm GIN indexes (ILIKE uses them), ranked by trigram similarity.
     * The keyword must already have LIKE wildcards escaped.
     */
    @Query(value = """
            SELECT * FROM users u
            WHERE u.username ILIKE CONCAT('%', :q, '%') ESCAPE '\\'
               OR u.name ILIKE CONCAT('%', :q, '%') ESCAPE '\\'
            ORDER BY GREATEST(similarity(u.username, :raw), similarity(u.name, :raw)) DESC, u.points DESC, u.id
            """,
            countQuery = """
            SELECT count(*) FROM users u
            WHERE u.username ILIKE CONCAT('%', :q, '%') ESCAPE '\\'
               OR u.name ILIKE CONCAT('%', :q, '%') ESCAPE '\\'
            """,
            nativeQuery = true)
    Page<User> search(@Param("q") String escaped, @Param("raw") String raw, Pageable pageable);
}
