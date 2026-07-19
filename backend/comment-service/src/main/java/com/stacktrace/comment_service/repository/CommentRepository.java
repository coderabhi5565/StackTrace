package com.stacktrace.comment_service.repository;
import com.stacktrace.comment_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndParentCommentIsNullAndDeletedAtIsNull(Long postId);

    List<Comment> findByParentCommentIdAndDeletedAtIsNull(Long parentCommentId);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}