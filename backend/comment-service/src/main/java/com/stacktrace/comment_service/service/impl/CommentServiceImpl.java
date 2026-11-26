package com.stacktrace.comment_service.service.impl;

import com.stacktrace.comment_service.client.NotificationClient;
import com.stacktrace.comment_service.client.PostClient;
import com.stacktrace.comment_service.client.UserClient;
import com.stacktrace.comment_service.config.AfterCommit;
import com.stacktrace.comment_service.dto.request.CountDeltaRequest;
import com.stacktrace.comment_service.dto.request.CreateCommentRequest;
import com.stacktrace.comment_service.dto.request.NotificationRequest;
import com.stacktrace.comment_service.dto.request.UpdateCommentRequest;
import com.stacktrace.comment_service.dto.response.AuthorSummary;
import com.stacktrace.comment_service.dto.response.CommentResponse;
import com.stacktrace.comment_service.dto.response.PageResponse;
import com.stacktrace.comment_service.entity.Comment;
import com.stacktrace.comment_service.enums.NotificationType;
import com.stacktrace.comment_service.exception.CommentNotFoundException;
import com.stacktrace.comment_service.exception.InvalidCommentRequestException;
import com.stacktrace.comment_service.exception.PostNotFoundException;
import com.stacktrace.comment_service.exception.UnauthorizedCommentAccessException;
import com.stacktrace.comment_service.mapper.CommentMapper;
import com.stacktrace.comment_service.repository.CommentRepository;
import com.stacktrace.comment_service.service.CommentService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    public static final int MAX_PAGE_SIZE = 50;

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostClient postClient;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

    @Override
    public CommentResponse createComment(CreateCommentRequest request, Long userId) {
        // only published, non-deleted posts accept comments
        if (!postClient.exists(request.getPostId())) {
            throw new PostNotFoundException("Post not found.");
        }

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository
                    .findByIdAndDeletedAtIsNull(request.getParentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException("Parent comment not found."));
            if (!parentComment.getPostId().equals(request.getPostId())) {
                throw new InvalidCommentRequestException("Parent comment does not belong to this post.");
            }
        }

        Comment saved = commentRepository.save(commentMapper.toEntity(request, userId, parentComment));

        Long postId = request.getPostId();
        Long parentAuthor = parentComment == null ? null : parentComment.getAuthorId();
        AfterCommit.runAsync("comment-count+1", () -> postClient.adjustCommentCount(postId, new CountDeltaRequest(1)));
        AfterCommit.runAsync("notify", () -> notify(saved, postId, parentAuthor, userId));

        return commentMapper.toResponse(saved, authors(List.of(userId)));
    }

    /** Notifies the post author (top-level comment) or the parent comment's author (reply). Best effort. */
    private void notify(Comment saved, Long postId, Long parentAuthor, Long userId) {
        Long recipientId = parentAuthor != null ? parentAuthor : postClient.getAuthorId(postId).getAuthorId();
        if (recipientId == null || recipientId.equals(userId)) {
            return;
        }
        notificationClient.createNotification(NotificationRequest.builder()
                .recipientId(recipientId)
                .senderId(userId)
                .type(parentAuthor != null ? NotificationType.REPLY : NotificationType.COMMENT)
                .message(parentAuthor != null ? "replied to your comment." : "commented on your post.")
                .referenceId(postId)
                .build());
    }

    @Override
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found."));
        if (!comment.getAuthorId().equals(userId)) {
            throw new UnauthorizedCommentAccessException("You are not allowed to update this comment.");
        }
        commentMapper.updateEntity(comment, request);
        Comment updated = commentRepository.saveAndFlush(comment);
        return commentMapper.toResponse(updated, authors(List.of(userId)));
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found."));
        if (!comment.getAuthorId().equals(userId)) {
            throw new UnauthorizedCommentAccessException("You are not allowed to delete this comment.");
        }
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
        Long postId = comment.getPostId();
        AfterCommit.runAsync("comment-count-1", () -> postClient.adjustCommentCount(postId, new CountDeltaRequest(-1)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        Page<Comment> roots = commentRepository.findThreads(postId, pageable(page, size));
        List<Long> rootIds = roots.getContent().stream().map(Comment::getId).toList();
        List<Comment> replies = rootIds.isEmpty() ? List.of() : commentRepository.findAllInThreads(rootIds);

        Set<Long> authorIds = new HashSet<>();
        roots.getContent().forEach(c -> authorIds.add(c.getAuthorId()));
        replies.forEach(c -> authorIds.add(c.getAuthorId()));
        Map<Long, AuthorSummary> authors = authors(authorIds);

        // build every thread in memory: O(n), two queries total regardless of depth
        Map<Long, CommentResponse> byId = new HashMap<>();
        List<CommentResponse> top = new ArrayList<>();
        for (Comment root : roots.getContent()) {
            CommentResponse r = commentMapper.toResponse(root, authors);
            byId.put(root.getId(), r);
            top.add(r);
        }
        for (Comment reply : replies) {
            CommentResponse r = commentMapper.toResponse(reply, authors);
            byId.put(reply.getId(), r);
            Long parentId = reply.getParentComment() == null ? null : reply.getParentComment().getId();
            CommentResponse parent = parentId == null ? null : byId.get(parentId);
            if (parent != null) {
                parent.getReplies().add(r);
            }
        }
        top.forEach(CommentServiceImpl::pruneDeletedLeaves);

        return new PageResponse<>(top, roots.getNumber(), roots.getSize(), roots.getTotalElements(),
                roots.getTotalPages(), roots.isLast());
    }

    /** Removes deleted comments that no longer have live replies (tombstones only where needed). */
    private static boolean pruneDeletedLeaves(CommentResponse node) {
        node.getReplies().removeIf(CommentServiceImpl::pruneDeletedLeaves);
        return node.isDeleted() && node.getReplies().isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getReplies(Long commentId, int page, int size) {
        Page<Comment> replies = commentRepository.findByParentCommentIdAndDeletedAtIsNull(commentId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                        org.springframework.data.domain.Sort.by("createdAt", "id")));
        Map<Long, AuthorSummary> authors = authors(replies.getContent().stream().map(Comment::getAuthorId).toList());
        return new PageResponse<>(replies.getContent().stream().map(c -> commentMapper.toResponse(c, authors)).toList(),
                replies.getNumber(), replies.getSize(), replies.getTotalElements(), replies.getTotalPages(),
                replies.isLast());
    }

    private Map<Long, AuthorSummary> authors(Collection<Long> ids) {
        Set<Long> unique = new HashSet<>(ids);
        unique.remove(null);
        if (unique.isEmpty()) {
            return Map.of();
        }
        try {
            return userClient.batch(unique).stream()
                    .collect(Collectors.toMap(AuthorSummary::id, Function.identity(), (a, b) -> a));
        } catch (FeignException e) {
            log.warn("user-service batch lookup failed: {}", e.getMessage());
            return Map.of();
        }
    }

    private static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }
}
