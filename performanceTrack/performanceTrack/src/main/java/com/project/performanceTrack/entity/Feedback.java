package com.project.performanceTrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Feedback Entity
 * ---------------
 * Captures ad-hoc or formal comments throughout the review cycle.
 * This is primarily used for "Request Changes" and "Additional Evidence" flows.
 */
@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId; // Unique ID for each feedback entry

    /**
     * Review Linkage (Workflow Phase: 9 & 10)
     * Optional link to a formal Performance Review.
     * Used if feedback is given specifically during the final review discussion
     * or acknowledgment phase (e.g., Rahul's response in Phase 10, Step 3).
     */
    @ManyToOne
    @JoinColumn(name = "review_id")
    private PerformanceReview review;

    /**
     * Goal Linkage (Workflow Phase: 3 & 6)
     * Links the feedback to a specific goal.
     * CRITICAL for Phase 3, Step 4: When Priya requests changes to the
     * React Certification goal, the explanation is stored here.
     */
    @ManyToOne
    @JoinColumn(name = "goal_id")
    private Goal goal;

    /**
     * The Author (Workflow Phase: All)
     * Identifies who wrote the feedback.
     * Typically the Manager (Priya) during a change request, but can also be
     * the Employee (Rahul) replying to feedback.
     */
    @ManyToOne
    @JoinColumn(name = "given_by_user_id", nullable = false)
    private User givenByUser;

    /**
     * The Feedback Content (Workflow Phase: 3, 4, & 6)
     * E.g., "Please specify which React certification you plan to complete..."
     * This is the raw text displayed in the "Manager Feedback" section of the
     * goal edit page in Phase 4, Step 2.
     */
    @Column(columnDefinition = "TEXT")
    private String comments;

    /**
     * Feedback Classification (Workflow Phase: 3 & 6)
     * Stores types like:
     * - "ChangeRequest": Used during initial goal setting.
     * - "EvidenceRequest": Used when a manager needs a better link (Phase 6, Step 7).
     * - "GeneralComment": Standard communication.
     */
    @Column(name = "feedback_type", length = 50)
    private String feedbackType;

    /**
     * Transaction Date (Workflow Phase: 4, Step 1)
     * The timestamp used to sort the "Feedback History" chronologically
     * so Rahul sees the most recent request first.
     */
    @Column(nullable = false)
    private LocalDateTime date;
}