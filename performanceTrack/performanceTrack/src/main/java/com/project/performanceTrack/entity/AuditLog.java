package com.project.performanceTrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * AuditLog Entity
 * ---------------
 * The permanent, immutable record of every significant event in the system.
 * Used by Admins to ensure compliance and monitor system health.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer auditId; // Unique sequence number for every logged event (e.g., AuditID: 9001)

    /**
     * The Actor (Workflow Phase: Phase 1, Step 1)
     * Identifies the user who performed the action.
     * Example: Admin (ID 500) logging in, or Rahul (ID 501) submitting a goal.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The Event Type (Workflow Phase: All)
     * Short codes like "LOGIN", "GOAL_CREATED", "REVIEW_COMPLETED".
     * This allows the Admin to filter logs by specific actions in Phase 11.
     */
    private String action;

    /**
     * Contextual Data (Workflow Phase: Detailed Auditing)
     * Stores a JSON string of specific details (e.g., "Goal title: API Optimization").
     * Captures "before and after" snapshots if data was changed.
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Entity Categorization (Workflow Phase: Analytics)
     * Defines which table was affected: "Goal", "User", "Review", or "ReviewCycle".
     */
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    /**
     * Target Entity ID (Workflow Phase: Analytics)
     * The specific ID of the modified object (e.g., GoalID 2001).
     * This allows Admins to reconstruct the history of a specific goal.
     */
    @Column(name = "related_entity_id")
    private Integer relatedEntityId;

    /**
     * Security Metadata (Workflow Phase: Security Monitoring)
     * Captures the device's IP address (e.g., "192.168.1.100").
     * Crucial for Phase 11's "Unusual Activity Detected" metric.
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * Result State (Workflow Phase: Phase 11 Analytics)
     * "SUCCESS" or "FAILED".
     * Helps track technical issues, like failed login attempts or database errors.
     */
    private String status;

    /**
     * Event Timestamp (Workflow Phase: Phase 11 Timeline)
     * The exact moment the action was finalized in the backend.
     * Used to calculate "Average Review Time" and "Approval Turnaround."
     */
    private LocalDateTime timestamp;
}