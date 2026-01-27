package com.project.performanceTrack.entity;

import com.project.performanceTrack.enums.NotificationStatus;
import com.project.performanceTrack.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification Entity
 * -------------------
 * Acts as the real-time alert system. It bridges the gap between different
 * user roles (Admin, Manager, Employee) by flagging pending actions.
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId; // Unique ID for each system alert

    /**
     * The Recipient (Workflow Phase: All)
     * Links to the user who needs to see the alert.
     * Example: Rahul (Employee) when a goal is approved, or Priya (Manager)
     * when a self-assessment is submitted.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Category of Alert (Workflow Phase: All)
     * E.g., GOAL_SUBMITTED, GOAL_APPROVED, REVIEW_REMINDER.
     * Helps the frontend display the correct icon (Bell, Checkmark, Warning).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * The Human-Readable Alert (Workflow Phase: All)
     * E.g., "Rahul Sharma has marked goal 'API optimization' as complete."
     * This is the text displayed in the notification bell dropdown.
     */
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * Deep-Linking: Entity Type (Workflow Phase: Redirection)
     * Stores "Goal", "PerformanceReview", or "AuditLog".
     * Tells the frontend which page to load when the user clicks the notification.
     */
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    /**
     * Deep-Linking: Entity ID (Workflow Phase: Redirection)
     * Stores the specific GoalID or ReviewID.
     * Example: Clicking the alert redirects directly to /goals/2001.
     */
    @Column(name = "related_entity_id")
    private Integer relatedEntityId;

    /**
     * Tracking State (Workflow Phase: Dashboard View)
     * UNREAD or READ. Controls the "red badge" count on the user's dashboard.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD;

    /**
     * Urgency Level (Workflow Phase: Priority Sorting)
     * E.g., HIGH (for Review Deadlines) or MEDIUM (for general updates).
     * High-priority notifications often trigger the email fallback.
     */
    @Column(length = 20)
    private String priority;

    /**
     * The Action Toggle (Workflow Phase: 8 - Review Cycle)
     * If true, the notification stays prominent until a specific action is taken.
     * Example: The "Review Reminder" banner in Phase 8, Step 2.
     */
    @Column(name = "action_required")
    private Boolean actionRequired = false;

    /**
     * Trigger Timestamp (Workflow Phase: Sorting)
     * Used to show "5 mins ago" or "Yesterday" in the notification panel.
     */
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    /**
     * Audit Field (Workflow Phase: 2 - Employee Dashboard)
     * Records exactly when the user clicked the notification to mark it as read.
     */
    @Column(name = "read_date")
    private LocalDateTime readDate;
}