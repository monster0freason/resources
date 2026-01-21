// User.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 50)
    private String department;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "is_first_login")
    private Boolean isFirstLogin = true;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "assignedToUser", cascade = CascadeType.ALL)
    private List<Goal> goals;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PerformanceReview> performanceReviews;

    public enum Role {
        EMPLOYEE, MANAGER, ADMIN
    }

    public enum Status {
        ACTIVE, INACTIVE
    }
}

// ReviewCycle.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "review_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cycle_id")
    private Long cycleId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CycleStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "reviewCycle", cascade = CascadeType.ALL)
    private List<PerformanceReview> performanceReviews;

    public enum CycleStatus {
        ACTIVE, INACTIVE, COMPLETED
    }
}

// Goal.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalStatus status;

    @Column(columnDefinition = "TEXT")
    private String successCriteria;

    @Column(columnDefinition = "TEXT")
    private String expectedOutcome;

    @Column(columnDefinition = "TEXT")
    private String keyPerformanceIndicators;

    @Column(length = 500)
    private String evidenceTypes;

    @Column(name = "evidence_planned_description", columnDefinition = "TEXT")
    private String evidencePlannedDescription;

    @Column(name = "current_progress")
    private Integer currentProgress = 0;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "manager_assessment", columnDefinition = "TEXT")
    private String managerAssessment;

    @Column(name = "change_requested")
    private Boolean changeRequested = false;

    @Column(name = "change_request_details", columnDefinition = "TEXT")
    private String changeRequestDetails;

    @Column(name = "change_requested_by")
    private Long changeRequestedBy;

    @Column(name = "change_requested_date")
    private LocalDateTime changeRequestedDate;

    @Column(name = "resubmission_due_date")
    private LocalDate resubmissionDueDate;

    @Column(name = "resubmitted_date")
    private LocalDateTime resubmittedDate;

    @Column(name = "revision_count")
    private Integer revisionCount = 0;

    @Column(name = "completion_submitted_date")
    private LocalDateTime completionSubmittedDate;

    @Column(name = "completion_approved_date")
    private LocalDateTime completionApprovedDate;

    @Column(name = "final_achievement_level", length = 50)
    private String finalAchievementLevel;

    @Column(name = "manager_completion_rating")
    private Integer managerCompletionRating;

    @Column(name = "completion_rejected_count")
    private Integer completionRejectedCount = 0;

    @Column(name = "last_progress_update")
    private LocalDateTime lastProgressUpdate;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<GoalMilestone> milestones;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<GoalProgress> progressUpdates;

    @OneToOne(mappedBy = "goal", cascade = CascadeType.ALL)
    private GoalCompletion goalCompletion;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public enum GoalStatus {
        PENDING_APPROVAL,
        IN_PROGRESS,
        COMPLETED_PENDING_APPROVAL,
        COMPLETED,
        REJECTED,
        NOT_ACHIEVED
    }
}

// GoalMilestone.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "goal_milestones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_id")
    private Long milestoneId;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "completed_date")
    private LocalDate completedDate;
}

// GoalProgress.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @CreationTimestamp
    @Column(name = "update_date", updatable = false)
    private LocalDateTime updateDate;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "work_done", columnDefinition = "TEXT")
    private String workDone;

    @Column(columnDefinition = "TEXT")
    private String challenges;

    @Column(name = "next_steps", columnDefinition = "TEXT")
    private String nextSteps;

    @Column(name = "interim_evidence", columnDefinition = "TEXT")
    private String interimEvidence;

    @Column(name = "updated_by")
    private Long updatedBy;
}

// GoalCompletion.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "goal_completions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_id")
    private Long completionId;

    @OneToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @CreationTimestamp
    @Column(name = "submission_date", updatable = false)
    private LocalDateTime submissionDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "achievement_summary", columnDefinition = "TEXT")
    private String achievementSummary;

    @Column(name = "success_criteria_met", columnDefinition = "TEXT")
    private String successCriteriaMet;

    @Column(name = "lessons_learned", columnDefinition = "TEXT")
    private String lessonsLearned;

    @Column(columnDefinition = "TEXT")
    private String challenges;

    @Column(name = "future_recommendations", columnDefinition = "TEXT")
    private String futureRecommendations;

    @Column(name = "manager_instructions", columnDefinition = "TEXT")
    private String managerInstructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "achievement_level", length = 50)
    private String achievementLevel;

    @Column(name = "manager_rating")
    private Integer managerRating;

    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;

    @Column(name = "impact_assessment", columnDefinition = "TEXT")
    private String impactAssessment;

    @Column(name = "development_areas", columnDefinition = "TEXT")
    private String developmentAreas;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "resubmission_allowed")
    private Boolean resubmissionAllowed;

    @Column(name = "new_target_date")
    private LocalDate newTargetDate;

    @Column(name = "additional_evidence_requested")
    private Boolean additionalEvidenceRequested = false;

    @Column(name = "additional_evidence_request_date")
    private LocalDateTime additionalEvidenceRequestDate;

    @Column(name = "resubmission_due_date")
    private LocalDate resubmissionDueDate;

    @OneToMany(mappedBy = "goalCompletion", cascade = CascadeType.ALL)
    private List<GoalEvidence> evidenceItems;

    public enum ApprovalStatus {
        PENDING_MANAGER_APPROVAL,
        APPROVED,
        REJECTED,
        ADDITIONAL_EVIDENCE_REQUIRED
    }
}

// GoalEvidence.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id")
    private Long evidenceId;

    @ManyToOne
    @JoinColumn(name = "completion_id", nullable = false)
    private GoalCompletion goalCompletion;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "evidence_type", nullable = false, length = 50)
    private String evidenceType;

    @Column(name = "evidence_title", length = 200)
    private String evidenceTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_format", length = 50)
    private String evidenceFormat;

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @Column(name = "verification_instructions", columnDefinition = "TEXT")
    private String verificationInstructions;

    @CreationTimestamp
    @Column(name = "submitted_date", updatable = false)
    private LocalDateTime submittedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 30)
    private VerificationStatus verificationStatus;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    public enum VerificationStatus {
        PENDING_VERIFICATION,
        VERIFIED_ACCEPTABLE,
        VERIFIED_EXCELLENT,
        ISSUES_FOUND,
        INVALID
    }
}

// PerformanceReview.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle reviewCycle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "submission_date", updatable = false)
    private LocalDateTime submissionDate;

    @Column(name = "self_assessment", columnDefinition = "TEXT")
    private String selfAssessment;

    @Column(name = "self_rating")
    private Integer selfRating;

    @Column(name = "goals_achievement_summary", columnDefinition = "TEXT")
    private String goalsAchievementSummary;

    @Column(name = "additional_accomplishments", columnDefinition = "TEXT")
    private String additionalAccomplishments;

    @Column(name = "competency_self_ratings", columnDefinition = "TEXT")
    private String competencySelfRatings;

    @Column(name = "challenges_and_learning", columnDefinition = "TEXT")
    private String challengesAndLearning;

    @Column(name = "development_areas", columnDefinition = "TEXT")
    private String developmentAreas;

    @Column(name = "future_goals", columnDefinition = "TEXT")
    private String futureGoals;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "evidence_count")
    private Integer evidenceCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewStatus status;

    @Column(name = "manager_feedback", columnDefinition = "TEXT")
    private String managerFeedback;

    @Column(name = "manager_rating")
    private Integer managerRating;

    @Column(name = "manager_submission_date")
    private LocalDateTime managerSubmissionDate;

    @Column(name = "competency_manager_ratings", columnDefinition = "TEXT")
    private String competencyManagerRatings;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "manager_development_areas", columnDefinition = "TEXT")
    private String managerDevelopmentAreas;

    @Column(name = "manager_recommendations", columnDefinition = "TEXT")
    private String managerRecommendations;

    @Column(name = "final_rating")
    private Integer finalRating;

    @Column(name = "review_discussion_scheduled")
    private Boolean reviewDiscussionScheduled = false;

    @Column(name = "review_discussion_date")
    private LocalDateTime reviewDiscussionDate;

    @Column(name = "employee_notes", columnDefinition = "TEXT")
    private String employeeNotes;

    @Column(name = "discussion_completed")
    private Boolean discussionCompleted = false;

    @Column(name = "discussion_summary", columnDefinition = "TEXT")
    private String discussionSummary;

    @Column(name = "employee_agreement", length = 20)
    private String employeeAgreement;

    @Column(name = "finalized_by")
    private Long finalizedBy;

    @Column(name = "finalized_date")
    private LocalDateTime finalizedDate;

    @Column(name = "admin_approved")
    private Boolean adminApproved = false;

    @Column(name = "approved_by_admin")
    private Long approvedByAdmin;

    @Column(name = "admin_approval_date")
    private LocalDateTime adminApprovalDate;

    @Column(name = "final_status", length = 30)
    private String finalStatus;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL)
    private List<SelfAssessmentEvidence> selfAssessmentEvidences;

    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL)
    private List<ActionItem> actionItems;

    public enum ReviewStatus {
        AWAITING_MANAGER_REVIEW,
        MANAGER_REVIEW_COMPLETE,
        FINALIZED,
        APPROVED
    }
}

// SelfAssessmentEvidence.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "self_assessment_evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAssessmentEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id")
    private Long evidenceId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview performanceReview;

    @Column(name = "evidence_type", length = 50)
    private String evidenceType;

    @Column(name = "evidence_title", length = 200)
    private String evidenceTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @CreationTimestamp
    @Column(name = "submitted_date", updatable = false)
    private LocalDateTime submittedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 30)
    private VerificationStatus verificationStatus;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    public enum VerificationStatus {
        PENDING_VERIFICATION,
        VERIFIED_ACCEPTABLE,
        VERIFIED_EXCELLENT,
        ISSUES_FOUND
    }
}

// ActionItem.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_item_id")
    private Long actionItemId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview performanceReview;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String task;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}

// Feedback.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private PerformanceReview performanceReview;

    @Column(name = "given_by_user_id")
    private Long givenByUserId;

    @Column(name = "feedback_type", length = 50)
    private String feedbackType;

    @Column(columnDefinition = "TEXT")
    private String comments;

    private Integer rating;

    @CreationTimestamp
    @Column(name = "date", updatable = false)
    private LocalDateTime date;
}

// Notification.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;

    @Column(name = "action_required")
    private Boolean actionRequired = false;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "celebratory_flag")
    private Boolean celebratoryFlag = false;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "read_date")
    private LocalDateTime readDate;

    public enum NotificationStatus {
        UNREAD, READ
    }

    public enum Priority {
        LOW, NORMAL, HIGH
    }
}

// AuditLog.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity;

    public enum Status {
        SUCCESS, FAILED, SUSPICIOUS
    }

    public enum Severity {
        LOW, NORMAL, MEDIUM, HIGH, CRITICAL
    }
}

// EmployeePerformanceMetrics.java
package com.company.performancetrack.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_performance_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePerformanceMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "completed_goals_count")
    private Integer completedGoalsCount = 0;

    @Column(name = "exceeded_goals_count")
    private Integer exceededGoalsCount = 0;

    @Column(name = "average_goal_rating", precision = 3, scale = 2)
    private BigDecimal averageGoalRating;

    @Column(name = "total_goals_count")
    private Integer totalGoalsCount = 0;

    @Column(name = "goal_completion_rate", precision = 5, scale = 2)
    private BigDecimal goalCompletionRate;

    @Column(name = "average_performance_rating", precision = 3, scale = 2)
    private BigDecimal averagePerformanceRating;

    @Column(name = "last_review_rating")
    private Integer lastReviewRating;

    @Column(name = "last_review_date
