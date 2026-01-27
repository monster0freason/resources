package com.project.performanceTrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * GoalCompletionApproval Entity
 * -----------------------------
 * Acts as the formal "Receipt of Completion." This record is created
 * during Phase 6 when a manager evaluates an employee's evidence link.
 */
@Entity
@Table(name = "goal_completion_approvals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalCompletionApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Integer approvalId; // Unique ID for this specific approval transaction (Audit ID 6001)

    /**
     * Goal Linkage (Workflow Phase: 6)
     * Points to the specific goal being finalized (e.g., Goal 2001: API Optimization).
     */
    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    /**
     * The Outcome (Workflow Phase: 6, Step 6-8)
     * Stores the final verdict:
     * - APPROVED: Goal officially closes.
     * - REJECTED: Goal returns to "In Progress."
     * - ADDITIONAL_EVIDENCE_REQUIRED: Employee must submit a new link (Phase 6, Step 7).
     */
    @Column(name = "approval_decision", nullable = false, length = 40)
    private String approvalDecision;

    /**
     * Approver Identity (Workflow Phase: 6)
     * Links to the Manager (e.g., Priya Patel) who made the decision.
     */
    @ManyToOne
    @JoinColumn(name = "approved_by", nullable = false)
    private User approvedBy;

    /**
     * Timestamp (Workflow Phase: 6, Step 6)
     * The exact date and time the manager clicked "Confirm Approval."
     */
    @Column(name = "approval_date", nullable = false)
    private LocalDateTime approvalDate;

    /**
     * Managerial Feedback (Workflow Phase: 7, Step 2)
     * Detailed commentary from the manager (e.g., "Exceptional work, Rahul! Exceeded target by 5%").
     * These comments are later pulled into the final Performance Review Results page.
     */
    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;

    /**
     * Link Verification Toggle (Workflow Phase: 6, Step 4)
     * A boolean flag set to true once the manager clicks "Verified" in the
     * Evidence verification section. This ensures a link cannot be approved
     * without being checked first.
     */
    @Column(name = "evidence_link_verified")
    private Boolean evidenceLinkVerified;

    /**
     * Internal Logic (Workflow Phase: 11 - Admin Reporting)
     * The rationale behind the decision (e.g., "Evidence verified and goal exceeded target").
     * Helps Admins understand why goals are being rejected or delayed.
     */
    @Column(name = "decision_rationale", columnDefinition = "TEXT")
    private String decisionRationale;

    /**
     * System Metadata
     * Automatically captures the record creation time for audit logs.
     */
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}