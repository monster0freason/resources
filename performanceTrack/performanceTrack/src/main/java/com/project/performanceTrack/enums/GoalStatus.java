package com.project.performanceTrack.enums;

// Goal lifecycle status
public enum GoalStatus {
    PENDING,                        // Goal created, waiting for manager approval
    IN_PROGRESS,                    // Goal approved by manager, employee working on it
    PENDING_COMPLETION_APPROVAL,    // Employee submitted completion with evidence
    COMPLETED,                      // Manager approved goal completion
    REJECTED                        // Goal rejected by manager (rare case)
}
