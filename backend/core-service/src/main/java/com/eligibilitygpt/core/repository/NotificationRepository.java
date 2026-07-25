package com.eligibilitygpt.core.repository;

import com.eligibilitygpt.core.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUploadedById(Long userId, Pageable pageable);
    List<Notification> findByExamBody(String examBody);
    Page<Notification> findByStatus(Notification.Status status, Pageable pageable);
}
