package com.project.performanceTrack.service;

import com.project.performanceTrack.dto.ManagerReviewRequest;
import com.project.performanceTrack.dto.SelfAssessmentRequest;
import com.project.performanceTrack.entity.*;
import com.project.performanceTrack.enums.NotificationStatus;
import com.project.performanceTrack.enums.NotificationType;
import com.project.performanceTrack.enums.PerformanceReviewStatus;
import com.project.performanceTrack.exception.BadRequestException;
import com.project.performanceTrack.exception.ResourceNotFoundException;
import com.project.performanceTrack.exception.UnauthorizedException;
import com.project.performanceTrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.performanceTrack.enums.GoalStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================================
 * PERFORMANCE REVIEW SERVICE - COMPREHENSIVE EXPLANATION
 * =============================================================================================
 * 
 * PURPOSE:
 * This service handles the complete performance review workflow in the PerformanceTrack system.
 * It manages the multi-step review process involving employees and managers.
 * 
 * BUSINESS CONTEXT:
 * Performance reviews are formal evaluations conducted during specific review cycles.
 * The process follows a structured workflow:
 * 1. Employee submits self-assessment (their own evaluation)
 * 2. Manager reviews and provides feedback + ratings
 * 3. Employee acknowledges the completed review
 * 
 * KEY FEATURES:
 * - Self-assessment submission and draft management
 * - Manager review and rating submission
 * - Automatic linking of completed goals to reviews
 * - Real-time notifications at each workflow step
 * - Comprehensive audit trail for compliance
 * - Role-based authorization checks
 * 
 * WORKFLOW STATUS PROGRESSION:
 * PENDING → SELF_ASSESSMENT_COMPLETED → COMPLETED → COMPLETED_AND_ACKNOWLEDGED
 * 
 * INTEGRATION POINTS:
 * - Links to ReviewCycle (defines when reviews happen)
 * - Links to User (employee being reviewed and their manager)
 * - Links to Goals (completed goals are attached to reviews)
 * - Creates Notifications (keeps stakeholders informed)
 * - Creates AuditLogs (tracks all actions for compliance)
 * =============================================================================================
 */
@Service  // Marks this as a Spring service component in the business logic layer
public class PerformanceReviewService {
    
    // ========================================================================================
    // DEPENDENCY INJECTION - Automatically connects to required repositories
    // ========================================================================================
    // Spring automatically creates instances of these repositories and injects them here
    // This follows the Dependency Injection pattern for loose coupling
    
    @Autowired  // Automatically inject PerformanceReview repository
    private PerformanceReviewRepository reviewRepo;  // For CRUD operations on performance reviews
    
    @Autowired  // Automatically inject User repository
    private UserRepository userRepo;  // For retrieving employee and manager information
    
    @Autowired  // Automatically inject ReviewCycle repository
    private ReviewCycleRepository cycleRepo;  // For accessing review cycle information
    
    @Autowired  // Automatically inject Notification repository
    private NotificationRepository notifRepo;  // For creating notifications to users
    
    @Autowired  // Automatically inject AuditLog repository
    private AuditLogRepository auditRepo;  // For tracking all actions for compliance/auditing
    
    @Autowired  // Automatically inject PerformanceReviewGoals linking repository
    private PerformanceReviewGoalsRepository reviewGoalsRepo;  // For linking goals to reviews
    
    @Autowired  // Automatically inject Goal repository
    private GoalRepository goalRepo;  // For retrieving completed goals
    
    
    // ========================================================================================
    // METHOD 1: GET REVIEWS BY USER
    // ========================================================================================
    /**
     * Retrieves all performance reviews for a specific user (employee).
     * 
     * BUSINESS USE CASE:
     * - Employee wants to see their review history
     * - Manager/Admin wants to see all reviews for a specific employee
     * 
     * HOW IT WORKS:
     * - Uses Spring Data JPA query method: findByUser_UserId
     * - The underscore (_) notation navigates the relationship: User.userId
     * - Returns all reviews where the User entity's userId matches the parameter
     * 
     * @param userId - The ID of the user whose reviews to retrieve
     * @return List of PerformanceReview objects for that user
     */
    public List<PerformanceReview> getReviewsByUser(Integer userId) {
        // Call repository method - Spring Data JPA automatically generates the SQL:
        // SELECT * FROM performance_review WHERE user_id = ?
        return reviewRepo.findByUser_UserId(userId);
    }
    
    
    // ========================================================================================
    // METHOD 2: GET REVIEWS BY REVIEW CYCLE
    // ========================================================================================
    /**
     * Retrieves all performance reviews for a specific review cycle.
     * 
     * BUSINESS USE CASE:
     * - Admin wants to see all reviews submitted during Q1 2024 review cycle
     * - Manager wants to track review completion status for their team in a cycle
     * 
     * EXAMPLE SCENARIO:
     * If cycleId = 5 (Q1 2024 Review Cycle), this returns all employee reviews
     * submitted for that quarter's performance evaluation.
     * 
     * @param cycleId - The ID of the review cycle
     * @return List of all PerformanceReview objects in that cycle
     */
    public List<PerformanceReview> getReviewsByCycle(Integer cycleId) {
        // Navigate the relationship to ReviewCycle entity:
        // SELECT * FROM performance_review WHERE cycle_id = ?
        return reviewRepo.findByCycle_CycleId(cycleId);
    }
    
    
    // ========================================================================================
    // METHOD 3: GET REVIEW BY ID
    // ========================================================================================
    /**
     * Retrieves a single performance review by its unique ID.
     * 
     * BUSINESS USE CASE:
     * - User clicks on a specific review to view details
     * - System needs to load review data for editing or viewing
     * 
     * ERROR HANDLING:
     * If the review doesn't exist, throws ResourceNotFoundException instead of returning null.
     * This provides clear error messaging to the frontend.
     * 
     * @param reviewId - Unique identifier of the review
     * @return The PerformanceReview object
     * @throws ResourceNotFoundException if review doesn't exist
     */
    public PerformanceReview getReviewById(Integer reviewId) {
        // findById returns Optional<PerformanceReview> - might be empty if not found
        return reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
                // orElseThrow: If Optional is empty, throw custom exception
    }
    
    
    // ========================================================================================
    // METHOD 4: SUBMIT SELF-ASSESSMENT (EMPLOYEE) - CORE WORKFLOW METHOD
    // ========================================================================================
    /**
     * Handles employee self-assessment submission - FIRST STEP of review process.
     * 
     * BUSINESS WORKFLOW:
     * 1. Employee reflects on their performance during the review cycle
     * 2. They write self-assessment and give themselves a rating (1-5)
     * 3. System saves this and notifies their manager to review
     * 4. System automatically links all COMPLETED goals to this review
     * 
     * DETAILED PROCESS FLOW:
     * Step 1: Verify employee exists in database
     * Step 2: Verify review cycle exists and is valid
     * Step 3: Check if review already exists for this employee + cycle combo
     * Step 4: Prevent duplicate submissions if already completed
     * Step 5: Create new review OR update existing draft
     * Step 6: Save self-assessment text and self-rating
     * Step 7: Change status to SELF_ASSESSMENT_COMPLETED
     * Step 8: Link all completed goals to this review
     * Step 9: Notify manager to review
     * Step 10: Create audit log entry
     * 
     * BUSINESS RULES:
     * - Can only submit self-assessment once per cycle (prevents duplicates)
     * - Can update draft multiple times before final submission
     * - Status changes from PENDING → SELF_ASSESSMENT_COMPLETED
     * - Manager is notified immediately (HIGH priority, action required)
     * 
     * INTEGRATION IMPACT:
     * - Creates notification for manager
     * - Links completed goals automatically
     * - Creates audit trail
     * 
     * @param req - SelfAssessmentRequest containing assessment text and rating
     * @param empId - ID of the employee submitting assessment
     * @return Saved PerformanceReview object with self-assessment data
     * @throws ResourceNotFoundException if employee or cycle not found
     * @throws BadRequestException if self-assessment already submitted
     */
    public PerformanceReview submitSelfAssessment(SelfAssessmentRequest req, Integer empId) {
        
        // STEP 1: Retrieve the employee from database
        // Why? We need the full User object to create relationships and notifications
        User emp = userRepo.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        // STEP 2: Retrieve the review cycle
        // Why? Reviews are always tied to specific time periods (Q1, Q2, Annual, etc.)
        ReviewCycle cycle = cycleRepo.findById(req.getCycleId())
                .orElseThrow(() -> new ResourceNotFoundException("Review cycle not found"));
        
        // STEP 3: Check if review already exists for this employee + cycle combination
        // Business rule: One employee can only have ONE review per cycle
        // findByCycle_CycleIdAndUser_UserId is a custom query method that finds reviews
        // matching both the cycle ID AND user ID
        PerformanceReview review = reviewRepo
                .findByCycle_CycleIdAndUser_UserId(req.getCycleId(), empId)
                .orElse(null);  // Returns null if not found (that's okay - means new review)
        
        // STEP 4: Business validation - prevent duplicate submissions
        // If review exists AND status is NOT pending, it means already submitted
        if (review != null && review.getStatus() != PerformanceReviewStatus.PENDING) {
            throw new BadRequestException("Self-assessment already submitted");
            // This protects data integrity - employees can't keep overwriting reviews
        }
        
        // STEP 5: Create NEW review OR use existing draft
        if (review == null) {
            // First time submitting - create brand new review object
            review = new PerformanceReview();
            review.setCycle(cycle);      // Link to review cycle
            review.setUser(emp);         // Link to employee being reviewed
        }
        // If review exists (was in PENDING status), we'll update it below
        
        // STEP 6: Set self-assessment data from request
        review.setSelfAssessment(req.getSelfAssmt());         // Employee's written assessment
        review.setEmployeeSelfRating(req.getSelfRating());   // Employee's self-rating (1-5)
        review.setStatus(PerformanceReviewStatus.SELF_ASSESSMENT_COMPLETED);  // Update status
        review.setSubmittedDate(LocalDateTime.now());        // Track submission timestamp
        
        // STEP 7: Save to database
        // If new review: INSERT INTO performance_review...
        // If existing: UPDATE performance_review SET...
        PerformanceReview saved = reviewRepo.save(review);
        
        // STEP 8: AUTOMATIC GOAL LINKING - Important business logic!
        // Find all goals assigned to this employee that are COMPLETED
        // Why? Reviews should reflect achievements - completed goals = evidence of performance
        List<Goal> completedGoals = goalRepo.findByAssignedToUser_UserIdAndStatus(empId, GoalStatus.COMPLETED);
        
        // Create linking records in the PerformanceReviewGoals junction table
        // This creates many-to-many relationship between reviews and goals
        for (Goal goal : completedGoals) {
            PerformanceReviewGoals link = new PerformanceReviewGoals();
            link.setReview(saved);              // Foreign key to review
            link.setGoal(goal);                 // Foreign key to goal
            link.setLinkedDate(LocalDateTime.now());  // Track when linked
            reviewGoalsRepo.save(link);         // Save to junction table
        }
        // This allows the review to show: "During this period, you completed goals X, Y, Z"
        
        // STEP 9: Notify manager - Critical for workflow progression
        // Manager needs to know their employee submitted self-assessment
        if (emp.getManager() != null) {  // Check if employee has a manager assigned
            Notification notif = new Notification();
            notif.setUser(emp.getManager());  // Send notification TO the manager
            notif.setType(NotificationType.SELF_ASSESSMENT_SUBMITTED);  // Categorize notification
            notif.setMessage(emp.getName() + " submitted self-assessment");  // User-friendly message
            notif.setRelatedEntityType("PerformanceReview");  // Link back to review
            notif.setRelatedEntityId(saved.getReviewId());    // Specific review ID
            notif.setStatus(NotificationStatus.UNREAD);       // Initially unread
            notif.setPriority("HIGH");                        // High priority - needs action
            notif.setActionRequired(true);                    // Manager must review this
            notifRepo.save(notif);  // Save notification to database
            // Manager will see this in their notification panel/email
        }
        
        // STEP 10: Create audit log - Compliance and tracking
        // Every significant action must be logged for auditing purposes
        AuditLog log = new AuditLog();
        log.setUser(emp);                           // Who performed the action
        log.setAction("SELF_ASSESSMENT_SUBMITTED"); // What action was performed
        log.setDetails("Submitted self-assessment for " + cycle.getTitle());  // Context
        log.setRelatedEntityType("PerformanceReview");  // Entity type affected
        log.setRelatedEntityId(saved.getReviewId());    // Specific entity affected
        log.setStatus("SUCCESS");                       // Action completed successfully
        log.setTimestamp(LocalDateTime.now());          // When it happened
        auditRepo.save(log);  // Persist to audit_log table
        // This creates permanent record: "On [date], [employee] submitted self-assessment"
        
        // Return the saved review object back to controller
        return saved;
    }
    
    
    // ========================================================================================
    // METHOD 5: UPDATE SELF-ASSESSMENT DRAFT (EMPLOYEE)
    // ========================================================================================
    /**
     * Allows employee to update their self-assessment before final manager review.
     * 
     * BUSINESS USE CASE:
     * - Employee submits self-assessment but realizes they want to add more details
     * - Employee wants to update their self-rating before manager sees it
     * - Allows iterative improvement of self-reflection
     * 
     * BUSINESS RULES:
     * - Can only update if review belongs to the requesting employee (authorization)
     * - Can only update if status is PENDING or SELF_ASSESSMENT_COMPLETED
     * - Cannot update after manager has completed their review
     * - Status does NOT change (stays in current state)
     * 
     * AUTHORIZATION CHECK:
     * Verifies that the employee requesting the update is the same employee
     * being reviewed. Prevents employees from editing each other's reviews.
     * 
     * @param reviewId - ID of the review to update
     * @param req - Updated self-assessment data
     * @param empId - ID of employee making the update
     * @return Updated PerformanceReview object
     * @throws ResourceNotFoundException if review not found
     * @throws UnauthorizedException if employee doesn't own this review
     * @throws BadRequestException if review is already completed by manager
     */
    public PerformanceReview updateSelfAssessmentDraft(Integer reviewId, SelfAssessmentRequest req, Integer empId) {
        
        // STEP 1: Retrieve the review
        PerformanceReview review = getReviewById(reviewId);
        
        // STEP 2: AUTHORIZATION CHECK - Security validation
        // Compare the user ID in the review with the requesting employee's ID
        if (!review.getUser().getUserId().equals(empId)) {
            // Employee is trying to edit someone else's review - DENY
            throw new UnauthorizedException("Not authorized");
        }
        
        // STEP 3: BUSINESS RULE VALIDATION - Check review state
        // Can only update if still in early stages of workflow
        if (review.getStatus() != PerformanceReviewStatus.PENDING && 
            review.getStatus() != PerformanceReviewStatus.SELF_ASSESSMENT_COMPLETED) {
            // Review has progressed too far - manager already reviewed it
            throw new BadRequestException("Cannot update - review already completed");
        }
        
        // STEP 4: Update the self-assessment data
        review.setSelfAssessment(req.getSelfAssmt());       // New assessment text
        review.setEmployeeSelfRating(req.getSelfRating()); // New self-rating
        
        // NOTE: We do NOT change the status here
        // If it was PENDING, it stays PENDING
        // If it was SELF_ASSESSMENT_COMPLETED, it stays SELF_ASSESSMENT_COMPLETED
        // This is intentional - it's just an update, not a new submission
        
        // STEP 5: Save changes to database
        PerformanceReview updated = reviewRepo.save(review);
        
        // STEP 6: Create audit log for tracking changes
        User emp = userRepo.findById(empId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(emp);
        log.setAction("SELF_ASSESSMENT_DRAFT_UPDATED");  // Different action than submission
        log.setDetails("Updated self-assessment draft");
        log.setRelatedEntityType("PerformanceReview");
        log.setRelatedEntityId(reviewId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        // Audit trail shows: "Employee updated their self-assessment on [date]"
        
        return updated;
    }
    
    
    // ========================================================================================
    // METHOD 6: SUBMIT MANAGER REVIEW (MANAGER) - SECOND STEP OF WORKFLOW
    // ========================================================================================
    /**
     * Manager submits their review and rating for the employee - COMPLETES THE REVIEW.
     * 
     * BUSINESS WORKFLOW:
     * After employee submits self-assessment, manager must:
     * 1. Review employee's self-assessment
     * 2. Review linked completed goals (evidence of performance)
     * 3. Provide detailed feedback on employee's performance
     * 4. Give manager rating (may differ from self-rating)
     * 5. Justify the rating with specific examples
     * 6. Provide compensation recommendations (raise, bonus, etc.)
     * 7. Set goals for next performance period
     * 
     * DETAILED PROCESS FLOW:
     * Step 1: Retrieve the review
     * Step 2: Verify requesting manager is the employee's actual manager
     * Step 3: Verify self-assessment is completed (prerequisite)
     * Step 4: Save manager's feedback, rating, and recommendations
     * Step 5: Mark review as COMPLETED
     * Step 6: Notify employee that review is ready
     * Step 7: Create audit log
     * 
     * BUSINESS RULES:
     * - Only the employee's direct manager can submit review
     * - Self-assessment MUST be completed first (enforced workflow order)
     * - Status changes: SELF_ASSESSMENT_COMPLETED → COMPLETED
     * - Employee receives immediate notification
     * 
     * KEY FIELDS POPULATED:
     * - managerFeedback: Detailed performance evaluation
     * - managerRating: 1-5 rating by manager
     * - ratingJustification: Why this rating was given
     * - compensationRecommendations: Salary/bonus recommendations
     * - nextPeriodGoals: Goals for upcoming period
     * - reviewedBy: Which manager completed the review
     * - reviewCompletedDate: Timestamp of completion
     * 
     * @param reviewId - ID of review to complete
     * @param req - ManagerReviewRequest with all manager input
     * @param mgrId - ID of manager submitting review
     * @return Completed PerformanceReview object
     * @throws ResourceNotFoundException if review not found
     * @throws UnauthorizedException if not employee's manager
     * @throws BadRequestException if self-assessment not completed
     */
    public PerformanceReview submitManagerReview(Integer reviewId, ManagerReviewRequest req, Integer mgrId) {
        
        // STEP 1: Retrieve the review
        PerformanceReview review = getReviewById(reviewId);
        
        // STEP 2: CRITICAL AUTHORIZATION CHECK
        // Verify the requesting manager is the employee's actual manager
        // This prevents managers from reviewing employees they don't manage
        if (!review.getUser().getManager().getUserId().equals(mgrId)) {
            // Example: If Manager A tries to review Manager B's employee → DENY
            throw new UnauthorizedException("Not authorized");
        }
        
        // STEP 3: WORKFLOW VALIDATION - Check prerequisite
        // Manager can only review AFTER employee completes self-assessment
        // This enforces proper workflow order
        if (review.getStatus() != PerformanceReviewStatus.SELF_ASSESSMENT_COMPLETED) {
            throw new BadRequestException("Self-assessment not completed");
            // Without self-assessment, manager has nothing to review
        }
        
        // STEP 4: Retrieve manager user object
        User mgr = userRepo.findById(mgrId).orElse(null);
        
        // STEP 5: Populate all manager review fields
        review.setManagerFeedback(req.getMgrFb());  // Detailed feedback paragraph
        review.setManagerRating(req.getMgrRating());  // Manager's rating (1-5)
        review.setRatingJustification(req.getRatingJust());  // Why this rating?
        review.setCompensationRecommendations(req.getCompRec());  // Raise/bonus recommendations
        review.setNextPeriodGoals(req.getNextGoals());  // Goals for next quarter/year
        review.setReviewedBy(mgr);  // Link to manager who did the review
        review.setReviewCompletedDate(LocalDateTime.now());  // Timestamp completion
        review.setStatus(PerformanceReviewStatus.COMPLETED);  // WORKFLOW PROGRESSION
        
        // STEP 6: Save the completed review
        PerformanceReview saved = reviewRepo.save(review);
        
        // STEP 7: Notify employee - CRITICAL
        // Employee should know their review is ready to view
        Notification notif = new Notification();
        notif.setUser(review.getUser());  // Send TO the employee
        notif.setType(NotificationType.PERFORMANCE_REVIEW_COMPLETED);
        notif.setMessage("Your performance review has been completed");
        notif.setRelatedEntityType("PerformanceReview");
        notif.setRelatedEntityId(reviewId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setPriority("HIGH");  // Important notification
        // NOTE: No actionRequired flag - employee just needs to read it
        notifRepo.save(notif);
        
        // STEP 8: Create audit log
        AuditLog log = new AuditLog();
        log.setUser(mgr);  // Manager performed the action
        log.setAction("MANAGER_REVIEW_COMPLETED");
        log.setDetails("Completed review for " + review.getUser().getName());  // Context
        log.setRelatedEntityType("PerformanceReview");
        log.setRelatedEntityId(reviewId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return saved;
    }
    
    
    // ========================================================================================
    // METHOD 7: ACKNOWLEDGE REVIEW (EMPLOYEE) - FINAL STEP
    // ========================================================================================
    /**
     * Employee acknowledges they have read and understood their performance review.
     * This is the FINAL STEP of the review workflow.
     * 
     * BUSINESS PURPOSE:
     * - Legal/HR requirement: Employee must acknowledge receipt of review
     * - Provides closure to the review process
     * - Allows employee to respond or provide comments
     * - Creates record that employee was informed of their evaluation
     * 
     * DETAILED PROCESS:
     * Step 1: Retrieve the review
     * Step 2: Verify employee owns this review (authorization)
     * Step 3: Verify review is completed by manager (prerequisite)
     * Step 4: Record acknowledgment with optional response
     * Step 5: Change status to final state: COMPLETED_AND_ACKNOWLEDGED
     * Step 6: Notify manager of acknowledgment
     * Step 7: Create audit log
     * 
     * BUSINESS RULES:
     * - Can only acknowledge your own review (not someone else's)
     * - Manager must have completed the review first
     * - Status changes: COMPLETED → COMPLETED_AND_ACKNOWLEDGED
     * - Employee can provide optional response/comments
     * - Manager is notified when employee acknowledges
     * 
     * LEGAL CONTEXT:
     * This acknowledgment may be legally required in some jurisdictions
     * to prove employee was informed of performance evaluation.
     * 
     * @param reviewId - ID of review to acknowledge
     * @param empId - ID of employee acknowledging
     * @param response - Optional employee response/comments
     * @return Acknowledged PerformanceReview object
     * @throws ResourceNotFoundException if review not found
     * @throws UnauthorizedException if not employee's review
     * @throws BadRequestException if review not completed by manager yet
     */
    public PerformanceReview acknowledgeReview(Integer reviewId, Integer empId, String response) {
        
        // STEP 1: Retrieve the review
        PerformanceReview review = getReviewById(reviewId);
        
        // STEP 2: AUTHORIZATION CHECK
        // Verify the requesting employee is the one being reviewed
        if (!review.getUser().getUserId().equals(empId)) {
            throw new UnauthorizedException("Not authorized");
            // Can't acknowledge someone else's review
        }
        
        // STEP 3: WORKFLOW VALIDATION
        // Can only acknowledge after manager has completed the review
        if (review.getStatus() != PerformanceReviewStatus.COMPLETED) {
            throw new BadRequestException("Review not completed");
            // Nothing to acknowledge if manager hasn't finished
        }
        
        // STEP 4: Record acknowledgment details
        User emp = userRepo.findById(empId).orElse(null);
        review.setAcknowledgedBy(emp);  // Who acknowledged (should match review.user)
        review.setAcknowledgedDate(LocalDateTime.now());  // When acknowledged
        review.setEmployeeResponse(response);  // Optional employee comments
        review.setStatus(PerformanceReviewStatus.COMPLETED_AND_ACKNOWLEDGED);  // FINAL STATE
        
        // STEP 5: Save the acknowledged review
        PerformanceReview saved = reviewRepo.save(review);
        
        // STEP 6: Notify manager - Close the loop
        // Manager should know employee has seen and acknowledged the review
        if (review.getUser().getManager() != null) {
            Notification notif = new Notification();
            notif.setUser(review.getUser().getManager());  // Send TO manager
            notif.setType(NotificationType.REVIEW_ACKNOWLEDGED);
            notif.setMessage(review.getUser().getName() + " acknowledged their review");
            notif.setRelatedEntityType("PerformanceReview");
            notif.setRelatedEntityId(reviewId);
            notif.setStatus(NotificationStatus.UNREAD);
            // Note: Not high priority, just informational
            notifRepo.save(notif);
        }
        
        // STEP 7: Create audit log - Complete the paper trail
        AuditLog log = new AuditLog();
        log.setUser(emp);
        log.setAction("REVIEW_ACKNOWLEDGED");
        log.setDetails("Acknowledged performance review");
        log.setRelatedEntityType("PerformanceReview");
        log.setRelatedEntityId(reviewId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        // Review workflow is now COMPLETE
        // PENDING → SELF_ASSESSMENT_COMPLETED → COMPLETED → COMPLETED_AND_ACKNOWLEDGED ✓
        
        return saved;
    }
}


/**
 * =============================================================================================
 * SUMMARY OF SERVICE METHODS
 * =============================================================================================
 * 
 * 1. getReviewsByUser(userId)
 *    - Simple retrieval of all reviews for a user
 *    - Used for review history display
 * 
 * 2. getReviewsByCycle(cycleId)
 *    - Retrieves all reviews in a specific time period
 *    - Used for cycle-based reporting
 * 
 * 3. getReviewById(reviewId)
 *    - Gets single review by ID
 *    - Used for detail views and updates
 * 
 * 4. submitSelfAssessment(request, empId) ⭐ COMPLEX
 *    - Employee submits initial self-assessment
 *    - Links completed goals automatically
 *    - Notifies manager
 *    - Creates audit trail
 * 
 * 5. updateSelfAssessmentDraft(reviewId, request, empId)
 *    - Allows iterative updates before manager reviews
 *    - Authorization and status checks
 * 
 * 6. submitManagerReview(reviewId, request, mgrId) ⭐ COMPLEX
 *    - Manager completes the review
 *    - Requires self-assessment prerequisite
 *    - Notifies employee
 *    - Creates audit trail
 * 
 * 7. acknowledgeReview(reviewId, empId, response) ⭐ FINAL STEP
 *    - Employee confirms receipt of review
 *    - Completes the workflow
 *    - Notifies manager
 *    - Creates audit trail
 * 
 * =============================================================================================
 * WORKFLOW DIAGRAM
 * =============================================================================================
 * 
 * [CYCLE CREATED] → [PENDING]
 *                       ↓
 *         Employee calls submitSelfAssessment()
 *                       ↓
 *         [SELF_ASSESSMENT_COMPLETED] ← Can update with updateSelfAssessmentDraft()
 *                       ↓
 *         Manager calls submitManagerReview()
 *                       ↓
 *         [COMPLETED] ← Notification sent to employee
 *                       ↓
 *         Employee calls acknowledgeReview()
 *                       ↓
 *         [COMPLETED_AND_ACKNOWLEDGED] ← Final state, notification sent to manager
 * 
 * =============================================================================================

 */



 package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.dto.ManagerReviewRequest;
import com.project.performanceTrack.dto.SelfAssessmentRequest;
import com.project.performanceTrack.entity.PerformanceReview;
import com.project.performanceTrack.service.PerformanceReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * =============================================================================================
 * PERFORMANCE REVIEW CONTROLLER - COMPREHENSIVE EXPLANATION
 * =============================================================================================
 * 
 * PURPOSE:
 * This controller is the REST API layer for performance review operations.
 * It receives HTTP requests from the frontend, validates them, and delegates
 * business logic to PerformanceReviewService.
 * 
 * LAYER ARCHITECTURE ROLE:
 * Controller Layer (Presentation) ← YOU ARE HERE
 *     ↓ Calls methods
 * Service Layer (Business Logic) ← PerformanceReviewService
 *     ↓ Calls methods
 * Repository Layer (Data Access) ← PerformanceReviewRepository
 *     ↓ Executes SQL
 * Database (MySQL) ← Physical storage
 * 
 * KEY RESPONSIBILITIES:
 * 1. Define REST API endpoints (URLs that frontend can call)
 * 2. Handle HTTP methods (GET, POST, PUT)
 * 3. Extract data from requests (path variables, query params, request body)
 * 4. Enforce role-based access control (@PreAuthorize)
 * 5. Validate incoming data (@Valid)
 * 6. Call appropriate service methods
 * 7. Wrap responses in consistent ApiResponse format
 * 8. Extract user context from JWT (userId, role)
 * 
 * BASE URL: /api/v1/performance-reviews
 * All endpoints in this controller start with this base path
 * 
 * AUTHENTICATION & AUTHORIZATION:
 * - JwtAuthFilter runs BEFORE any controller method
 * - JwtAuthFilter extracts userId and userRole from JWT token
 * - These are stored in HttpServletRequest attributes
 * - @PreAuthorize checks roles before allowing method execution
 * - Service layer does additional authorization checks (e.g., manager validation)
 * 
 * =============================================================================================
 */

@RestController  // Marks this as REST API controller - returns JSON, not HTML views
// @RestController = @Controller + @ResponseBody (automatic JSON serialization)

@RequestMapping("/api/v1/performance-reviews")  // Base URL for all endpoints in this controller
// Example: http://localhost:8080/api/v1/performance-reviews
public class PerformanceReviewController {
    
    // ========================================================================================
    // DEPENDENCY INJECTION
    // ========================================================================================
    @Autowired  // Spring automatically injects the PerformanceReviewService instance
    private PerformanceReviewService reviewSvc;  // All business logic lives here
    // Controller is THIN - just routing and validation
    // Service is FAT - all business rules and database operations
    
    
    // ========================================================================================
    // ENDPOINT 1: GET REVIEWS (WITH FILTERING)
    // ========================================================================================
    /**
     * Retrieves performance reviews with optional filtering.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/performance-reviews
     * 
     * QUERY PARAMETERS (all optional):
     * - userId: Filter by specific user (admin only)
     * - cycleId: Filter by review cycle (all roles)
     * 
     * URL EXAMPLES:
     * GET /api/v1/performance-reviews
     *     → Returns current user's reviews
     * GET /api/v1/performance-reviews?userId=5
     *     → Returns user #5's reviews (admin only)
     * GET /api/v1/performance-reviews?cycleId=3
     *     → Returns all reviews for cycle #3
     * 
     * BUSINESS LOGIC:
     * - If cycleId provided: Get all reviews in that cycle (any role)
     * - If userId provided AND user is ADMIN: Get specific user's reviews
     * - Otherwise: Get current logged-in user's reviews
     * 
     * ROLE-BASED BEHAVIOR:
     * Employee: Can only see their own reviews
     * Manager: Can only see their own reviews (unless cycleId filter)
     * Admin: Can see any user's reviews
     * 
     * SECURITY:
     * - JWT token required (JwtAuthFilter validates before reaching here)
     * - userId and userRole extracted from JWT by JwtAuthFilter
     * - Non-admins cannot access other users' reviews
     * 
     * @param httpReq - HttpServletRequest object containing JWT-extracted attributes
     * @param userId - Optional query parameter: specific user to filter by
     * @param cycleId - Optional query parameter: specific cycle to filter by
     * @return ApiResponse containing list of PerformanceReview objects
     */
    @GetMapping  // Maps to: GET /api/v1/performance-reviews
    public ApiResponse<List<PerformanceReview>> getReviews(
            HttpServletRequest httpReq,  // Automatically injected by Spring
            @RequestParam(required = false) Integer userId,   // ?userId=5 (optional)
            @RequestParam(required = false) Integer cycleId)  // ?cycleId=3 (optional)
    {
        // STEP 1: Extract JWT context from request attributes
        // JwtAuthFilter already validated JWT and extracted these values
        String role = (String) httpReq.getAttribute("userRole");  // "ADMIN", "MANAGER", or "EMPLOYEE"
        Integer currentUserId = (Integer) httpReq.getAttribute("userId");  // User ID from JWT
        
        // STEP 2: Apply filtering logic based on parameters and role
        List<PerformanceReview> reviews;
        
        if (cycleId != null) {
            // SCENARIO 1: Filter by review cycle
            // Any role can filter by cycle (shows all reviews in that cycle)
            reviews = reviewSvc.getReviewsByCycle(cycleId);
        } 
        else if (userId != null && role.equals("ADMIN")) {
            // SCENARIO 2: Admin requests specific user's reviews
            // Only admins can view other users' reviews
            reviews = reviewSvc.getReviewsByUser(userId);
        } 
        else {
            // SCENARIO 3: Default - return current user's own reviews
            // Employees/Managers see only their own reviews
            reviews = reviewSvc.getReviewsByUser(currentUserId);
        }
        
        // STEP 3: Wrap in ApiResponse and return
        // ApiResponse provides consistent response format:
        // {
        //   "message": "Reviews retrieved",
        //   "data": [...review objects...],
        //   "timestamp": "2024-01-29T10:30:00"
        // }
        return ApiResponse.success("Reviews retrieved", reviews);
    }
    
    
    // ========================================================================================
    // ENDPOINT 2: GET REVIEW BY ID
    // ========================================================================================
    /**
     * Retrieves a single performance review by its unique ID.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/performance-reviews/{reviewId}
     * 
     * PATH VARIABLE:
     * - reviewId: Unique identifier of the review
     * 
     * URL EXAMPLE:
     * GET /api/v1/performance-reviews/42
     *     → Returns review with ID 42
     * 
     * BUSINESS USE CASE:
     * - User clicks "View Details" on a review in the list
     * - Frontend needs full review data for display
     * - Used when editing or acknowledging a review
     * 
     * SECURITY:
     * - JWT required (enforced by JwtAuthFilter)
     * - No role restriction at controller level
     * - Service layer may add authorization checks
     * 
     * ERROR HANDLING:
     * - If reviewId doesn't exist, service throws ResourceNotFoundException
     * - GlobalExceptionHandler converts to 404 Not Found response
     * 
     * @param reviewId - Path variable from URL
     * @return ApiResponse containing single PerformanceReview object
     */
    @GetMapping("/{reviewId}")  // {reviewId} is a path variable (placeholder in URL)
    // Maps to: GET /api/v1/performance-reviews/42
    public ApiResponse<PerformanceReview> getReviewById(
            @PathVariable Integer reviewId)  // @PathVariable extracts {reviewId} from URL
    {
        // STEP 1: Call service to retrieve review
        PerformanceReview review = reviewSvc.getReviewById(reviewId);
        // Service handles validation and throws exception if not found
        
        // STEP 2: Return wrapped response
        return ApiResponse.success("Review retrieved", review);
    }
    
    
    // ========================================================================================
    // ENDPOINT 3: SUBMIT SELF-ASSESSMENT (EMPLOYEE ONLY)
    // ========================================================================================
    /**
     * Employee submits their self-assessment for a review cycle.
     * This is the FIRST STEP in the performance review workflow.
     * 
     * API SPECIFICATION:
     * Method: POST
     * URL: /api/v1/performance-reviews
     * Authorization: EMPLOYEE role required
     * 
     * REQUEST BODY (JSON):
     * {
     *   "cycleId": 3,
     *   "selfAssmt": "I completed all my goals this quarter...",
     *   "selfRating": 4
     * }
     * 
     * BUSINESS WORKFLOW:
     * 1. Employee writes self-assessment during review period
     * 2. Employee rates their own performance (1-5)
     * 3. System saves assessment and notifies manager
     * 4. System links all completed goals to this review
     * 
     * VALIDATION:
     * - @Valid annotation triggers Jakarta validation on SelfAssessmentRequest
     * - Checks @NotNull, @NotBlank, @Min, @Max annotations in DTO
     * - If validation fails, Spring returns 400 Bad Request automatically
     * 
     * AUTHORIZATION:
     * - @PreAuthorize("hasRole('EMPLOYEE')") = Only EMPLOYEE role can access
     * - Checked BEFORE method executes
     * - Non-employees get 403 Forbidden response
     * 
     * SECURITY:
     * - JWT token required
     * - Employee ID extracted from JWT (cannot be faked)
     * - Employees can only submit their own assessments
     * 
     * @param req - SelfAssessmentRequest DTO validated by @Valid
     * @param httpReq - HttpServletRequest to extract employee ID from JWT
     * @return ApiResponse containing saved PerformanceReview object
     */
    @PostMapping  // Maps to: POST /api/v1/performance-reviews
    @PreAuthorize("hasRole('EMPLOYEE')")  // AUTHORIZATION: Only employees allowed
    // If user's role is not EMPLOYEE, method never executes → 403 Forbidden
    public ApiResponse<PerformanceReview> submitSelfAssessment(
            @Valid @RequestBody SelfAssessmentRequest req,  
            // @Valid: Validate DTO before method executes
            // @RequestBody: Deserialize JSON from request body to Java object
            HttpServletRequest httpReq)  // Access JWT-extracted user context
    {
        // STEP 1: Extract employee ID from JWT context
        Integer empId = (Integer) httpReq.getAttribute("userId");
        // JWT guarantees this is the authenticated user's ID (cannot be spoofed)
        
        // STEP 2: Call service to handle business logic
        PerformanceReview review = reviewSvc.submitSelfAssessment(req, empId);
        // Service handles:
        // - Validation (cycle exists, no duplicate submission)
        // - Saving review
        // - Linking completed goals
        // - Notifying manager
        // - Creating audit log
        
        // STEP 3: Return success response
        return ApiResponse.success("Self-assessment submitted", review);
        // Frontend receives:
        // {
        //   "message": "Self-assessment submitted",
        //   "data": {...review object with all fields...},
        //   "timestamp": "2024-01-29T10:30:00"
        // }
    }
    
    
    // ========================================================================================
    // ENDPOINT 4: UPDATE SELF-ASSESSMENT DRAFT (EMPLOYEE ONLY)
    // ========================================================================================
    /**
     * Employee updates their self-assessment before manager reviews it.
     * 
     * API SPECIFICATION:
     * Method: PUT
     * URL: /api/v1/performance-reviews/{reviewId}/draft
     * Authorization: EMPLOYEE role required
     * 
     * PATH VARIABLE:
     * - reviewId: ID of the review to update
     * 
     * REQUEST BODY (JSON):
     * {
     *   "cycleId": 3,
     *   "selfAssmt": "Updated assessment text...",
     *   "selfRating": 5
     * }
     * 
     * URL EXAMPLE:
     * PUT /api/v1/performance-reviews/42/draft
     * 
     * BUSINESS USE CASE:
     * - Employee submitted assessment but wants to revise it
     * - Employee wants to add more details before manager sees it
     * - Allows iterative improvement
     * 
     * BUSINESS RULES:
     * - Can only update own review (authorization check in service)
     * - Can only update if manager hasn't reviewed yet
     * - Status does NOT change (stays in current state)
     * 
     * AUTHORIZATION:
     * - Role check: EMPLOYEE only
     * - Ownership check: Service verifies review belongs to requesting employee
     * 
     * @param reviewId - Path variable: which review to update
     * @param req - Updated self-assessment data
     * @param httpReq - HttpServletRequest for employee ID extraction
     * @return ApiResponse containing updated PerformanceReview object
     */
    @PutMapping("/{reviewId}/draft")  // PUT = Update existing resource
    // Maps to: PUT /api/v1/performance-reviews/42/draft
    @PreAuthorize("hasRole('EMPLOYEE')")  // Only employees can update drafts
    public ApiResponse<PerformanceReview> updateDraft(
            @PathVariable Integer reviewId,  // Extract reviewId from URL path
            @Valid @RequestBody SelfAssessmentRequest req,  // Validate and deserialize JSON
            HttpServletRequest httpReq)  // Access JWT context
    {
        // STEP 1: Extract employee ID from JWT
        Integer empId = (Integer) httpReq.getAttribute("userId");
        
        // STEP 2: Call service to update draft
        PerformanceReview review = reviewSvc.updateSelfAssessmentDraft(reviewId, req, empId);
        // Service validates:
        // - Review exists
        // - Employee owns this review
        // - Review is still in editable state
        
        // STEP 3: Return success response
        return ApiResponse.success("Draft updated", review);
    }
    
    
    // ========================================================================================
    // ENDPOINT 5: SUBMIT MANAGER REVIEW (MANAGER ONLY)
    // ========================================================================================
    /**
     * Manager submits their review for an employee.
     * This is the SECOND STEP in the performance review workflow.
     * 
     * API SPECIFICATION:
     * Method: PUT
     * URL: /api/v1/performance-reviews/{reviewId}
     * Authorization: MANAGER role required
     * 
     * REQUEST BODY (JSON):
     * {
     *   "mgrFb": "Employee exceeded expectations in...",
     *   "mgrRating": 4,
     *   "ratingJust": "Rating justified because...",
     *   "compRec": "Recommend 5% raise and $2000 bonus",
     *   "nextGoals": "Goals for next quarter include..."
     * }
     * 
     * URL EXAMPLE:
     * PUT /api/v1/performance-reviews/42
     * 
     * BUSINESS WORKFLOW:
     * 1. Manager reviews employee's self-assessment
     * 2. Manager reviews linked completed goals (evidence)
     * 3. Manager writes detailed feedback
     * 4. Manager assigns rating (may differ from self-rating)
     * 5. Manager provides justification for rating
     * 6. Manager makes compensation recommendations
     * 7. Manager sets goals for next period
     * 8. System notifies employee review is complete
     * 
     * BUSINESS RULES:
     * - Self-assessment must be completed first (enforced in service)
     * - Only employee's direct manager can review (enforced in service)
     * - Status changes: SELF_ASSESSMENT_COMPLETED → COMPLETED
     * 
     * AUTHORIZATION:
     * - Role check: MANAGER only (at controller level)
     * - Manager ownership check: Service verifies manager relationship
     * 
     * @param reviewId - ID of review to complete
     * @param req - ManagerReviewRequest with all manager input
     * @param httpReq - HttpServletRequest for manager ID extraction
     * @return ApiResponse containing completed PerformanceReview object
     */
    @PutMapping("/{reviewId}")  // PUT = Complete/update the review
    // Maps to: PUT /api/v1/performance-reviews/42
    @PreAuthorize("hasRole('MANAGER')")  // Only managers can submit reviews
    public ApiResponse<PerformanceReview> submitManagerReview(
            @PathVariable Integer reviewId,  // Which review to complete
            @Valid @RequestBody ManagerReviewRequest req,  // Manager's input (validated)
            HttpServletRequest httpReq)  // Access manager context
    {
        // STEP 1: Extract manager ID from JWT
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        
        // STEP 2: Call service to complete review
        PerformanceReview review = reviewSvc.submitManagerReview(reviewId, req, mgrId);
        // Service validates:
        // - Review exists
        // - Manager is employee's actual manager
        // - Self-assessment is completed
        // Service performs:
        // - Save manager feedback and rating
        // - Update review status to COMPLETED
        // - Notify employee
        // - Create audit log
        
        // STEP 3: Return success response
        return ApiResponse.success("Manager review submitted", review);
    }
    
    
    // ========================================================================================
    // ENDPOINT 6: ACKNOWLEDGE REVIEW (EMPLOYEE ONLY)
    // ========================================================================================
    /**
     * Employee acknowledges they have read their performance review.
     * This is the FINAL STEP in the performance review workflow.
     * 
     * API SPECIFICATION:
     * Method: POST
     * URL: /api/v1/performance-reviews/{reviewId}/acknowledge
     * Authorization: EMPLOYEE role required
     * 
     * REQUEST BODY (JSON):
     * {
     *   "response": "Thank you for the feedback. I agree with the assessment..."
     * }
     * 
     * URL EXAMPLE:
     * POST /api/v1/performance-reviews/42/acknowledge
     * 
     * BUSINESS PURPOSE:
     * - Legal/HR requirement: Employee must acknowledge receipt
     * - Provides record that employee was informed
     * - Allows employee to respond or comment
     * - Completes the review cycle
     * 
     * BUSINESS RULES:
     * - Manager must have completed review first (enforced in service)
     * - Only own review can be acknowledged (enforced in service)
     * - Status changes: COMPLETED → COMPLETED_AND_ACKNOWLEDGED
     * - Manager is notified of acknowledgment
     * 
     * AUTHORIZATION:
     * - Role check: EMPLOYEE only
     * - Ownership check: Service validates employee owns this review
     * 
     * REQUEST BODY STRUCTURE:
     * Uses Map<String, String> instead of dedicated DTO because:
     * - Only one simple field (response)
     * - No complex validation needed
     * - More flexible than creating single-field DTO
     * 
     * @param reviewId - ID of review to acknowledge
     * @param body - Map containing "response" key with employee's optional comments
     * @param httpReq - HttpServletRequest for employee ID extraction
     * @return ApiResponse containing acknowledged PerformanceReview object
     */
    @PostMapping("/{reviewId}/acknowledge")  // POST = Create acknowledgment action
    // Maps to: POST /api/v1/performance-reviews/42/acknowledge
    @PreAuthorize("hasRole('EMPLOYEE')")  // Only employees can acknowledge their reviews
    public ApiResponse<PerformanceReview> acknowledgeReview(
            @PathVariable Integer reviewId,  // Which review to acknowledge
            @RequestBody Map<String, String> body,  // Simple key-value: {"response": "..."}
            HttpServletRequest httpReq)  // Access employee context
    {
        // STEP 1: Extract employee ID from JWT
        Integer empId = (Integer) httpReq.getAttribute("userId");
        
        // STEP 2: Extract optional response from request body
        String response = body.get("response");  // Employee's optional comments
        // May be null if employee doesn't want to respond
        
        // STEP 3: Call service to acknowledge review
        PerformanceReview review = reviewSvc.acknowledgeReview(reviewId, empId, response);
        // Service validates:
        // - Review exists
        // - Employee owns this review
        // - Review is completed by manager
        // Service performs:
        // - Record acknowledgment with timestamp
        // - Save employee response
        // - Update status to COMPLETED_AND_ACKNOWLEDGED
        // - Notify manager
        // - Create audit log
        
        // STEP 4: Return success response
        return ApiResponse.success("Review acknowledged", review);
        
        // WORKFLOW NOW COMPLETE:
        // PENDING → SELF_ASSESSMENT_COMPLETED → COMPLETED → COMPLETED_AND_ACKNOWLEDGED ✓
    }
}


/**
 * =============================================================================================
 * CONTROLLER DESIGN PATTERNS SUMMARY
 * =============================================================================================
 * 
 * 1. THIN CONTROLLER PATTERN
 *    - Controller has minimal logic
 *    - Only handles HTTP concerns (routing, validation, response wrapping)
 *    - All business logic delegated to service layer
 * 
 * 2. REST API CONVENTIONS
 *    - GET: Retrieve resources (getReviews, getReviewById)
 *    - POST: Create new resources (submitSelfAssessment, acknowledgeReview)
 *    - PUT: Update existing resources (updateDraft, submitManagerReview)
 * 
 * 3. CONSISTENT RESPONSE FORMAT
 *    - All methods return ApiResponse<T>
 *    - Provides uniform JSON structure
 *    - Includes message, data, and timestamp
 * 
 * 4. ROLE-BASED ACCESS CONTROL
 *    - @PreAuthorize at method level for role checks
 *    - Additional authorization in service layer
 *    - JWT provides authenticated user context
 * 
 * 5. VALIDATION STRATEGY
 *    - @Valid on request bodies for field validation
 *    - DTO-level constraints (@NotNull, @NotBlank, etc.)
 *    - Service-level business rule validation
 * 
 * 6. PATH DESIGN
 *    - Base path: /api/v1/performance-reviews
 *    - Subresources: /{reviewId}/draft, /{reviewId}/acknowledge
 *    - Version in URL (/v1) for future API evolution
 * 
 * =============================================================================================
 * REST API ENDPOINTS SUMMARY
 * =============================================================================================
 * 
 * 1. GET /api/v1/performance-reviews
 *    - Get reviews (filtered by userId or cycleId)
 *    - All roles (with different data visibility)
 * 
 * 2. GET /api/v1/performance-reviews/{reviewId}
 *    - Get single review by ID
 *    - All roles (service may add auth checks)
 * 
 * 3. POST /api/v1/performance-reviews
 *    - Submit self-assessment (EMPLOYEE only)
 *    - First step of workflow
 * 
 * 4. PUT /api/v1/performance-reviews/{reviewId}/draft
 *    - Update self-assessment draft (EMPLOYEE only)
 *    - Before manager reviews
 * 
 * 5. PUT /api/v1/performance-reviews/{reviewId}
 *    - Submit manager review (MANAGER only)
 *    - Second step of workflow
 * 
 * 6. POST /api/v1/performance-reviews/{reviewId}/acknowledge
 *    - Acknowledge review (EMPLOYEE only)
 *    - Final step of workflow
 * 
 * =============================================================================================
 * DATA FLOW EXAMPLE
 * =============================================================================================
 * 
 * Frontend → POST /api/v1/performance-reviews
 *         ↓
 * JwtAuthFilter (validates JWT, extracts userId and role)
 *         ↓
 * PerformanceReviewController.submitSelfAssessment()
 *         ↓ (calls)
 * PerformanceReviewService.submitSelfAssessment()
 *         ↓ (calls)
 * PerformanceReviewRepository.save()
 * NotificationRepository.save()
 * AuditLogRepository.save()
 *         ↓ (executes SQL)
 * MySQL Database
 *         ↓ (returns data)
 * Controller wraps in ApiResponse
 *         ↓
 * Frontend receives JSON response
 * 
 * =============================================================================================

 */


 /**
 * =============================================================================================
 * REVIEW CYCLE SERVICE - COMPREHENSIVE EXPLANATION
 * =============================================================================================
 * 
 * PURPOSE:
 * Manages review cycles - the time periods when performance reviews are conducted.
 * Examples: "Q1 2024 Reviews", "Annual Review 2024", "Mid-Year Check-in"
 * 
 * BUSINESS CONTEXT:
 * Organizations conduct performance reviews on a schedule:
 * - Quarterly reviews (every 3 months)
 * - Annual reviews (once per year)
 * - Mid-year reviews (6-month intervals)
 * 
 * A review cycle defines:
 * - When reviews start and end
 * - Whether the cycle is active, planned, or completed
 * - Whether evidence/approval is required
 * 
 * RELATIONSHIP TO OTHER ENTITIES:
 * ReviewCycle (1) ←→ (Many) PerformanceReview
 * - One cycle can have many employee reviews
 * - Each review belongs to exactly one cycle
 * 
 * TYPICAL WORKFLOW:
 * 1. Admin creates new review cycle (e.g., "Q1 2024")
 * 2. Admin sets start and end dates
 * 3. Cycle status: PLANNED
 * 4. When start date arrives, admin changes status to ACTIVE
 * 5. Employees submit self-assessments during active period
 * 6. Managers submit reviews during active period
 * 7. When end date passes, admin changes status to COMPLETED
 * 8. Next cycle is created
 * 
 * =============================================================================================
 */

package com.project.performanceTrack.service;

import com.project.performanceTrack.dto.CreateReviewCycleRequest;
import com.project.performanceTrack.entity.AuditLog;
import com.project.performanceTrack.entity.ReviewCycle;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.enums.ReviewCycleStatus;
import com.project.performanceTrack.exception.ResourceNotFoundException;
import com.project.performanceTrack.repository.AuditLogRepository;
import com.project.performanceTrack.repository.ReviewCycleRepository;
import com.project.performanceTrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing review cycles - the time periods when performance reviews occur.
 */
@Service  // Marks this as Spring service component
public class ReviewCycleService {
    
    // ========================================================================================
    // DEPENDENCY INJECTION
    // ========================================================================================
    
    @Autowired
    private ReviewCycleRepository cycleRepo;  // CRUD operations on review cycles
    
    @Autowired
    private UserRepository userRepo;  // For retrieving admin user details
    
    @Autowired
    private AuditLogRepository auditRepo;  // For tracking admin actions
    
    
    // ========================================================================================
    // METHOD 1: GET ALL REVIEW CYCLES
    // ========================================================================================
    /**
     * Retrieves all review cycles in the system.
     * 
     * BUSINESS USE CASE:
     * - Admin wants to see all past, current, and planned review cycles
     * - Dashboard showing review cycle history
     * - Dropdown for selecting which cycle to view
     * 
     * RETURNS:
     * List of all ReviewCycle objects, ordered by creation date
     * Example: [Q1 2023, Q2 2023, Q3 2023, Q4 2023, Q1 2024, Q2 2024]
     * 
     * @return List of all ReviewCycle objects
     */
    public List<ReviewCycle> getAllCycles() {
        // findAll() is built-in JPA method
        // SELECT * FROM review_cycle
        return cycleRepo.findAll();
    }
    
    
    // ========================================================================================
    // METHOD 2: GET CYCLE BY ID
    // ========================================================================================
    /**
     * Retrieves a specific review cycle by its unique ID.
     * 
     * BUSINESS USE CASE:
     * - User clicks on "Q1 2024" to see details
     * - System needs to load specific cycle data for editing
     * 
     * ERROR HANDLING:
     * Throws ResourceNotFoundException if cycle doesn't exist.
     * This provides clear error message instead of returning null.
     * 
     * @param cycleId - Unique identifier of the review cycle
     * @return ReviewCycle object with full details
     * @throws ResourceNotFoundException if cycle not found
     */
    public ReviewCycle getCycleById(Integer cycleId) {
        return cycleRepo.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Review cycle not found"));
    }
    
    
    // ========================================================================================
    // METHOD 3: GET ACTIVE REVIEW CYCLE
    // ========================================================================================
    /**
     * Retrieves the currently active review cycle.
     * 
     * BUSINESS USE CASE:
     * - Employee wants to submit self-assessment for current cycle
     * - System needs to know which cycle is accepting submissions
     * - Dashboard showing "Current Review Period: Q1 2024"
     * 
     * BUSINESS LOGIC:
     * - Only ONE cycle should be ACTIVE at a time
     * - Returns most recent active cycle (by start date)
     * - If no active cycle exists, throws exception
     * 
     * QUERY BREAKDOWN:
     * findFirstByStatusOrderByStartDateDesc:
     * - findFirst: Get only one result (the first match)
     * - ByStatus: WHERE status = ACTIVE
     * - OrderByStartDateDesc: ORDER BY start_date DESC (newest first)
     * 
     * WHY ORDER BY START DATE?
     * In case admin accidentally activates multiple cycles,
     * we get the most recent one (fail-safe mechanism).
     * 
     * @return Currently active ReviewCycle
     * @throws ResourceNotFoundException if no active cycle exists
     */
    public ReviewCycle getActiveCycle() {
        return cycleRepo.findFirstByStatusOrderByStartDateDesc(ReviewCycleStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active review cycle found"));
        // This exception tells employees: "No review cycle is open for submissions right now"
    }
    
    
    // ========================================================================================
    // METHOD 4: CREATE REVIEW CYCLE (ADMIN ONLY)
    // ========================================================================================
    /**
     * Creates a new review cycle.
     * 
     * BUSINESS USE CASE:
     * - Admin plans next quarter's review cycle
     * - Setting up annual performance review period
     * - Defining mid-year check-in cycle
     * 
     * DETAILED PROCESS:
     * Step 1: Extract data from request DTO
     * Step 2: Create new ReviewCycle entity
     * Step 3: Set all required fields
     * Step 4: Save to database
     * Step 5: Create audit log for compliance
     * 
     * BUSINESS FIELDS:
     * - title: "Q1 2024 Performance Reviews"
     * - startDate: When cycle opens (e.g., Jan 1, 2024)
     * - endDate: When cycle closes (e.g., Mar 31, 2024)
     * - status: PLANNED, ACTIVE, or COMPLETED
     * - requiresCompletionApproval: Manager must approve goal completion?
     * - evidenceRequired: Must employees provide evidence for goals?
     * 
     * AUDIT TRAIL:
     * Creates log entry: "Admin [name] created review cycle Q1 2024 on [timestamp]"
     * This is important for compliance and tracking.
     * 
     * @param req - CreateReviewCycleRequest with cycle details
     * @param adminId - ID of admin creating the cycle
     * @return Saved ReviewCycle object with generated ID
     */
    public ReviewCycle createCycle(CreateReviewCycleRequest req, Integer adminId) {
        
        // STEP 1: Create new ReviewCycle entity
        ReviewCycle cycle = new ReviewCycle();
        
        // STEP 2: Map request DTO fields to entity
        cycle.setTitle(req.getTitle());                    // "Q1 2024 Reviews"
        cycle.setStartDate(req.getStartDt());              // 2024-01-01
        cycle.setEndDate(req.getEndDt());                  // 2024-03-31
        cycle.setStatus(req.getStatus());                  // PLANNED/ACTIVE/COMPLETED
        
        // Business configuration fields
        cycle.setRequiresCompletionApproval(req.getReqCompAppr());  // true = manager approval needed
        cycle.setEvidenceRequired(req.getEvReq());                  // true = evidence required
        
        // STEP 3: Save to database
        // INSERT INTO review_cycle (title, start_date, end_date, ...) VALUES (...)
        ReviewCycle saved = cycleRepo.save(cycle);
        // After save, 'saved' object has generated cycleId
        
        // STEP 4: Create audit log entry
        User admin = userRepo.findById(adminId).orElse(null);  // Get admin user object
        AuditLog log = new AuditLog();
        log.setUser(admin);                             // Who performed the action
        log.setAction("REVIEW_CYCLE_CREATED");          // What happened
        log.setDetails("Created review cycle: " + cycle.getTitle());  // Context
        log.setRelatedEntityType("ReviewCycle");        // Type of entity affected
        log.setRelatedEntityId(saved.getCycleId());     // Specific entity ID
        log.setStatus("SUCCESS");                       // Operation succeeded
        log.setTimestamp(LocalDateTime.now());          // When it happened
        auditRepo.save(log);
        // Creates permanent record for auditing/compliance
        
        return saved;
    }
    
    
    // ========================================================================================
    // METHOD 5: UPDATE REVIEW CYCLE (ADMIN ONLY)
    // ========================================================================================
    /**
     * Updates an existing review cycle.
     * 
     * BUSINESS USE CASE:
     * - Admin needs to extend cycle end date
     * - Admin wants to change cycle status from PLANNED to ACTIVE
     * - Admin needs to update title or configuration
     * 
     * DETAILED PROCESS:
     * Step 1: Retrieve existing cycle from database
     * Step 2: Update all fields with new values
     * Step 3: Save changes
     * Step 4: Create audit log
     * 
     * COMMON UPDATE SCENARIOS:
     * 
     * Scenario 1: Activate a planned cycle
     * - Change status: PLANNED → ACTIVE
     * - Employees can now submit self-assessments
     * 
     * Scenario 2: Extend cycle duration
     * - Change endDate: 2024-03-31 → 2024-04-15
     * - Give employees more time to complete reviews
     * 
     * Scenario 3: Close a cycle
     * - Change status: ACTIVE → COMPLETED
     * - No more submissions accepted
     * 
     * Scenario 4: Change evidence requirement
     * - Change evidenceRequired: false → true
     * - Now employees must provide proof for goal completion
     * 
     * AUDIT TRAIL:
     * Every update is logged for compliance.
     * Example log: "Admin [name] updated Q1 2024 cycle on [timestamp]"
     * 
     * @param cycleId - ID of cycle to update
     * @param req - CreateReviewCycleRequest with updated values
     * @param adminId - ID of admin making changes
     * @return Updated ReviewCycle object
     * @throws ResourceNotFoundException if cycle doesn't exist
     */
    public ReviewCycle updateCycle(Integer cycleId, CreateReviewCycleRequest req, Integer adminId) {
        
        // STEP 1: Retrieve existing cycle
        ReviewCycle cycle = getCycleById(cycleId);
        // Throws exception if cycle doesn't exist
        
        // STEP 2: Update all fields with new values
        cycle.setTitle(req.getTitle());                    // May change title
        cycle.setStartDate(req.getStartDt());              // May extend start
        cycle.setEndDate(req.getEndDt());                  // May extend deadline
        cycle.setStatus(req.getStatus());                  // May activate/close cycle
        cycle.setRequiresCompletionApproval(req.getReqCompAppr());  // May change approval requirement
        cycle.setEvidenceRequired(req.getEvReq());                  // May change evidence requirement
        
        // STEP 3: Save updated entity
        // UPDATE review_cycle SET title=?, start_date=?, ... WHERE cycle_id=?
        ReviewCycle updated = cycleRepo.save(cycle);
        
        // STEP 4: Create audit log
        User admin = userRepo.findById(adminId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(admin);
        log.setAction("REVIEW_CYCLE_UPDATED");          // Action type
        log.setDetails("Updated review cycle: " + cycle.getTitle());
        log.setRelatedEntityType("ReviewCycle");
        log.setRelatedEntityId(cycleId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
}


/**
 * =============================================================================================
 * REVIEW CYCLE CONTROLLER - COMPREHENSIVE EXPLANATION
 * =============================================================================================
 * 
 * PURPOSE:
 * REST API endpoints for managing review cycles.
 * Provides CRUD operations for admin cycle management.
 * 
 * BASE URL: /api/v1/review-cycles
 * 
 * AUTHORIZATION:
 * - Most endpoints require ADMIN role
 * - Some GET endpoints accessible to all authenticated users
 * 
 * =============================================================================================
 */

package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.dto.CreateReviewCycleRequest;
import com.project.performanceTrack.entity.ReviewCycle;
import com.project.performanceTrack.service.ReviewCycleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for review cycle management.
 */
@RestController
@RequestMapping("/api/v1/review-cycles")  // Base path for all endpoints
public class ReviewCycleController {
    
    @Autowired
    private ReviewCycleService cycleSvc;  // Delegate business logic to service
    
    
    // ========================================================================================
    // ENDPOINT 1: GET ALL REVIEW CYCLES
    // ========================================================================================
    /**
     * Retrieves all review cycles.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/review-cycles
     * Authorization: Any authenticated user (no role restriction)
     * 
     * BUSINESS USE CASE:
     * - Admin viewing all cycles for management
     * - Employee selecting which cycle to view reviews for
     * - Manager checking cycle schedule
     * 
     * URL EXAMPLE:
     * GET http://localhost:8080/api/v1/review-cycles
     * 
     * RESPONSE:
     * {
     *   "message": "Review cycles retrieved",
     *   "data": [
     *     {
     *       "cycleId": 1,
     *       "title": "Q1 2024 Reviews",
     *       "startDate": "2024-01-01",
     *       "endDate": "2024-03-31",
     *       "status": "ACTIVE",
     *       "requiresCompletionApproval": true,
     *       "evidenceRequired": true
     *     },
     *     {...more cycles...}
     *   ],
     *   "timestamp": "2024-01-29T10:30:00"
     * }
     * 
     * @return ApiResponse containing list of all ReviewCycle objects
     */
    @GetMapping  // Maps to: GET /api/v1/review-cycles
    public ApiResponse<List<ReviewCycle>> getAllCycles() {
        // Call service to retrieve all cycles
        List<ReviewCycle> cycles = cycleSvc.getAllCycles();
        
        // Wrap in consistent response format
        return ApiResponse.success("Review cycles retrieved", cycles);
    }
    
    
    // ========================================================================================
    // ENDPOINT 2: GET CYCLE BY ID
    // ========================================================================================
    /**
     * Retrieves a specific review cycle by ID.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/review-cycles/{cycleId}
     * Authorization: Any authenticated user
     * 
     * PATH VARIABLE:
     * - cycleId: Unique identifier of the cycle
     * 
     * URL EXAMPLE:
     * GET http://localhost:8080/api/v1/review-cycles/5
     * 
     * BUSINESS USE CASE:
     * - View details of specific review cycle
     * - Admin editing cycle configuration
     * - Employee checking cycle requirements
     * 
     * @param cycleId - Path variable extracted from URL
     * @return ApiResponse containing single ReviewCycle object
     */
    @GetMapping("/{cycleId}")  // Maps to: GET /api/v1/review-cycles/5
    public ApiResponse<ReviewCycle> getCycleById(@PathVariable Integer cycleId) {
        // Retrieve specific cycle
        ReviewCycle cycle = cycleSvc.getCycleById(cycleId);
        
        return ApiResponse.success("Review cycle retrieved", cycle);
    }
    
    
    // ========================================================================================
    // ENDPOINT 3: GET ACTIVE CYCLE
    // ========================================================================================
    /**
     * Retrieves the currently active review cycle.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/review-cycles/active
     * Authorization: Any authenticated user
     * 
     * URL EXAMPLE:
     * GET http://localhost:8080/api/v1/review-cycles/active
     * 
     * BUSINESS USE CASE:
     * - Employee wants to submit self-assessment for current cycle
     * - System determining which cycle to use for new reviews
     * - Dashboard showing current review period
     * 
     * SPECIAL ENDPOINT PATH:
     * /active is a specific resource (not an ID)
     * This must be defined BEFORE /{cycleId} in code order
     * Otherwise Spring would treat "active" as a cycleId parameter
     * 
     * ERROR SCENARIO:
     * If no active cycle exists:
     * - Service throws ResourceNotFoundException
     * - GlobalExceptionHandler converts to 404 Not Found
     * - Response: "No active review cycle found"
     * 
     * @return ApiResponse containing active ReviewCycle object
     */
    @GetMapping("/active")  // Maps to: GET /api/v1/review-cycles/active
    // NOTE: This MUST be defined before /{cycleId} endpoint
    public ApiResponse<ReviewCycle> getActiveCycle() {
        // Get currently active cycle
        ReviewCycle cycle = cycleSvc.getActiveCycle();
        
        return ApiResponse.success("Active cycle retrieved", cycle);
    }
    
    
    // ========================================================================================
    // ENDPOINT 4: CREATE REVIEW CYCLE (ADMIN ONLY)
    // ========================================================================================
    /**
     * Creates a new review cycle.
     * 
     * API SPECIFICATION:
     * Method: POST
     * URL: /api/v1/review-cycles
     * Authorization: ADMIN role required
     * 
     * REQUEST BODY (JSON):
     * {
     *   "title": "Q2 2024 Performance Reviews",
     *   "startDt": "2024-04-01",
     *   "endDt": "2024-06-30",
     *   "status": "PLANNED",
     *   "reqCompAppr": true,
     *   "evReq": true
     * }
     * 
     * VALIDATION:
     * @Valid annotation triggers validation on CreateReviewCycleRequest:
     * - title: @NotBlank (cannot be empty)
     * - startDt: @NotNull (must be provided)
     * - endDt: @NotNull (must be provided)
     * - status: @NotNull (must be valid ReviewCycleStatus enum)
     * 
     * If validation fails, Spring returns 400 Bad Request with details.
     * 
     * AUTHORIZATION:
     * @PreAuthorize("hasRole('ADMIN')") ensures only admins can create cycles.
     * Non-admin users get 403 Forbidden response.
     * 
     * @param req - Validated CreateReviewCycleRequest DTO
     * @param httpReq - HttpServletRequest to extract admin ID from JWT
     * @return ApiResponse containing created ReviewCycle object
     */
    @PostMapping  // Maps to: POST /api/v1/review-cycles
    @PreAuthorize("hasRole('ADMIN')")  // ADMIN role required
    public ApiResponse<ReviewCycle> createCycle(
            @Valid @RequestBody CreateReviewCycleRequest req,  // Validate and deserialize JSON
            HttpServletRequest httpReq)  // Access admin context
    {
        // Extract admin ID from JWT
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        
        // Call service to create cycle
        ReviewCycle cycle = cycleSvc.createCycle(req, adminId);
        // Service handles saving and audit logging
        
        return ApiResponse.success("Review cycle created", cycle);
    }
    
    
    // ========================================================================================
    // ENDPOINT 5: UPDATE REVIEW CYCLE (ADMIN ONLY)
    // ========================================================================================
    /**
     * Updates an existing review cycle.
     * 
     * API SPECIFICATION:
     * Method: PUT
     * URL: /api/v1/review-cycles/{cycleId}
     * Authorization: ADMIN role required
     * 
     * PATH VARIABLE:
     * - cycleId: ID of cycle to update
     * 
     * REQUEST BODY (JSON):
     * {
     *   "title": "Q2 2024 Performance Reviews (Extended)",
     *   "startDt": "2024-04-01",
     *   "endDt": "2024-07-15",  // Extended deadline
     *   "status": "ACTIVE",      // Changed from PLANNED to ACTIVE
     *   "reqCompAppr": true,
     *   "evReq": false           // Changed evidence requirement
     * }
     * 
     * URL EXAMPLE:
     * PUT http://localhost:8080/api/v1/review-cycles/5
     * 
     * BUSINESS USE CASES:
     * - Activate a planned cycle (PLANNED → ACTIVE)
     * - Extend cycle duration (change endDt)
     * - Close a cycle (ACTIVE → COMPLETED)
     * - Modify cycle configuration
     * 
     * AUTHORIZATION:
     * Only admins can update cycles.
     * 
     * @param cycleId - Path variable: which cycle to update
     * @param req - Validated CreateReviewCycleRequest with updated values
     * @param httpReq - HttpServletRequest to extract admin ID
     * @return ApiResponse containing updated ReviewCycle object
     */
    @PutMapping("/{cycleId}")  // Maps to: PUT /api/v1/review-cycles/5
    @PreAuthorize("hasRole('ADMIN')")  // ADMIN role required
    public ApiResponse<ReviewCycle> updateCycle(
            @PathVariable Integer cycleId,  // Which cycle to update
            @Valid @RequestBody CreateReviewCycleRequest req,  // Updated data
            HttpServletRequest httpReq)  // Admin context
    {
        // Extract admin ID from JWT
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        
        // Call service to update cycle
        ReviewCycle cycle = cycleSvc.updateCycle(cycleId, req, adminId);
        // Service handles validation, saving, and audit logging
        
        return ApiResponse.success("Review cycle updated", cycle);
    }
}


/**
 * =============================================================================================
 * DTO EXPLANATION: CreateReviewCycleRequest
 * =============================================================================================
 */

package com.project.performanceTrack.dto;

import com.project.performanceTrack.enums.ReviewCycleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for creating or updating review cycles.
 * 
 * PURPOSE:
 * - Define structure of request body for cycle creation/update
 * - Apply validation rules to incoming data
 * - Separate API contract from database entity
 * 
 * VALIDATION ANNOTATIONS:
 * @NotBlank - Field cannot be null or empty string
 * @NotNull - Field cannot be null (but can be empty for strings)
 * 
 * LOMBOK ANNOTATIONS:
 * @Data - Generates getters, setters, toString, equals, hashCode
 * 
 * WHY USE DTO INSTEAD OF ENTITY?
 * 1. API contract stability - Can change entity without breaking API
 * 2. Validation rules - Apply different rules than entity
 * 3. Security - Don't expose internal entity structure
 * 4. Field mapping - API fields may differ from database columns
 */
@Data  // Lombok: Generate getters, setters, etc.
public class CreateReviewCycleRequest {
    
    /**
     * Title of the review cycle.
     * Examples: "Q1 2024 Reviews", "Annual Review 2024", "Mid-Year Check-in"
     * 
     * Validation: Cannot be null or empty
     */
    @NotBlank(message = "Title is required")
    private String title;
    
    /**
     * Start date of the review cycle.
     * This is when employees can begin submitting self-assessments.
     * 
     * Type: LocalDate (date without time)
     * Format: "2024-01-01"
     * 
     * Validation: Cannot be null
     */
    @NotNull(message = "Start date is required")
    private LocalDate startDt;
    
    /**
     * End date of the review cycle.
     * This is the deadline for all review submissions.
     * 
     * Type: LocalDate (date without time)
     * Format: "2024-03-31"
     * 
     * Validation: Cannot be null
     */
    @NotNull(message = "End date is required")
    private LocalDate endDt;
    
    /**
     * Status of the review cycle.
     * Enum values: PLANNED, ACTIVE, COMPLETED
     * 
     * - PLANNED: Cycle is scheduled but not yet started
     * - ACTIVE: Cycle is currently accepting submissions
     * - COMPLETED: Cycle has ended, no more submissions
     * 
     * Validation: Cannot be null
     */
    @NotNull(message = "Status is required")
    private ReviewCycleStatus status;
    
    /**
     * Whether goal completion requires manager approval.
     * 
     * Default: true
     * If true: Employee marks goal complete → Manager must approve
     * If false: Employee can mark goal complete without approval
     * 
     * Business impact: Controls approval workflow
     */
    private Boolean reqCompAppr = true;  // Default value
    
    /**
     * Whether employees must provide evidence for goal completion.
     * 
     * Default: true
     * If true: Employee must upload documents/proof when completing goals
     * If false: Employee can complete goals without evidence
     * 
     * Business impact: Affects goal verification rigor
     */
    private Boolean evReq = true;  // Default value
}


/**
 * =============================================================================================
 * SUMMARY - REVIEW CYCLE MANAGEMENT
 * =============================================================================================
 * 
 * SERVICE METHODS:
 * 1. getAllCycles() - List all cycles
 * 2. getCycleById(cycleId) - Get specific cycle
 * 3. getActiveCycle() - Get currently active cycle
 * 4. createCycle(request, adminId) - Create new cycle (admin only)
 * 5. updateCycle(cycleId, request, adminId) - Update cycle (admin only)
 * 
 * REST API ENDPOINTS:
 * 1. GET /api/v1/review-cycles - List all cycles
 * 2. GET /api/v1/review-cycles/{cycleId} - Get specific cycle
 * 3. GET /api/v1/review-cycles/active - Get active cycle
 * 4. POST /api/v1/review-cycles - Create cycle (admin)
 * 5. PUT /api/v1/review-cycles/{cycleId} - Update cycle (admin)
 * 
 * TYPICAL ADMIN WORKFLOW:
 * 1. Create new cycle with status=PLANNED
 * 2. Wait for start date to arrive
 * 3. Update cycle to status=ACTIVE
 * 4. Employees/managers submit reviews during active period
 * 5. When end date passes, update status=COMPLETED
 * 6. Create next cycle
 * 
 * =============================================================================================

 */

 /**
 * =============================================================================================
 * REPOSITORY LAYER - COMPREHENSIVE EXPLANATION
 * =============================================================================================
 * 
 * PURPOSE:
 * Repositories are the DATA ACCESS LAYER in Spring Boot applications.
 * They handle all database operations (CRUD + custom queries).
 * 
 * THREE-LAYER ARCHITECTURE:
 * Controller Layer (REST API) ← Handles HTTP requests/responses
 *     ↓ calls
 * Service Layer (Business Logic) ← Handles workflows and business rules
 *     ↓ calls
 * Repository Layer (Data Access) ← YOU ARE HERE - Handles database operations
 *     ↓ executes SQL
 * Database (MySQL) ← Physical data storage
 * 
 * SPRING DATA JPA MAGIC:
 * - Repository interfaces extend JpaRepository
 * - Spring automatically implements these interfaces at runtime
 * - No need to write SQL manually for standard operations
 * - Custom query methods generated from method names
 * 
 * KEY CONCEPTS:
 * 1. JpaRepository provides built-in methods:
 *    - findAll() → SELECT * FROM table
 *    - findById(id) → SELECT * FROM table WHERE id = ?
 *    - save(entity) → INSERT or UPDATE
 *    - deleteById(id) → DELETE FROM table WHERE id = ?
 *    - count() → SELECT COUNT(*) FROM table
 * 
 * 2. Query Method Naming Convention:
 *    Method name defines the query automatically:
 *    - findBy[Field] → WHERE field = ?
 *    - findBy[Field1]And[Field2] → WHERE field1 = ? AND field2 = ?
 *    - findBy[Field]OrderBy[Field2]Desc → WHERE ... ORDER BY ... DESC
 *    - findFirstBy[Field] → WHERE ... LIMIT 1
 * 
 * 3. Relationship Navigation:
 *    Underscore (_) navigates relationships:
 *    - findByUser_UserId → SELECT * WHERE user_id = ?
 *    - findByCycle_CycleId → SELECT * WHERE cycle_id = ?
 * 
 * =============================================================================================
 */


/**
 * =============================================================================================
 * PERFORMANCE REVIEW REPOSITORY
 * =============================================================================================
 */

package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.PerformanceReview;
import com.project.performanceTrack.enums.PerformanceReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PerformanceReview entity - handles all review database operations.
 * 
 * ENTITY MAPPING:
 * This repository manages the 'performance_review' table in MySQL.
 * 
 * INHERITED METHODS (from JpaRepository<PerformanceReview, Integer>):
 * - findAll() → Get all reviews
 * - findById(id) → Get review by ID
 * - save(review) → Create or update review
 * - deleteById(id) → Delete review
 * - count() → Count total reviews
 * - existsById(id) → Check if review exists
 * 
 * CUSTOM QUERY METHODS:
 * Spring Data JPA automatically generates SQL from method names.
 */
@Repository  // Marks this as a Spring Data repository component
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Integer> {
    // JpaRepository<PerformanceReview, Integer>
    //              ↑                 ↑
    //              Entity type       Primary key type
    
    
    /**
     * Find all reviews for a specific user.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * User_ - Navigate to User entity relationship
     * UserId - Filter by userId field in User entity
     * 
     * GENERATED SQL:
     * SELECT * FROM performance_review pr
     * JOIN user u ON pr.user_id = u.user_id
     * WHERE u.user_id = ?
     * 
     * BUSINESS USE CASE:
     * - Get all reviews for employee ID 5
     * - Show employee their review history
     * 
     * @param userId - ID of the user (employee)
     * @return List of all PerformanceReview objects for that user
     */
    List<PerformanceReview> findByUser_UserId(Integer userId);
    // Example call: findByUser_UserId(5)
    // Returns all reviews where user_id = 5
    
    
    /**
     * Find all reviews in a specific review cycle.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * Cycle_ - Navigate to ReviewCycle entity relationship
     * CycleId - Filter by cycleId field in ReviewCycle entity
     * 
     * GENERATED SQL:
     * SELECT * FROM performance_review pr
     * JOIN review_cycle rc ON pr.cycle_id = rc.cycle_id
     * WHERE rc.cycle_id = ?
     * 
     * BUSINESS USE CASE:
     * - Get all reviews submitted for "Q1 2024" cycle
     * - Admin checking cycle completion status
     * 
     * @param cycleId - ID of the review cycle
     * @return List of all PerformanceReview objects in that cycle
     */
    List<PerformanceReview> findByCycle_CycleId(Integer cycleId);
    // Example call: findByCycle_CycleId(3)
    // Returns all reviews for cycle_id = 3
    
    
    /**
     * Find all reviews with a specific status.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * Status - Filter by status field
     * 
     * GENERATED SQL:
     * SELECT * FROM performance_review
     * WHERE status = ?
     * 
     * BUSINESS USE CASE:
     * - Find all reviews awaiting manager review (SELF_ASSESSMENT_COMPLETED)
     * - Count how many reviews are completed (COMPLETED)
     * 
     * @param status - PerformanceReviewStatus enum value
     * @return List of all PerformanceReview objects with that status
     */
    List<PerformanceReview> findByStatus(PerformanceReviewStatus status);
    // Example call: findByStatus(PerformanceReviewStatus.COMPLETED)
    // Returns all reviews with status = 'COMPLETED'
    
    
    /**
     * Find review for specific user in specific cycle.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * Cycle_CycleId - Navigate to cycle, filter by cycleId
     * And - Combine conditions with AND
     * User_UserId - Navigate to user, filter by userId
     * 
     * GENERATED SQL:
     * SELECT * FROM performance_review pr
     * JOIN review_cycle rc ON pr.cycle_id = rc.cycle_id
     * JOIN user u ON pr.user_id = u.user_id
     * WHERE rc.cycle_id = ? AND u.user_id = ?
     * 
     * BUSINESS USE CASE:
     * - Check if employee already has review for current cycle
     * - Prevent duplicate review submissions
     * 
     * RETURN TYPE:
     * Optional<PerformanceReview> - May or may not exist
     * - If found: Optional contains the review
     * - If not found: Optional is empty
     * 
     * @param cycleId - ID of review cycle
     * @param userId - ID of user
     * @return Optional containing review if it exists, empty otherwise
     */
    Optional<PerformanceReview> findByCycle_CycleIdAndUser_UserId(Integer cycleId, Integer userId);
    // Example call: findByCycle_CycleIdAndUser_UserId(3, 5)
    // Returns review where cycle_id = 3 AND user_id = 5 (if exists)
    
    // NOTE: No custom @Query annotations needed!
    // Spring Data JPA generates all SQL automatically from method names
}


/**
 * =============================================================================================
 * REVIEW CYCLE REPOSITORY
 * =============================================================================================
 */

package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.ReviewCycle;
import com.project.performanceTrack.enums.ReviewCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ReviewCycle entity - handles review cycle database operations.
 * 
 * ENTITY MAPPING:
 * This repository manages the 'review_cycle' table in MySQL.
 * 
 * INHERITED METHODS:
 * Same as PerformanceReviewRepository (findAll, save, etc.)
 */
@Repository
public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, Integer> {
    
    
    /**
     * Find all review cycles with a specific status.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * Status - Filter by status field
     * 
     * GENERATED SQL:
     * SELECT * FROM review_cycle
     * WHERE status = ?
     * 
     * BUSINESS USE CASE:
     * - Get all ACTIVE cycles
     * - Get all COMPLETED cycles for reporting
     * - Get all PLANNED cycles to schedule
     * 
     * @param status - ReviewCycleStatus enum value
     * @return List of ReviewCycle objects with that status
     */
    List<ReviewCycle> findByStatus(ReviewCycleStatus status);
    // Example call: findByStatus(ReviewCycleStatus.ACTIVE)
    // Returns all cycles where status = 'ACTIVE'
    
    
    /**
     * Find first (most recent) active review cycle.
     * 
     * METHOD NAME BREAKDOWN:
     * findFirst - Get only first result (LIMIT 1)
     * By - Start filtering
     * Status - Filter by status field
     * OrderBy - Add ORDER BY clause
     * StartDate - Sort by startDate field
     * Desc - Descending order (newest first)
     * 
     * GENERATED SQL:
     * SELECT * FROM review_cycle
     * WHERE status = ?
     * ORDER BY start_date DESC
     * LIMIT 1
     * 
     * BUSINESS LOGIC:
     * Should only be ONE active cycle at a time.
     * If multiple exist (admin error), get most recent.
     * 
     * RETURN TYPE:
     * Optional<ReviewCycle> - May not exist if no active cycle
     * 
     * BUSINESS USE CASE:
     * - Employee wants to submit self-assessment for current cycle
     * - System determining which cycle is accepting submissions
     * 
     * @param status - ReviewCycleStatus (usually ACTIVE)
     * @return Optional containing most recent cycle with that status
     */
    Optional<ReviewCycle> findFirstByStatusOrderByStartDateDesc(ReviewCycleStatus status);
    // Example call: findFirstByStatusOrderByStartDateDesc(ReviewCycleStatus.ACTIVE)
    // Returns most recent active cycle (or empty Optional if none)
}


/**
 * =============================================================================================
 * PERFORMANCE REVIEW GOALS REPOSITORY
 * =============================================================================================
 */

package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.PerformanceReviewGoals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PerformanceReviewGoals entity - manages review-goal linking.
 * 
 * ENTITY MAPPING:
 * This repository manages the 'performance_review_goals' junction table.
 * This is a MANY-TO-MANY relationship table:
 * - One review can have many goals
 * - One goal can be part of many reviews
 * 
 * TABLE STRUCTURE:
 * performance_review_goals
 * - link_id (PK)
 * - review_id (FK → performance_review)
 * - goal_id (FK → goal)
 * - linked_date (timestamp)
 * 
 * BUSINESS PURPOSE:
 * When employee submits self-assessment, system automatically links
 * all their COMPLETED goals to that review as evidence of performance.
 */
@Repository
public interface PerformanceReviewGoalsRepository extends JpaRepository<PerformanceReviewGoals, Integer> {
    
    
    /**
     * Find all goal links for a specific review.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * Review_ - Navigate to Review entity relationship
     * ReviewId - Filter by reviewId field in Review entity
     * 
     * GENERATED SQL:
     * SELECT * FROM performance_review_goals prg
     * JOIN performance_review pr ON prg.review_id = pr.review_id
     * WHERE pr.review_id = ?
     * 
     * BUSINESS USE CASE:
     * - Display all goals completed during a review period
     * - Show evidence of employee's performance
     * 
     * EXAMPLE:
     * Review #42 might be linked to goals:
     * - Goal #10: "Implement new feature"
     * - Goal #15: "Improve code coverage"
     * - Goal #20: "Mentor junior developer"
     * 
     * @param reviewId - ID of the performance review
     * @return List of PerformanceReviewGoals linking records
     */
    List<PerformanceReviewGoals> findByReview_ReviewId(Integer reviewId);
    // Returns all goal links for review_id = ?
    
    
    /**
     * Find all reviews that reference a specific goal.
     * 
     * METHOD NAME BREAKDOWN:
     * findBy - Starts query method
     * Goal_ - Navigate to Goal entity relationship
     * GoalId - Filter by goalId field in Goal entity
     * 
     * GENERATED SQL:
     * SELECT * FROM performance_review_goals prg
     * JOIN goal g ON prg.goal_id = g.goal_id
     * WHERE g.goal_id = ?
     * 
     * BUSINESS USE CASE:
     * - See which reviews mention a specific goal
     * - Track goal's impact across multiple review cycles
     * 
     * EXAMPLE:
     * Goal #10 might be referenced in:
     * - Review #40 (Q1 2024)
     * - Review #50 (Q2 2024) - if extended
     * 
     * @param goalId - ID of the goal
     * @return List of PerformanceReviewGoals linking records
     */
    List<PerformanceReviewGoals> findByGoal_GoalId(Integer goalId);
    // Returns all review links for goal_id = ?
}


/**
 * =============================================================================================
 * REPORT CONTROLLER - COMPREHENSIVE EXPLANATION
 * =============================================================================================
 * 
 * PURPOSE:
 * Provides analytics and reporting endpoints for performance data.
 * This is part of the Analytics & Reporting module.
 * 
 * BUSINESS VALUE:
 * Answers critical business questions:
 * - How are employees performing overall?
 * - Which departments are meeting their goals?
 * - What percentage of reviews are completed?
 * - How many goals are in progress vs completed?
 * - What's the distribution of performance ratings?
 * 
 * STAKEHOLDERS:
 * - Admins: Full system analytics
 * - Managers: Team performance metrics
 * - Employees: Personal dashboard
 * 
 * BASE URL: /api/v1/reports
 * 
 * AUTHORIZATION:
 * Most endpoints require ADMIN or MANAGER roles.
 * Regular employees have limited access.
 * 
 * =============================================================================================
 */

package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.entity.Report;
import com.project.performanceTrack.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for analytics and reporting.
 * Provides dashboard metrics and performance analytics.
 */
@RestController
@RequestMapping("/api/v1/reports")  // Base URL
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")  // Controller-level authorization
// All endpoints require ADMIN or MANAGER role (except where overridden)
public class ReportController {
    
    @Autowired
    private ReportService reportSvc;  // Business logic for analytics
    
    
    // ========================================================================================
    // ENDPOINT 1: GET ALL REPORTS
    // ========================================================================================
    /**
     * Retrieves all generated reports in the system.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/reports
     * Authorization: ADMIN or MANAGER
     * 
     * BUSINESS USE CASE:
     * - View history of generated reports
     * - Access previously generated analytics
     * - Audit report generation activity
     * 
     * REPORT TYPES:
     * Reports might include:
     * - Monthly performance summaries
     * - Department comparison reports
     * - Goal completion analytics
     * - Review cycle statistics
     * 
     * @return ApiResponse containing list of all Report objects
     */
    @GetMapping  // Maps to: GET /api/v1/reports
    public ApiResponse<List<Report>> getAllReports() {
        // Retrieve all reports from database
        List<Report> reports = reportSvc.getAllReports();
        
        return ApiResponse.success("Reports retrieved", reports);
    }
    
    
    // ========================================================================================
    // ENDPOINT 2: GET REPORT BY ID
    // ========================================================================================
    /**
     * Retrieves a specific report by its ID.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/reports/{reportId}
     * Authorization: ADMIN or MANAGER
     * 
     * URL EXAMPLE:
     * GET /api/v1/reports/42
     * 
     * BUSINESS USE CASE:
     * - View details of a specific generated report
     * - Download a previously generated report
     * 
     * @param reportId - Unique identifier of the report
     * @return ApiResponse containing single Report object
     */
    @GetMapping("/{reportId}")  // Maps to: GET /api/v1/reports/42
    public ApiResponse<Report> getReportById(@PathVariable Integer reportId) {
        // Retrieve specific report
        Report report = reportSvc.getReportById(reportId);
        
        return ApiResponse.success("Report retrieved", report);
    }
    
    
    // ========================================================================================
    // ENDPOINT 3: GENERATE REPORT
    // ========================================================================================
    /**
     * Generates a new report based on specified parameters.
     * 
     * API SPECIFICATION:
     * Method: POST
     * URL: /api/v1/reports/generate
     * Authorization: ADMIN or MANAGER
     * 
     * REQUEST BODY (JSON):
     * {
     *   "scope": "DEPARTMENT",        // DEPARTMENT, TEAM, INDIVIDUAL, COMPANY
     *   "metrics": "PERFORMANCE",     // PERFORMANCE, GOALS, REVIEWS
     *   "format": "PDF"               // PDF, EXCEL, CSV
     * }
     * 
     * BUSINESS USE CASE:
     * - Manager generates monthly team performance report
     * - Admin generates quarterly company-wide analytics
     * - Department head generates goal completion summary
     * 
     * REPORT PARAMETERS:
     * - scope: Level of aggregation (department, team, individual, company)
     * - metrics: What to measure (performance ratings, goal completion, review status)
     * - format: Output format (PDF for viewing, Excel for analysis, CSV for import)
     * 
     * PROCESS:
     * 1. Extract parameters from request body
     * 2. Call service to generate report (may take time for large datasets)
     * 3. Save report to database with generated content
     * 4. Return report object with download link/data
     * 
     * @param body - Map containing report parameters
     * @param httpReq - HttpServletRequest for user context
     * @return ApiResponse containing generated Report object
     */
    @PostMapping("/generate")  // Maps to: POST /api/v1/reports/generate
    public ApiResponse<Report> generateReport(
            @RequestBody Map<String, String> body,  // Report parameters
            HttpServletRequest httpReq)  // User context
    {
        // Extract user ID (who generated the report)
        Integer userId = (Integer) httpReq.getAttribute("userId");
        
        // Extract report parameters from request body
        String scope = body.get("scope");           // DEPARTMENT, TEAM, etc.
        String metrics = body.get("metrics");       // PERFORMANCE, GOALS, etc.
        String format = body.getOrDefault("format", "PDF");  // Default to PDF
        
        // Call service to generate report
        // This might:
        // - Query database for relevant data
        // - Calculate statistics and aggregations
        // - Generate PDF/Excel file
        // - Save report metadata to database
        Report report = reportSvc.generateReport(scope, metrics, format, userId);
        
        return ApiResponse.success("Report generated", report);
    }
    
    
    // ========================================================================================
    // ENDPOINT 4: GET DASHBOARD METRICS
    // ========================================================================================
    /**
     * Retrieves role-based dashboard metrics for current user.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/reports/dashboard
     * Authorization: ADMIN or MANAGER (or EMPLOYEE with different data)
     * 
     * BUSINESS VALUE:
     * Provides key metrics for user's dashboard home page.
     * Different data based on role.
     * 
     * EMPLOYEE DASHBOARD METRICS:
     * - My total goals: 15
     * - My completed goals: 8
     * - My pending reviews: 1
     * - My average rating: 4.2
     * 
     * MANAGER DASHBOARD METRICS:
     * - Team goals: 75
     * - Team completion rate: 65%
     * - Reviews awaiting my action: 5
     * - Team average rating: 4.0
     * - Direct reports: 12
     * 
     * ADMIN DASHBOARD METRICS:
     * - Total employees: 250
     * - Active review cycle: Q1 2024
     * - Reviews completed: 180/250 (72%)
     * - Company average rating: 3.9
     * - Goals completion rate: 68%
     * - Pending reviews: 70
     * 
     * RESPONSE FORMAT:
     * {
     *   "message": "Dashboard metrics retrieved",
     *   "data": {
     *     "totalGoals": 15,
     *     "completedGoals": 8,
     *     "pendingReviews": 1,
     *     "avgRating": 4.2,
     *     "recentActivity": [...]
     *   }
     * }
     * 
     * @param httpReq - HttpServletRequest to extract userId and userRole
     * @return ApiResponse containing Map with dashboard metrics
     */
    @GetMapping("/dashboard")  // Maps to: GET /api/v1/reports/dashboard
    public ApiResponse<Map<String, Object>> getDashboard(HttpServletRequest httpReq) {
        // Extract user context
        String role = (String) httpReq.getAttribute("userRole");    // ADMIN/MANAGER/EMPLOYEE
        Integer userId = (Integer) httpReq.getAttribute("userId");  // Current user ID
        
        // Get role-based metrics
        // Service determines what data to return based on role
        Map<String, Object> metrics = reportSvc.getDashboardMetrics(userId, role);
        
        return ApiResponse.success("Dashboard metrics retrieved", metrics);
    }
    
    
    // ========================================================================================
    // ENDPOINT 5: GET PERFORMANCE SUMMARY
    // ========================================================================================
    /**
     * Retrieves performance summary with optional filtering.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/reports/performance-summary
     * Authorization: ADMIN or MANAGER
     * 
     * QUERY PARAMETERS (optional):
     * - cycleId: Filter by review cycle
     * - dept: Filter by department
     * 
     * URL EXAMPLES:
     * GET /api/v1/reports/performance-summary
     *     → Overall performance summary
     * GET /api/v1/reports/performance-summary?cycleId=5
     *     → Performance for Q1 2024 cycle
     * GET /api/v1/reports/performance-summary?dept=Engineering
     *     → Engineering department performance
     * GET /api/v1/reports/performance-summary?cycleId=5&dept=Engineering
     *     → Engineering performance in Q1 2024
     * 
     * BUSINESS QUESTIONS ANSWERED:
     * - What's the average performance rating this cycle?
     * - How many employees rated 4+ vs below 3?
     * - What percentage of reviews are completed?
     * - How does this cycle compare to previous?
     * 
     * RESPONSE DATA:
     * {
     *   "totalReviews": 180,
     *   "completedReviews": 150,
     *   "avgRating": 3.9,
     *   "ratingDistribution": {
     *     "5": 30,  // 30 employees rated 5
     *     "4": 80,  // 80 employees rated 4
     *     "3": 50,  // etc.
     *     "2": 15,
     *     "1": 5
     *   },
     *   "completionRate": 83.3  // percentage
     * }
     * 
     * @param cycleId - Optional: Filter by specific review cycle
     * @param dept - Optional: Filter by department name
     * @return ApiResponse containing performance summary Map
     */
    @GetMapping("/performance-summary")  // Maps to: GET /api/v1/reports/performance-summary
    public ApiResponse<Map<String, Object>> getPerformanceSummary(
            @RequestParam(required = false) Integer cycleId,  // Optional filter
            @RequestParam(required = false) String dept)      // Optional filter
    {
        // Call service to calculate performance metrics
        // Service queries database, calculates averages, distributions
        Map<String, Object> summary = reportSvc.getPerformanceSummary(cycleId, dept);
        
        return ApiResponse.success("Performance summary retrieved", summary);
    }
    
    
    // ========================================================================================
    // ENDPOINT 6: GET GOAL ANALYTICS
    // ========================================================================================
    /**
     * Retrieves goal tracking and completion analytics.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/reports/goal-analytics
     * Authorization: ADMIN or MANAGER
     * 
     * BUSINESS QUESTIONS ANSWERED:
     * - How many goals are in each status?
     * - What's the overall goal completion rate?
     * - Which goal types are most common?
     * - What's the average time to complete goals?
     * 
     * RESPONSE DATA:
     * {
     *   "totalGoals": 450,
     *   "goalsByStatus": {
     *     "NOT_STARTED": 50,
     *     "IN_PROGRESS": 200,
     *     "COMPLETED": 180,
     *     "CANCELLED": 20
     *   },
     *   "completionRate": 40.0,  // 180/450 = 40%
     *   "goalsByCategory": {
     *     "TECHNICAL": 200,
     *     "LEADERSHIP": 100,
     *     "LEARNING": 150
     *   },
     *   "avgCompletionTime": 45  // days
     * }
     * 
     * @return ApiResponse containing goal analytics Map
     */
    @GetMapping("/goal-analytics")  // Maps to: GET /api/v1/reports/goal-analytics
    public ApiResponse<Map<String, Object>> getGoalAnalytics() {
        // Call service to aggregate goal data
        Map<String, Object> analytics = reportSvc.getGoalAnalytics();
        
        return ApiResponse.success("Goal analytics retrieved", analytics);
    }
    
    
    // ========================================================================================
    // ENDPOINT 7: GET DEPARTMENT PERFORMANCE
    // ========================================================================================
    /**
     * Retrieves performance comparison across all departments.
     * 
     * API SPECIFICATION:
     * Method: GET
     * URL: /api/v1/reports/department-performance
     * Authorization: ADMIN or MANAGER
     * 
     * BUSINESS USE CASE:
     * - Compare performance across departments
     * - Identify high-performing and struggling departments
     * - Resource allocation decisions
     * - Identify best practices from top departments
     * 
     * RESPONSE DATA (List of department objects):
     * [
     *   {
     *     "department": "Engineering",
     *     "employeeCount": 80,
     *     "avgRating": 4.1,
     *     "goalCompletionRate": 75.0,
     *     "reviewCompletionRate": 90.0
     *   },
     *   {
     *     "department": "Sales",
     *     "employeeCount": 50,
     *     "avgRating": 3.8,
     *     "goalCompletionRate": 68.0,
     *     "reviewCompletionRate": 85.0
     *   },
     *   {
     *     "department": "Marketing",
     *     "employeeCount": 40,
     *     "avgRating": 4.0,
     *     "goalCompletionRate": 72.0,
     *     "reviewCompletionRate": 88.0
     *   }
     * ]
     * 
     * SORTING:
     * Typically sorted by avgRating DESC (best performing first)
     * 
     * @return ApiResponse containing List of department performance Maps
     */
    @GetMapping("/department-performance")  // Maps to: GET /api/v1/reports/department-performance
    public ApiResponse<List<Map<String, Object>>> getDeptPerformance() {
        // Call service to aggregate by department
        // Service groups data by department, calculates metrics
        List<Map<String, Object>> performance = reportSvc.getDepartmentPerformance();
        
        return ApiResponse.success("Department performance retrieved", performance);
    }
}


/**
 * =============================================================================================
 * ANALYTICS & REPORTING MODULE SUMMARY
 * =============================================================================================
 * 
 * PURPOSE:
 * Provides data-driven insights for decision making at all organizational levels.
 * 
 * REST API ENDPOINTS:
 * 1. GET /api/v1/reports
 *    - List all generated reports
 * 
 * 2. GET /api/v1/reports/{reportId}
 *    - Get specific report details
 * 
 * 3. POST /api/v1/reports/generate
 *    - Generate new custom report
 * 
 * 4. GET /api/v1/reports/dashboard
 *    - Get role-based dashboard metrics
 * 
 * 5. GET /api/v1/reports/performance-summary
 *    - Get performance analytics (filterable)
 * 
 * 6. GET /api/v1/reports/goal-analytics
 *    - Get goal tracking statistics
 * 
 * 7. GET /api/v1/reports/department-performance
 *    - Compare department performance
 * 
 * KEY BUSINESS QUESTIONS ANSWERED:
 * 
 * FOR EMPLOYEES:
 * - How am I performing compared to goals?
 * - What's my review status?
 * - How many goals have I completed?
 * 
 * FOR MANAGERS:
 * - How is my team performing?
 * - Who needs coaching or recognition?
 * - What's our goal completion rate?
 * - How do we compare to other teams?
 * 
 * FOR ADMINS:
 * - What's the company-wide performance trend?
 * - Which departments are excelling/struggling?
 * - Are reviews being completed on time?
 * - What's the distribution of ratings?
 * 
 * DATA VISUALIZATION:
 * These endpoints provide data for:
 * - Dashboard charts (bar, line, pie)
 * - Performance tables
 * - Comparison graphs
 * - Trend analysis
 * 
 * =============================================================================================
 * REPOSITORY LAYER SUMMARY
 * =============================================================================================
 * 
 * KEY CONCEPTS:
 * 
 * 1. SPRING DATA JPA MAGIC:
 *    - Extends JpaRepository<Entity, PrimaryKeyType>
 *    - Automatic implementation at runtime
 *    - No SQL writing needed for standard operations
 * 
 * 2. QUERY METHOD NAMING:
 *    - Method name defines the query
 *    - findBy[Field] → WHERE field = ?
 *    - findBy[Field1]And[Field2] → WHERE field1 = ? AND field2 = ?
 *    - findFirstBy[Field]OrderBy[Field2]Desc → WHERE ... ORDER BY ... LIMIT 1
 * 
 * 3. RELATIONSHIP NAVIGATION:
 *    - Underscore (_) navigates relationships
 *    - findByUser_UserId → Joins User table, filters by userId
 *    - findByCycle_CycleId → Joins ReviewCycle table, filters by cycleId
 * 
 * 4. BUILT-IN METHODS:
 *    - findAll(), findById(), save(), deleteById(), count(), existsById()
 *    - Automatically provided by JpaRepository
 * 
 * 5. OPTIONAL RETURN TYPE:
 *    - Used when result might not exist
 *    - Prevents NullPointerException
 *    - Use .orElse(), .orElseThrow(), .isPresent()
 * 
 * =============================================================================================
 */
