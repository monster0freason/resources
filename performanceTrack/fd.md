```java
package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FEEDBACK REPOSITORY
 * Data Access Layer for Feedback entity.
 * 
 * PURPOSE:
 * Manages database operations for feedback records in the PerformanceTrack system.
 * Feedback can be associated with either Goals or Performance Reviews.
 * 
 * Spring Data JPA automatically implements this interface - no manual implementation needed.
 */
@Repository  // Marks this as a Spring Data repository component for dependency injection
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    // JpaRepository<Feedback, Integer>
    //               ^        ^
    //               |        └─ Primary Key type (Integer feedbackId)
    //               └────────── Entity type being managed (Feedback)
    
    // Inherited methods available without definition:
    // - save(Feedback fb)           : Insert or update feedback
    // - findById(Integer id)        : Find feedback by ID
    // - findAll()                   : Get all feedback records
    // - deleteById(Integer id)      : Delete feedback by ID
    // - count()                     : Count total feedback records
    
    /**
     * CUSTOM QUERY METHOD 1: Find all feedback for a specific goal
     * 
     * METHOD NAME BREAKDOWN:
     * findBy           - Indicates SELECT query
     * Goal_GoalId      - Navigate from Feedback.goal to Goal.goalId
     * 
     * HOW IT WORKS:
     * Feedback entity has: @ManyToOne Goal goal;
     * Goal entity has: @Id Integer goalId;
     * 
     * "Goal_GoalId" means:
     * 1. Access the "goal" field in Feedback entity
     * 2. Navigate through the relationship (underscore = relationship traversal)
     * 3. Access the "goalId" field in the Goal entity
     * 
     * GENERATED SQL (approximately):
     * SELECT f.* FROM feedback f
     * INNER JOIN goal g ON f.goal_id = g.goal_id
     * WHERE g.goal_id = ?
     * 
     * USE CASE:
     * When viewing a goal's details page, fetch all feedback comments
     * that managers or reviewers have provided about that specific goal.
     * 
     * RETURN: List of all Feedback entries associated with the given goal
     */
    List<Feedback> findByGoal_GoalId(Integer goalId);
    
    /**
     * CUSTOM QUERY METHOD 2: Find all feedback for a specific performance review
     * 
     * METHOD NAME BREAKDOWN:
     * findBy              - Indicates SELECT query
     * Review_ReviewId     - Navigate from Feedback.review to PerformanceReview.reviewId
     * 
     * HOW IT WORKS:
     * Feedback entity has: @ManyToOne PerformanceReview review;
     * PerformanceReview entity has: @Id Integer reviewId;
     * 
     * "Review_ReviewId" means:
     * 1. Access the "review" field in Feedback entity
     * 2. Navigate through the relationship (underscore = relationship traversal)
     * 3. Access the "reviewId" field in the PerformanceReview entity
     * 
     * GENERATED SQL (approximately):
     * SELECT f.* FROM feedback f
     * INNER JOIN performance_review pr ON f.review_id = pr.review_id
     * WHERE pr.review_id = ?
     * 
     * USE CASE:
     * When viewing a performance review, display all feedback comments
     * that have been provided during or about that review cycle.
     * 
     * RETURN: List of all Feedback entries associated with the given performance review
     */
    List<Feedback> findByReview_ReviewId(Integer reviewId);
}
```

---

```java
package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.entity.Feedback;
import com.project.performanceTrack.entity.Goal;
import com.project.performanceTrack.entity.PerformanceReview;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.repository.FeedbackRepository;
import com.project.performanceTrack.repository.GoalRepository;
import com.project.performanceTrack.repository.PerformanceReviewRepository;
import com.project.performanceTrack.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * FEEDBACK CONTROLLER
 * Handles REST API endpoints for feedback management in PerformanceTrack system.
 * 
 * BUSINESS CONTEXT:
 * Feedback allows managers, reviewers, or peers to provide comments and observations
 * about an employee's goals or performance reviews. This creates a record of continuous
 * feedback throughout the performance management cycle.
 * 
 * FEEDBACK CAN BE LINKED TO:
 * 1. Goals - Feedback on specific goal progress or achievement
 * 2. Performance Reviews - Feedback provided during review cycles
 * 
 * ARCHITECTURE NOTE:
 * This controller uses direct repository access instead of a service layer
 * because feedback operations are straightforward CRUD operations without
 * complex business logic or orchestration requirements.
 */
@RestController  // Combines @Controller + @ResponseBody - returns JSON automatically
@RequestMapping("/api/v1/feedback")  // Base URL: all endpoints start with /api/v1/feedback
public class FeedbackController {
    
    // Inject FeedbackRepository for feedback database operations
    @Autowired
    private FeedbackRepository fbRepo;
    
    // Inject GoalRepository to link feedback to goals
    @Autowired
    private GoalRepository goalRepo;
    
    // Inject PerformanceReviewRepository to link feedback to reviews
    @Autowired
    private PerformanceReviewRepository reviewRepo;
    
    // Inject UserRepository to identify who is giving the feedback
    @Autowired
    private UserRepository userRepo;
    
    /**
     * ENDPOINT: GET FEEDBACK WITH OPTIONAL FILTERS
     * URL: GET /api/v1/feedback
     * URL: GET /api/v1/feedback?goalId=5
     * URL: GET /api/v1/feedback?reviewId=10
     * 
     * PURPOSE:
     * Retrieves feedback records with optional filtering by goal or review.
     * This endpoint supports three scenarios:
     * 1. Get all feedback for a specific goal (when goalId is provided)
     * 2. Get all feedback for a specific review (when reviewId is provided)
     * 3. Get all feedback in the system (when no parameters provided)
     * 
     * FILTER PRIORITY:
     * If both goalId and reviewId are provided, goalId takes precedence
     * due to the if-else structure.
     */
    @GetMapping  // Maps HTTP GET requests to this method
    public ApiResponse<List<Feedback>> getFeedback(
            @RequestParam(required = false) Integer goalId,    // Optional query param: ?goalId=5
            @RequestParam(required = false) Integer reviewId) { // Optional query param: ?reviewId=10
        
        // Declare variable to store query results
        List<Feedback> feedback;
        
        // SCENARIO 1: Filter by goalId if provided
        if (goalId != null) {
            // Fetch all feedback associated with this specific goal
            // Uses custom repository method that navigates the Goal relationship
            // Example: Get all feedback comments on "Complete Training Module" goal
            feedback = fbRepo.findByGoal_GoalId(goalId);
            
        // SCENARIO 2: Filter by reviewId if provided (and goalId was not provided)
        } else if (reviewId != null) {
            // Fetch all feedback associated with this specific performance review
            // Uses custom repository method that navigates the PerformanceReview relationship
            // Example: Get all feedback from Q4 2024 performance review
            feedback = fbRepo.findByReview_ReviewId(reviewId);
            
        // SCENARIO 3: No filters - return all feedback in the system
        } else {
            // Fetch all feedback records from database
            // Uses inherited JpaRepository method
            // NOTE: In production, this might need pagination for large datasets
            feedback = fbRepo.findAll();
        }
        
        // Return standardized success response with feedback data
        // JSON format: {"success": true, "message": "...", "data": [...]}
        return ApiResponse.success("Feedback retrieved", feedback);
    }
    
    /**
     * ENDPOINT: CREATE NEW FEEDBACK
     * URL: POST /api/v1/feedback
     * 
     * PURPOSE:
     * Creates a new feedback record from the authenticated user.
     * Feedback can be linked to either a goal or a performance review (or both).
     * 
     * REQUEST BODY EXAMPLE:
     * {
     *   "comments": "Great progress on this goal. Keep up the excellent work!",
     *   "feedbackType": "POSITIVE",
     *   "goalId": 5,              // Optional - link to goal
     *   "reviewId": 10            // Optional - link to performance review
     * }
     * 
     * SECURITY:
     * The user giving feedback is automatically determined from JWT token.
     * This prevents users from impersonating others when providing feedback.
     */
    @PostMapping  // Maps HTTP POST requests to this method
    public ApiResponse<Feedback> createFeedback(
            @RequestBody Map<String, Object> body,  // Request body as flexible Map structure
                                                     // Allows dynamic fields without strict DTO
            HttpServletRequest httpReq) {            // Contains userId from JWT authentication
        
        // STEP 1: IDENTIFY WHO IS GIVING THE FEEDBACK
        // Extract userId from JWT token (stored in request attributes by auth filter)
        Integer userId = (Integer) httpReq.getAttribute("userId");
        
        // Fetch the User entity for the person giving feedback
        // .orElse(null) returns null if user not found (though this shouldn't happen
        // since user passed authentication, but handles edge cases)
        User user = userRepo.findById(userId).orElse(null);
        
        // STEP 2: CREATE NEW FEEDBACK OBJECT AND SET BASIC FIELDS
        Feedback fb = new Feedback();
        
        // Set who is giving the feedback (the authenticated user)
        fb.setGivenByUser(user);
        
        // Extract and set the feedback comments from request body
        // Cast to String since Map<String, Object> stores values as generic Objects
        fb.setComments((String) body.get("comments"));
        
        // Extract and set the feedback type (e.g., "POSITIVE", "CONSTRUCTIVE", "NEUTRAL")
        fb.setFeedbackType((String) body.get("feedbackType"));
        
        // Set the current timestamp as the feedback creation date
        fb.setDate(LocalDateTime.now());
        
        // STEP 3: LINK FEEDBACK TO GOAL (IF PROVIDED)
        // Check if goalId was included in the request body
        if (body.get("goalId") != null) {
            // Extract goalId from request body
            // Cast to Integer since Map stores values as Objects
            Integer goalId = (Integer) body.get("goalId");
            
            // Fetch the Goal entity from database
            // .orElse(null) handles case where goal doesn't exist
            // In production, you might want to throw an exception instead
            Goal goal = goalRepo.findById(goalId).orElse(null);
            
            // Link the feedback to this goal
            // This establishes the @ManyToOne relationship in the Feedback entity
            fb.setGoal(goal);
        }
        
        // STEP 4: LINK FEEDBACK TO PERFORMANCE REVIEW (IF PROVIDED)
        // Check if reviewId was included in the request body
        if (body.get("reviewId") != null) {
            // Extract reviewId from request body
            // Cast to Integer since Map stores values as Objects
            Integer reviewId = (Integer) body.get("reviewId");
            
            // Fetch the PerformanceReview entity from database
            // .orElse(null) handles case where review doesn't exist
            // In production, you might want to throw an exception instead
            PerformanceReview review = reviewRepo.findById(reviewId).orElse(null);
            
            // Link the feedback to this performance review
            // This establishes the @ManyToOne relationship in the Feedback entity
            fb.setReview(review);
        }
        
        // STEP 5: SAVE FEEDBACK TO DATABASE
        // save() performs an INSERT operation since this is a new entity (no ID set)
        // Returns the saved entity with auto-generated ID and any default values
        Feedback saved = fbRepo.save(fb);
        
        // STEP 6: RETURN SUCCESS RESPONSE
        // Return the saved feedback wrapped in standardized ApiResponse
        // Client receives confirmation with complete feedback data including generated ID
        return ApiResponse.success("Feedback created", saved);
    }
}
```

**KEY CONCEPTS EXPLAINED:**

1. **Flexible Request Body**: Uses `Map<String, Object>` instead of strict DTO to handle dynamic fields
2. **Optional Relationships**: Feedback can be linked to goal, review, both, or neither
3. **Automatic User Attribution**: User giving feedback is determined from JWT token (security)
4. **Filter Priority**: Query parameters use if-else chain (goalId checked first)
5. **Direct Repository Access**: No service layer needed for simple CRUD operations
6. **Timestamp Recording**: Automatically captures when feedback was created
7. **Relationship Navigation**: Repository methods use underscore notation to traverse entity relationships
