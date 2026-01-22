package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.Notification;
import com.project.performanceTrack.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Notification repository for database operations
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Find notifications by user
    List<Notification> findByUser_UserIdOrderByCreatedDateDesc(Integer userId);
    
    // Find notifications by user and status
    List<Notification> findByUser_UserIdAndStatusOrderByCreatedDateDesc(Integer userId, NotificationStatus status);
}
