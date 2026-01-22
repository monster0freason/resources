package com.project.performanceTrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Manager review request DTO
@Data
public class ManagerReviewRequest {
    
    @NotNull(message = "Manager feedback is required")
    private String mgrFb;  // JSON string with manager feedback
    
    @NotNull(message = "Manager rating is required")
    private Integer mgrRating;
    
    private String ratingJust;  // Rating justification
    
    private String compRec;     // Compensation recommendations (JSON)
    
    private String nextGoals;   // Next period goals
}
