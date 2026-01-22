package com.project.performanceTrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Self-assessment request DTO
@Data
public class SelfAssessmentRequest {
    
    @NotNull(message = "Cycle ID is required")
    private Integer cycleId;
    
    @NotNull(message = "Self-assessment data is required")
    private String selfAssmt;  // JSON string with all sections
    
    @NotNull(message = "Self-rating is required")
    private Integer selfRating;
}
