package com.project.performanceTrack.entity;

import com.project.performanceTrack.enums.PerformanceReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * PerformanceReview Entity
 * The final "Master Document" summarizing the quarter's work, ratings, and feedback.
 */
@Entity
@Table(name = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId; // Unique PK for the review document

    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReviewCycle cycle; // Context: Q1, Q2, etc. (Phase 1 Setup)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The Employee being reviewed (e.g., Rahul)

    // --- PHASE 8: EMPLOYEE INPUT ---
    @Column(name = "self_assessment", columnDefinition = "TEXT")
    private String selfAssessment; // JSON blob of Sections 1-4 & 6-7 of the self-assessment form

    @Column(name = "employee_self_rating")
    private Integer employeeSelfRating; // Rahul's rating (1-5 scale)

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate; // Timestamp of self-assessment submission

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes; // Analytics field: Time taken to complete self-assessment (e.g., 30)

    // --- PHASE 9: MANAGER EVALUATION ---
    @Column(name = "manager_feedback", columnDefinition = "TEXT")
    private String managerFeedback; // JSON blob of Strengths, Improvements, and Goal Feedback

    @Column(name = "manager_rating")
    private Integer managerRating; // Priya's final score for Rahul (e.g., 4)

    @Column(name = "rating_justification", columnDefinition = "TEXT")
    private String ratingJustification; // Required text explaining the manager's rating

    @Column(name = "compensation_recommendations", columnDefinition = "TEXT")
    private String compensationRecommendations; // JSON: Bonus/Merit increase flags for HR

    @Column(name = "next_period_goals", columnDefinition = "TEXT")
    private String nextPeriodGoals; // Recommendations for the upcoming cycle (Q2)

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy; // The Manager (Priya) who signed off

    @Column(name = "review_completed_date")
    private LocalDateTime reviewCompletedDate; // Timestamp of manager final submission

    // --- WORKFLOW STATE ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerformanceReviewStatus status = PerformanceReviewStatus.PENDING; // PENDING -> SELF_ASSESSMENT_COMPLETED -> COMPLETED

    // --- PHASE 10: FINAL ACKNOWLEDGMENT ---
    @ManyToOne
    @JoinColumn(name = "acknowledged_by")
    private User acknowledgedBy; // Confirms the Employee (Rahul) has read the review

    @Column(name = "acknowledged_date")
    private LocalDateTime acknowledgedDate; // Timestamp of final sign-off

    @Column(name = "employee_response", columnDefinition = "TEXT")
    private String employeeResponse; // Optional final comments from the employee

    // --- PHASE 11: AUDIT & ANALYTICS ---
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate; // Used to track cycle duration

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate; // Tracks if edits were made to the review document
}