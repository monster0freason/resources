package com.project.performanceTrack.enums;

// Notification types
public enum NotificationType {
    ACCOUNT_CREATED,                // User account created
    GOAL_SUBMITTED,                 // Employee submitted goal for approval
    GOAL_APPROVED,                  // Manager approved goal
    GOAL_CHANGE_REQUESTED,          // Manager requested changes to goal
    GOAL_RESUBMITTED,               // Employee resubmitted goal after changes
    GOAL_COMPLETION_SUBMITTED,      // Employee submitted goal completion
    GOAL_COMPLETION_APPROVED,       // Manager approved goal completion
    ADDITIONAL_EVIDENCE_REQUIRED,   // Manager needs more evidence
    REVIEW_REMINDER,                // Review cycle reminder
    SELF_ASSESSMENT_SUBMITTED,      // Employee submitted self-assessment
    PERFORMANCE_REVIEW_COMPLETED,   // Manager completed performance review
    REVIEW_ACKNOWLEDGED             // Employee acknowledged review
}
