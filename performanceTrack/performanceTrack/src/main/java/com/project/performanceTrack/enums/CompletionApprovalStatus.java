package com.project.performanceTrack.enums;

// Goal completion approval status
public enum CompletionApprovalStatus {
    PENDING,                        // Waiting for manager review
    APPROVED,                       // Manager approved completion
    ADDITIONAL_EVIDENCE_REQUIRED,   // Manager needs more evidence
    REJECTED                        // Manager rejected completion
}
