package com.project.performanceTrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PerformanceReviewGoals Entity
 * ----------------------------
 * This acts as the "Linker" in the system.
 * It connects the formal Review Document to the specific Goals worked on.
 */
@Entity
@Table(name = "performance_review_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewGoals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Integer linkId; // Unique ID for this specific connection record

    /**
     * The Review Side (Many-to-One)
     * Links to the formal assessment (e.g., "Rahul's Q1 2026 Review").
     * One PerformanceReview will have MANY entries in this table (one for each goal).
     */
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview review;

    /**
     * The Goal Side (Many-to-One)
     * Links to the specific objective (e.g., "Reduce API response time").
     * One Goal can technically be linked to multiple reviews (e.g., if a goal
     * spans across Q1 and Q2 cycles).
     */
    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    /**
     * The Linking Moment
     * Records exactly when this goal was officially attached to the review.
     * In the workflow, this typically happens in PHASE 8, Step 5, when
     * the Employee submits their Self-Assessment.
     */
    @Column(name = "linked_date", nullable = false)
    private LocalDateTime linkedDate;
}