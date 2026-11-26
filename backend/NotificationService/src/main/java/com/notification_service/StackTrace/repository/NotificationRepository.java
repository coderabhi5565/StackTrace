package com.notification_service.StackTrace.repository;

import com.notification_service.StackTrace.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Modifying
    @Query("update Notification n set n.isRead = true where n.recipientId = :recipientId and n.isRead = false")
    int markAllRead(@Param("recipientId") Long recipientId);
}
