package com.project.performanceTrack.entity;

import com.project.performanceTrack.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Goal Entity
 * Central ledger for the employee goal lifecycle (Creation -> Approval -> Evidence -> Completion).
 */
@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    // --- PHASE 1: IDENTITY ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Integer goalId; // Unique identifier for audit logs and API requests

    // --- PHASE 2: GOAL DEFINITION (Set by Employee) ---
    @Column(nullable = false, length = 200)
    private String title; // E.g., "Reduce API response time by 30%"

    @Column(columnDefinition = "TEXT")
    private String description; // Detailed breakdown of the objective

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private GoalCategory category; // Technical, Behavioral, Prof Dev, etc.

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GoalPriority priority; // High/Medium/Low - determines notification urgency

    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser; // The Employee owner (e.g., Rahul Sharma)

    @ManyToOne
    @JoinColumn(name = "assigned_manager_id", nullable = false)
    private User assignedManager; // The Manager reviewer (e.g., Priya Patel)

    @Column(name = "start_date")
    private LocalDate startDate; // Must be within the Review Cycle range

    @Column(name = "end_date")
    private LocalDate endDate; // Target completion date

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalStatus status = GoalStatus.PENDING; // Current state: PENDING, IN_PROGRESS, COMPLETED, etc.

    // --- PHASE 3: INITIAL APPROVAL WORKFLOW (Manager Action) ---
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Manager who clicked "Approve Goal"

    @Column(name = "approved_date")
    private LocalDateTime approvedDate; // Timestamp of initial goal approval

    @Column(name = "request_changes")
    private Boolean requestChanges = false; // Set to true if Manager clicks "Request Changes"

    @ManyToOne
    @JoinColumn(name = "last_reviewed_by")
    private User lastReviewedBy; // Tracks the last Manager to touch the goal during approval

    @Column(name = "last_reviewed_date")
    private LocalDateTime lastReviewedDate;

    @Column(name = "resubmitted_date")
    private LocalDateTime resubmittedDate; // Populated when Employee edits after a change request

    // --- PHASE 4: WORK & PROGRESS (Employee Action) ---
    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes; // Periodic logs (e.g., "Completed analysis of API endpoints")

    // --- PHASE 5: EVIDENCE SUBMISSION (Employee Action - Link Only) ---
    @Column(name = "evidence_link", length = 500)
    private String evidenceLink; // URL to external proof (Google Drive, GitHub, etc.)

    @Column(name = "evidence_link_description", columnDefinition = "TEXT")
    private String evidenceLinkDescription; // Summary of what the link proves

    @Column(name = "evidence_access_instructions", columnDefinition = "TEXT")
    private String evidenceAccessInstructions; // E.g., "Permissions set to @company domain"

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes; // Employee's final summary/learnings upon submission

    @Column(name = "completion_submitted_date")
    private LocalDateTime completionSubmittedDate; // Timestamp for "Mark as Complete" action

    // --- PHASE 6: EVIDENCE VERIFICATION (Manager Action) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_link_verification_status", length = 30)
    private EvidenceVerificationStatus evidenceLinkVerificationStatus; // VERIFIED or NEEDS_ADDITIONAL_LINK

    @Column(name = "evidence_link_verification_notes", columnDefinition = "TEXT")
    private String evidenceLinkVerificationNotes; // Manager feedback specifically on the link

    @ManyToOne
    @JoinColumn(name = "evidence_link_verified_by")
    private User evidenceLinkVerifiedBy;

    @Column(name = "evidence_link_verified_date")
    private LocalDateTime evidenceLinkVerifiedDate;

    // --- PHASE 7: FINAL COMPLETION APPROVAL (Manager Action) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "completion_approval_status", length = 30)
    private CompletionApprovalStatus completionApprovalStatus; // FINAL APPROVED or REJECTED

    @ManyToOne
    @JoinColumn(name = "completion_approved_by")
    private User completionApprovedBy;

    @Column(name = "completion_approved_date")
    private LocalDateTime completionApprovedDate;

    @Column(name = "final_completion_date")
    private LocalDateTime finalCompletionDate; // Official date for Performance Record

    @Column(name = "manager_completion_comments", columnDefinition = "TEXT")
    private String managerCompletionComments; // The high-praise feedback for the Review Cycle

    // --- SYSTEM & AUDIT METADATA ---
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate; // Used to track "Time to Set Goals" analytics

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate; // Tracks system-wide updates for data integrity
}