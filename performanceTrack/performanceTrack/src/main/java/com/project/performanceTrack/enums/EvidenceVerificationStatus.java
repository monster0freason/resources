package com.project.performanceTrack.enums;

// Evidence link verification status
public enum EvidenceVerificationStatus {
    NOT_VERIFIED,               // Not yet verified by manager
    VERIFIED,                   // Manager verified evidence
    NEEDS_ADDITIONAL_LINK,      // Manager needs additional evidence link
    REJECTED                    // Evidence link rejected
}
