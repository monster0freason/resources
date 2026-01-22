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

// Goal entity - represents employee goals
@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Integer goalId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private GoalCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GoalPriority priority;
    
    // Employee who owns this goal
    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser;
    
    // Manager who oversees this goal
    @ManyToOne
    @JoinColumn(name = "assigned_manager_id", nullable = false)
    private User assignedManager;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalStatus status = GoalStatus.PENDING;
    
    // Evidence link (only one link)
    @Column(name = "evidence_link", length = 500)
    private String evidenceLink;
    
    @Column(name = "evidence_link_description", columnDefinition = "TEXT")
    private String evidenceLinkDescription;
    
    @Column(name = "evidence_access_instructions", columnDefinition = "TEXT")
    private String evidenceAccessInstructions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_link_verification_status", length = 30)
    private EvidenceVerificationStatus evidenceLinkVerificationStatus;
    
    @Column(name = "evidence_link_verification_notes", columnDefinition = "TEXT")
    private String evidenceLinkVerificationNotes;
    
    @ManyToOne
    @JoinColumn(name = "evidence_link_verified_by")
    private User evidenceLinkVerifiedBy;
    
    @Column(name = "evidence_link_verified_date")
    private LocalDateTime evidenceLinkVerifiedDate;
    
    // Completion approval fields
    @Enumerated(EnumType.STRING)
    @Column(name = "completion_approval_status", length = 30)
    private CompletionApprovalStatus completionApprovalStatus;
    
    @Column(name = "completion_submitted_date")
    private LocalDateTime completionSubmittedDate;
    
    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;
    
    @ManyToOne
    @JoinColumn(name = "completion_approved_by")
    private User completionApprovedBy;
    
    @Column(name = "completion_approved_date")
    private LocalDateTime completionApprovedDate;
    
    @Column(name = "final_completion_date")
    private LocalDateTime finalCompletionDate;
    
    @Column(name = "manager_completion_comments", columnDefinition = "TEXT")
    private String managerCompletionComments;
    
    // Progress notes (stored as JSON or simple text)
    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;
    
    // Goal approval fields
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "request_changes")
    private Boolean requestChanges = false;
    
    @ManyToOne
    @JoinColumn(name = "last_reviewed_by")
    private User lastReviewedBy;
    
    @Column(name = "last_reviewed_date")
    private LocalDateTime lastReviewedDate;
    
    @Column(name = "resubmitted_date")
    private LocalDateTime resubmittedDate;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}
