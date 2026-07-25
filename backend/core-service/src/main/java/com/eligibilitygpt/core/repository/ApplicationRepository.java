package com.eligibilitygpt.core.repository;

import com.eligibilitygpt.core.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);
    List<Application> findByUserIdAndStatus(Long userId, Application.Status status);
    Optional<Application> findByUserIdAndNotificationIdAndPostId(Long userId, Long notificationId, Long postId);
    boolean existsByUserIdAndNotificationIdAndPostId(Long userId, Long notificationId, Long postId);
}
