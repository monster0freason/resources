package com.project.performanceTrack.entity;

import com.project.performanceTrack.enums.ReviewCycleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReviewCycle Entity
 * Defines the window of time and rules for a specific review period (e.g., Q1).
 */
@Entity
@Table(name = "review_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cycle_id")
    private Integer cycleId; // Links Goals and Self-Assessments to a specific period

    @Column(nullable = false, length = 100)
    private String title; // E.g., "Q1 2026 Performance Review" - displayed on all user dashboards

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // Earliest date a goal can start

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // Deadline for goal completion and self-assessment

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewCycleStatus status; // ACTIVE (open for work) or COMPLETED (closed for reports)

    // Workflow Rule: If true, Manager must sign off on goals before they are "Completed"
    @Column(name = "requires_completion_approval", nullable = false)
    private Boolean requiresCompletionApproval = true;

    // Workflow Rule: If true, the Evidence Link field becomes mandatory in Phase 5
    @Column(name = "evidence_required", nullable = false)
    private Boolean evidenceRequired = true;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate; // System audit field

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate; // Tracks if Admin updated cycle rules or dates
}