I'll add comprehensive comments to all three files explaining the Goal management system in detail:

## GoalService.java - Complete Annotated Version

```java
package com.project.performanceTrack.service;

import com.project.performanceTrack.dto.ApproveCompletionRequest;
import com.project.performanceTrack.dto.CreateGoalRequest;
import com.project.performanceTrack.dto.SubmitCompletionRequest;
import com.project.performanceTrack.entity.*;
import com.project.performanceTrack.enums.*;
import com.project.performanceTrack.exception.BadRequestException;
import com.project.performanceTrack.exception.ResourceNotFoundException;
import com.project.performanceTrack.exception.UnauthorizedException;
import com.project.performanceTrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GOAL SERVICE - Business Logic Layer for Goal Management
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PURPOSE:
 * Manages the complete lifecycle of employee performance goals in the system.
 * Implements all business rules, workflows, and state transitions for goals.
 * 
 * GOAL LIFECYCLE:
 * 1. PENDING          → Employee creates goal, awaits manager approval
 * 2. IN_PROGRESS      → Manager approved, employee working on it
 * 3. PENDING_COMPLETION_APPROVAL → Employee submitted completion with evidence
 * 4. COMPLETED        → Manager verified and approved completion (FINAL STATE)
 * 5. REJECTED         → Goal was deleted/rejected (TERMINAL STATE)
 * 
 * KEY FEATURES:
 * - Role-based authorization (Employee vs Manager actions)
 * - Evidence-based completion verification
 * - Notification system for all stakeholders
 * - Complete audit trail for compliance
 * - Change request workflow
 * - Progress tracking
 * 
 * PATTERN USED:
 * Every method follows the same pattern:
 * 1. Fetch required entities (goal, user, manager)
 * 2. Validate authorization (security checks)
 * 3. Validate business rules (status checks, date validation)
 * 4. Update goal state
 * 5. Create notifications for affected users
 * 6. Log action in audit trail
 * 7. Return updated entity
 */
@Service
public class GoalService {
    
    // ═══════════════════════════════════════════════════════════════════════
    // REPOSITORY DEPENDENCIES - Data Access Layer
    // ═══════════════════════════════════════════════════════════════════════
    
    @Autowired
    private GoalRepository goalRepo;  
    // Main goal CRUD operations and custom queries
    
    @Autowired
    private UserRepository userRepo;  
    // Access to employee and manager user data
    
    @Autowired
    private NotificationRepository notifRepo;  
    // Create notifications to inform users of goal events
    
    @Autowired
    private AuditLogRepository auditRepo;  
    // Record every action for compliance and tracking
    
    @Autowired
    private FeedbackRepository fbRepo;  
    // Store manager feedback and change requests
    
    @Autowired
    private GoalCompletionApprovalRepository approvalRepo;  
    // Formal records of completion approvals/rejections
    
    // ═══════════════════════════════════════════════════════════════════════
    // CREATE NEW GOAL - Employee Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * CREATE NEW GOAL (Employee initiates goal-setting process)
     * 
     * BUSINESS FLOW:
     * 1. Employee fills out goal form with title, description, timeline, priority
     * 2. Employee selects which manager will approve this goal
     * 3. Goal is created in PENDING status
     * 4. Manager receives notification with action required
     * 5. Manager must approve before employee can start work
     * 
     * WHY MANAGER APPROVAL?
     * - Ensures goals align with team/company objectives
     * - Manager can verify goals are realistic and measurable
     * - Provides opportunity for goal refinement before work begins
     * 
     * VALIDATION RULES:
     * - End date must be after start date (basic timeline logic)
     * - Both employee and manager must exist in system
     * - Employee ID comes from JWT token (cannot fake it)
     * 
     * SIDE EFFECTS:
     * - Notification sent to manager (appears in their dashboard)
     * - Audit log created for compliance tracking
     * - Goal now visible in employee's goal list
     * 
     * @param req Contains: title, description, category, priority, dates, managerId
     * @param empId ID of employee creating goal (from JWT authentication)
     * @return Newly created Goal entity with generated ID
     * @throws ResourceNotFoundException if employee or manager doesn't exist
     * @throws BadRequestException if dates are invalid
     */
    public Goal createGoal(CreateGoalRequest req, Integer empId) {
        // STEP 1: Fetch employee from database
        // Using orElseThrow ensures we fail fast if employee doesn't exist
        User emp = userRepo.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        // STEP 2: Fetch assigned manager
        // Manager is specified by employee when creating goal
        User mgr = userRepo.findById(req.getMgrId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        
        // STEP 3: Validate business rule - timeline must make sense
        if (req.getEndDt().isBefore(req.getStartDt())) {
            throw new BadRequestException("End date must be after start date");
        }
        
        // STEP 4: Create and populate new goal entity
        Goal goal = new Goal();
        goal.setTitle(req.getTitle());                    // e.g., "Complete Java certification"
        goal.setDescription(req.getDesc());               // Detailed explanation
        goal.setCategory(req.getCat());                   // e.g., SKILL_DEVELOPMENT, PRODUCTIVITY
        goal.setPriority(req.getPri());                   // HIGH, MEDIUM, LOW
        goal.setAssignedToUser(emp);                      // Employee who owns this goal
        goal.setAssignedManager(mgr);                     // Manager who will approve
        goal.setStartDate(req.getStartDt());              // When to begin
        goal.setEndDate(req.getEndDt());                  // Target completion date
        goal.setStatus(GoalStatus.PENDING);               // Awaiting manager approval
        
        // STEP 5: Persist to database
        // JPA will generate the goal_id and set timestamps
        Goal savedGoal = goalRepo.save(goal);
        
        // STEP 6: Notify manager - appears in their notification center
        Notification notif = new Notification();
        notif.setUser(mgr);                                          // Send to manager
        notif.setType(NotificationType.GOAL_SUBMITTED);              // Type determines icon/color in UI
        notif.setMessage(emp.getName() + " submitted goal: " + goal.getTitle());  // Human-readable message
        notif.setRelatedEntityType("Goal");                          // For linking back to goal
        notif.setRelatedEntityId(savedGoal.getGoalId());            // Clickable link in UI
        notif.setStatus(NotificationStatus.UNREAD);                  // Will show as unread
        notif.setPriority(req.getPri().name());                      // Match goal priority
        notif.setActionRequired(true);                               // Manager needs to approve/reject
        notifRepo.save(notif);
        
        // STEP 7: Create audit log entry
        // This is critical for compliance - who did what when
        AuditLog log = new AuditLog();
        log.setUser(emp);                                            // Who performed action
        log.setAction("GOAL_CREATED");                               // What they did
        log.setDetails("Created goal: " + goal.getTitle());         // Additional context
        log.setRelatedEntityType("Goal");                            // Entity type
        log.setRelatedEntityId(savedGoal.getGoalId());              // Which specific entity
        log.setStatus("SUCCESS");                                    // Action succeeded
        log.setTimestamp(LocalDateTime.now());                       // When it happened
        auditRepo.save(log);
        
        return savedGoal;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // READ/QUERY OPERATIONS - Fetching Goals
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * GET GOALS BY USER (Employee Dashboard View)
     * 
     * Returns all goals assigned to a specific employee.
     * Used to populate employee's "My Goals" dashboard.
     * 
     * SHOWS: All goals across all statuses (PENDING, IN_PROGRESS, COMPLETED, etc.)
     * 
     * @param userId ID of employee
     * @return List of all goals for this employee (could be empty list)
     */
    public List<Goal> getGoalsByUser(Integer userId) {
        // Uses Spring Data JPA method naming convention
        // findByAssignedToUser_UserId means:
        // - Look at the assignedToUser field (which is a User object)
        // - Navigate to that User's userId field
        // - Match it against the provided userId parameter
        return goalRepo.findByAssignedToUser_UserId(userId);
    }
    
    /**
     * GET GOALS BY MANAGER (Manager Dashboard View)
     * 
     * Returns all goals where this user is the assigned manager.
     * Used for manager's dashboard to see:
     * - Goals pending their approval
     * - Goals they're overseeing
     * - Completion submissions awaiting their review
     * 
     * @param mgrId ID of manager
     * @return List of all goals assigned to this manager
     */
    public List<Goal> getGoalsByManager(Integer mgrId) {
        // Similar naming convention as above but for manager relationship
        return goalRepo.findByAssignedManager_UserId(mgrId);
    }
    
    /**
     * GET SINGLE GOAL BY ID (Goal Details Page)
     * 
     * Used when user clicks on a goal to see full details.
     * Shows complete information including:
     * - Title, description, dates
     * - Current status and approval state
     * - Evidence links and completion notes
     * - Progress updates history
     * 
     * @param goalId Unique identifier for goal
     * @return Goal entity with all fields populated
     * @throws ResourceNotFoundException if goal doesn't exist (404 error in API)
     */
    public Goal getGoalById(Integer goalId) {
        return goalRepo.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // APPROVE GOAL - Manager Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * APPROVE GOAL (Manager accepts employee's proposed goal)
     * 
     * BUSINESS FLOW:
     * 1. Manager reviews goal details in their dashboard
     * 2. If goal is clear, realistic, and aligned with objectives, manager approves
     * 3. Status changes: PENDING → IN_PROGRESS
     * 4. Employee is now authorized to start working on the goal
     * 5. Employee receives success notification
     * 
     * WHY THIS MATTERS:
     * - Ensures goal alignment before work begins
     * - Prevents wasted effort on misaligned goals
     * - Creates clear authorization trail
     * - Establishes manager buy-in for goal completion
     * 
     * SECURITY:
     * - Only the assigned manager can approve (not any manager)
     * - Verified by comparing manager ID from JWT with assignedManager on goal
     * 
     * VALIDATION:
     * - Goal must be in PENDING status (can't re-approve approved goals)
     * - Manager must be the assigned manager (security check)
     * 
     * @param goalId Goal to approve
     * @param mgrId Manager ID from JWT token
     * @return Updated goal in IN_PROGRESS status
     * @throws UnauthorizedException if wrong manager tries to approve
     * @throws BadRequestException if goal isn't in PENDING status
     */
    public Goal approveGoal(Integer goalId, Integer mgrId) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY CHECK: Verify this manager is authorized
        // Prevents manager from approving goals assigned to other managers
        if (!goal.getAssignedManager().getUserId().equals(mgrId)) {
            throw new UnauthorizedException("Not authorized to approve this goal");
        }
        
        // BUSINESS RULE: Can only approve goals in PENDING status
        // Prevents re-approving already approved goals
        // Prevents approving completed goals, etc.
        if (!goal.getStatus().equals(GoalStatus.PENDING)) {
            throw new BadRequestException("Goal is not in pending status");
        }
        
        // UPDATE GOAL STATE - Approval granted
        goal.setStatus(GoalStatus.IN_PROGRESS);           // Now active
        goal.setApprovedBy(goal.getAssignedManager());    // Who approved it
        goal.setApprovedDate(LocalDateTime.now());         // When approval happened
        goal.setRequestChanges(false);                     // Clear any previous change requests
        Goal updated = goalRepo.save(goal);
        
        // NOTIFY EMPLOYEE - Good news!
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedToUser());          // Send to employee
        notif.setType(NotificationType.GOAL_APPROVED);    // Success notification type
        notif.setMessage("Your goal '" + goal.getTitle() + "' has been approved");
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notifRepo.save(notif);
        
        // AUDIT TRAIL - Record the approval
        User mgr = userRepo.findById(mgrId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(mgr);
        log.setAction("GOAL_APPROVED");
        log.setDetails("Approved goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REQUEST CHANGES - Manager Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * REQUEST CHANGES TO GOAL (Manager asks employee to revise goal)
     * 
     * BUSINESS FLOW:
     * 1. Manager reviews submitted goal
     * 2. Finds issues: unclear description, unrealistic timeline, wrong category, etc.
     * 3. Instead of rejecting outright, requests specific changes
     * 4. Employee receives feedback and can revise the goal
     * 5. Employee resubmits, manager reviews again
     * 
     * WHY NOT JUST REJECT?
     * - More collaborative approach
     * - Gives employee chance to fix issues
     * - Preserves the goal (doesn't start from scratch)
     * - Builds better employee-manager communication
     * 
     * TYPICAL CHANGE REQUESTS:
     * - "Please add measurable success criteria"
     * - "Timeline seems too aggressive, extend by 2 weeks"
     * - "Change category from PRODUCTIVITY to SKILL_DEVELOPMENT"
     * - "Add more detail about deliverables"
     * 
     * WORKFLOW STATE:
     * - Goal stays in current status (usually PENDING)
     * - Sets requestChanges flag to true
     * - Employee can only update goal when this flag is true
     * - Prevents unauthorized modifications to approved goals
     * 
     * @param goalId Goal needing revision
     * @param mgrId Manager requesting changes
     * @param comments Specific feedback on what needs to change
     * @return Updated goal with requestChanges flag set
     * @throws UnauthorizedException if wrong manager tries this action
     */
    public Goal requestChanges(Integer goalId, Integer mgrId, String comments) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY CHECK
        if (!goal.getAssignedManager().getUserId().equals(mgrId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // SET CHANGE REQUEST FLAG
        // This flag is the key - it unlocks the updateGoal method for employee
        goal.setRequestChanges(true);
        User mgr = userRepo.findById(mgrId).orElse(null);
        goal.setLastReviewedBy(mgr);                      // Track who reviewed
        goal.setLastReviewedDate(LocalDateTime.now());     // When they reviewed
        Goal updated = goalRepo.save(goal);
        
        // STORE FEEDBACK - Separate table for feedback history
        // This preserves all manager feedback over time
        Feedback fb = new Feedback();
        fb.setGoal(goal);                                  // Link to goal
        fb.setGivenByUser(mgr);                           // Who gave feedback
        fb.setComments(comments);                          // The actual feedback text
        fb.setFeedbackType("CHANGE_REQUEST");              // Type of feedback
        fb.setDate(LocalDateTime.now());                   // When given
        fbRepo.save(fb);
        
        // NOTIFY EMPLOYEE - Action required
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedToUser());
        notif.setType(NotificationType.GOAL_CHANGE_REQUESTED);
        notif.setMessage("Changes requested for goal: " + goal.getTitle());
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notifRepo.save(notif);
        
        // AUDIT TRAIL
        AuditLog log = new AuditLog();
        log.setUser(mgr);
        log.setAction("GOAL_CHANGE_REQUESTED");
        log.setDetails("Requested changes for goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // SUBMIT COMPLETION - Employee Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * SUBMIT GOAL COMPLETION WITH EVIDENCE (Employee claims goal is finished)
     * 
     * BUSINESS FLOW:
     * 1. Employee finishes working on goal
     * 2. Prepares evidence of completion (documents, reports, certificates, screenshots)
     * 3. Uploads evidence or provides link
     * 4. Writes completion notes explaining what was achieved
     * 5. Submits for manager verification
     * 6. Status changes: IN_PROGRESS → PENDING_COMPLETION_APPROVAL
     * 7. Manager must verify and approve before goal is officially complete
     * 
     * WHY EVIDENCE-BASED COMPLETION?
     * - Prevents employees from just clicking "complete" without proof
     * - Gives manager objective criteria to evaluate completion
     * - Creates documentation for performance reviews
     * - Ensures goals were actually achieved, not just claimed
     * 
     * EVIDENCE TYPES:
     * - Link to Google Drive document
     * - URL to deployed project
     * - Certificate/credential link
     * - Screenshots in shared folder
     * - Jira tickets showing completed work
     * 
     * CRITICAL FIELDS:
     * - evidenceLink: URL to proof of completion
     * - evidenceLinkDescription: What the evidence shows
     * - evidenceAccessInstructions: How manager can access (passwords, permissions)
     * - completionNotes: Employee's summary of what they accomplished
     * 
     * @param goalId Goal being completed
     * @param req Completion details (evidence link, notes, access info)
     * @param empId Employee submitting completion (from JWT)
     * @return Updated goal in PENDING_COMPLETION_APPROVAL status
     * @throws UnauthorizedException if wrong employee tries to submit
     * @throws BadRequestException if goal is not IN_PROGRESS
     */
    public Goal submitCompletion(Integer goalId, SubmitCompletionRequest req, Integer empId) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY: Only assigned employee can submit completion
        if (!goal.getAssignedToUser().getUserId().equals(empId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // BUSINESS RULE: Can only submit completion for IN_PROGRESS goals
        // Can't submit completion for pending, completed, or rejected goals
        if (!goal.getStatus().equals(GoalStatus.IN_PROGRESS)) {
            throw new BadRequestException("Goal is not in progress");
        }
        
        // UPDATE GOAL WITH COMPLETION EVIDENCE
        goal.setStatus(GoalStatus.PENDING_COMPLETION_APPROVAL);  // Waiting for manager
        goal.setEvidenceLink(req.getEvLink());                   // URL to evidence
        goal.setEvidenceLinkDescription(req.getLinkDesc());      // What evidence shows
        goal.setEvidenceAccessInstructions(req.getAccessInstr());// How to access
        goal.setCompletionNotes(req.getCompNotes());             // What was achieved
        goal.setCompletionSubmittedDate(LocalDateTime.now());    // When submitted
        goal.setCompletionApprovalStatus(CompletionApprovalStatus.PENDING);  // Awaiting approval
        goal.setEvidenceLinkVerificationStatus(EvidenceVerificationStatus.NOT_VERIFIED);  // Not checked yet
        Goal updated = goalRepo.save(goal);
        
        // HIGH-PRIORITY NOTIFICATION TO MANAGER
        // This is urgent - employee is waiting for verification
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedManager());
        notif.setType(NotificationType.GOAL_COMPLETION_SUBMITTED);
        notif.setMessage(goal.getAssignedToUser().getName() + " submitted completion for goal: " + goal.getTitle());
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setPriority("HIGH");                                // Urgent - needs attention
        notif.setActionRequired(true);                            // Manager must review
        notifRepo.save(notif);
        
        // AUDIT TRAIL
        User emp = userRepo.findById(empId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(emp);
        log.setAction("GOAL_COMPLETION_SUBMITTED");
        log.setDetails("Submitted completion for goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // APPROVE COMPLETION - Manager Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * APPROVE GOAL COMPLETION (Manager verifies and officially completes goal)
     * 
     * BUSINESS FLOW:
     * 1. Manager receives notification of completion submission
     * 2. Manager clicks evidence link and reviews proof
     * 3. Manager verifies employee truly achieved the goal
     * 4. If satisfied, manager approves completion
     * 5. Status changes: PENDING_COMPLETION_APPROVAL → COMPLETED (FINAL)
     * 6. GoalCompletionApproval record created (formal approval document)
     * 7. Employee receives congratulations notification
     * 8. Goal now counts toward performance metrics
     * 
     * WHY MANAGER VERIFICATION?
     * - Quality control - ensures standards were met
     * - Prevents gaming the system (claiming completion without actual work)
     * - Manager stakes their reputation on the approval
     * - Creates accountability for both employee and manager
     * 
     * WHAT MANAGER CHECKS:
     * - Evidence link works and is accessible
     * - Evidence actually demonstrates goal completion
     * - Quality meets expectations
     * - Goal was completed within timeline (or delay justified)
     * - All success criteria were met
     * 
     * COMPLETION APPROVAL RECORD:
     * - Separate table stores formal approval
     * - Includes manager comments
     * - Records evidence verification
     * - Provides audit trail for performance reviews
     * - Used in analytics and reporting
     * 
     * THIS IS PERMANENT:
     * - Once COMPLETED, goal cannot go back to other statuses
     * - This is the terminal success state
     * - Only alternative is REJECTED (terminal failure state)
     * 
     * @param goalId Goal to mark as completed
     * @param req Manager's comments on completion
     * @param mgrId Manager approving (from JWT)
     * @return Goal in COMPLETED status (terminal state)
     * @throws UnauthorizedException if wrong manager tries to approve
     * @throws BadRequestException if goal not in PENDING_COMPLETION_APPROVAL
     */
    public Goal approveCompletion(Integer goalId, ApproveCompletionRequest req, Integer mgrId) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY CHECK
        if (!goal.getAssignedManager().getUserId().equals(mgrId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // BUSINESS RULE: Must be pending completion approval
        if (!goal.getStatus().equals(GoalStatus.PENDING_COMPLETION_APPROVAL)) {
            throw new BadRequestException("Goal is not pending completion approval");
        }
        
        // MARK AS OFFICIALLY COMPLETED
        goal.setStatus(GoalStatus.COMPLETED);              // FINAL STATUS - terminal state
        goal.setCompletionApprovalStatus(CompletionApprovalStatus.APPROVED);
        User mgr = userRepo.findById(mgrId).orElse(null);
        goal.setCompletionApprovedBy(mgr);                 // Who approved
        goal.setCompletionApprovedDate(LocalDateTime.now());  // When approved
        goal.setFinalCompletionDate(LocalDateTime.now());  // Official completion timestamp
        goal.setManagerCompletionComments(req.getMgrComments());  // Manager's remarks
        goal.setEvidenceLinkVerificationStatus(EvidenceVerificationStatus.VERIFIED);  // Evidence checked
        goal.setEvidenceLinkVerifiedBy(mgr);               // Who verified evidence
        goal.setEvidenceLinkVerifiedDate(LocalDateTime.now());  // When verified
        Goal updated = goalRepo.save(goal);
        
        // CREATE FORMAL APPROVAL RECORD
        // This is stored in separate table for historical tracking
        // Used for performance reviews, analytics, compliance
        GoalCompletionApproval approval = new GoalCompletionApproval();
        approval.setGoal(goal);
        approval.setApprovalDecision("APPROVED");
        approval.setApprovedBy(mgr);
        approval.setApprovalDate(LocalDateTime.now());
        approval.setManagerComments(req.getMgrComments());
        approval.setEvidenceLinkVerified(true);
        approval.setDecisionRationale("Evidence verified and goal completion approved");
        approvalRepo.save(approval);
        
        // HIGH-PRIORITY SUCCESS NOTIFICATION
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedToUser());
        notif.setType(NotificationType.GOAL_COMPLETION_APPROVED);
        notif.setMessage("Your goal '" + goal.getTitle() + "' completion has been approved!");
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setPriority("HIGH");                         // Good news!
        notifRepo.save(notif);
        
        // AUDIT TRAIL
        AuditLog log = new AuditLog();
        log.setUser(mgr);
        log.setAction("GOAL_COMPLETION_APPROVED");
        log.setDetails("Approved completion for goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REQUEST ADDITIONAL EVIDENCE - Manager Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * REQUEST ADDITIONAL EVIDENCE (Manager needs better/more proof)
     * 
     * BUSINESS FLOW:
     * 1. Manager reviews submitted completion evidence
     * 2. Evidence link is broken, insufficient, or unclear
     * 3. Manager believes goal WAS completed, just needs better documentation
     * 4. Requests additional/better evidence instead of rejecting
     * 5. Employee provides better evidence
     * 6. Stays in completion approval workflow
     * 
     * DIFFERENCE FROM REJECTION:
     * - Rejection: Manager doesn't believe goal was completed → back to IN_PROGRESS
     * - Additional Evidence: Manager believes goal done, just needs better proof
     * 
     * COMMON SCENARIOS:
     * - "Evidence link returns 403 Forbidden - please fix permissions"
     * - "Screenshots are too small to read - upload higher resolution"
     * - "Document shows partial completion - please include final version"
     * - "Need to see actual certificate, not just course completion"
     * 
     * THIS IS SOFTER THAN REJECTION:
     * - Keeps employee morale up
     * - Goal work is done, just documentation issue
     * - Quick fix rather than redoing entire goal
     * 
     * @param goalId Goal needing better evidence
     * @param mgrId Manager requesting additional evidence
     * @param reason Specific explanation of what's needed
     * @return Updated goal with ADDITIONAL_EVIDENCE_REQUIRED status
     * @throws UnauthorizedException if wrong manager
     */
    public Goal requestAdditionalEvidence(Integer goalId, Integer mgrId, String reason) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY CHECK
        if (!goal.getAssignedManager().getUserId().equals(mgrId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // UPDATE APPROVAL STATUS TO SHOW ADDITIONAL EVIDENCE NEEDED
        goal.setCompletionApprovalStatus(CompletionApprovalStatus.ADDITIONAL_EVIDENCE_REQUIRED);
        goal.setEvidenceLinkVerificationStatus(EvidenceVerificationStatus.NEEDS_ADDITIONAL_LINK);
        User mgr = userRepo.findById(mgrId).orElse(null);
        goal.setEvidenceLinkVerificationNotes(reason);     // Explain what's needed
        Goal updated = goalRepo.save(goal);
        
        // NOTIFY EMPLOYEE WITH ACTION REQUIRED
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedToUser());
        notif.setType(NotificationType.ADDITIONAL_EVIDENCE_REQUIRED);
        notif.setMessage("Additional evidence needed for goal: " + goal.getTitle());
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setActionRequired(true);                     // Employee must respond
        notifRepo.save(notif);
        
        // AUDIT TRAIL
        AuditLog log = new AuditLog();
        log.setUser(mgr);
        log.setAction("ADDITIONAL_EVIDENCE_REQUESTED");
        log.setDetails("Requested additional evidence for goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE GOAL - Employee Action (Only When Changes Requested)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * UPDATE GOAL (Employee revises goal based on manager feedback)
     * 
     * BUSINESS FLOW:
     * 1. Manager requested changes to goal
     * 2. Employee reviews manager's feedback
     * 3. Employee makes requested revisions
     * 4. Employee resubmits updated goal
     * 5. Manager gets notified to review again
     * 
     * CRITICAL SECURITY FEATURE:
     * This method can ONLY be called when requestChanges flag is true.
     * This prevents employees from modifying approved goals without authorization.
     * 
     * WHY THIS RESTRICTION?
     * - Approved goals are commitments between employee and manager
     * - Can't change goals mid-stream without manager knowledge
     * - Prevents moving goalposts after approval
     * - Maintains integrity of performance tracking
     * 
     * TYPICAL UPDATES:
     * - Clarify vague description
     * - Adjust timeline based on feedback
     * - Change category to correct one
     * - Add measurable success criteria
     * - Update priority level
     * 
     * WORKFLOW:
     * - Can update any goal field except status
     * - Clears requestChanges flag after update
     * - Sets resubmittedDate for tracking
     * - Manager must review and approve again
     * 
     * @param goalId Goal to update
     * @param req Updated goal details (same format as create)
     * @param empId Employee making updates (from JWT)
     * @return Updated goal with changes applied
     * @throws UnauthorizedException if wrong employee tries to update
     * @throws BadRequestException if requestChanges flag is false
     */
    public Goal updateGoal(Integer goalId, CreateGoalRequest req, Integer empId) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY: Only assigned employee can update their own goal
        if (!goal.getAssignedToUser().getUserId().equals(empId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // CRITICAL BUSINESS RULE: Can ONLY update when changes were requested
        // This is the key security feature preventing unauthorized modifications
        if (!goal.getRequestChanges()) {
            throw new BadRequestException("Goal is not in change request status");
        }
        
        // UPDATE GOAL FIELDS WITH NEW VALUES
        goal.setTitle(req.getTitle());                     // Updated title
        goal.setDescription(req.getDesc());                // Updated description
        goal.setCategory(req.getCat());                    // Updated category
        goal.setPriority(req.getPri());                    // Updated priority
        goal.setStartDate(req.getStartDt());               // Updated start date
        goal.setEndDate(req.getEndDt());                   // Updated end date
        goal.setRequestChanges(false);                     // Clear the flag
        goal.setResubmittedDate(LocalDateTime.now());      // Track when resubmitted
        
        Goal updated = goalRepo.save(goal);
        
        // NOTIFY MANAGER THAT GOAL HAS BEEN REVISED
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedManager());
        notif.setType(NotificationType.GOAL_RESUBMITTED);
        notif.setMessage(goal.getAssignedToUser().getName() + " updated and resubmitted goal: " + goal.getTitle());
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notifRepo.save(notif);
        
        // AUDIT TRAIL
        User emp = userRepo.findById(empId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(emp);
        log.setAction("GOAL_UPDATED");
        log.setDetails("Updated and resubmitted goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═════════════════════════════════════════════════════════════════════
    // DELETE GOAL - Soft Delete
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * DELETE GOAL (Soft delete - marks as REJECTED)
     * 
     * AUTHORIZATION LOGIC:
     * - EMPLOYEE: Can delete their own goals only
     * - MANAGER: Can delete any goal in their team
     * - ADMIN: Can delete any goal (via role parameter)
     * 
     * SOFT DELETE APPROACH:
     * Does NOT actually remove goal from database.
     * Instead, marks status as REJECTED.
     * 
     * WHY SOFT DELETE?
     * - Preserves data for audit trail and compliance
     * - Can analyze rejected goals for patterns
     * - Can restore goal if deleted by mistake
     * - Performance reviews may need historical data
     * - Analytics can show goal rejection rates
     * 
     * USE CASES:
     * - Employee created goal by mistake → delete immediately
     * - Manager needs to remove outdated/irrelevant goals
     * - Admin cleanup of test data
     * - Goal became obsolete due to role change
     * 
     * @param goalId Goal to delete
     * @param userId User requesting deletion (from JWT)
     * @param role User's role (EMPLOYEE, MANAGER, ADMIN)
     * @throws UnauthorizedException if employee tries to delete someone else's goal
     */
    public void deleteGoal(Integer goalId, Integer userId, String role) {
        Goal goal = getGoalById(goalId);
        
        // AUTHORIZATION CHECK
        // Employees can only delete their own goals
        // Managers and admins can delete any goal
        if (role.equals("EMPLOYEE") && !goal.getAssignedToUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this goal");
        }
        
        // SOFT DELETE - Mark as rejected
        // Does NOT call goalRepo.delete() - that would hard delete
        goal.setStatus(GoalStatus.REJECTED);
        goalRepo.save(goal);
        
        // AUDIT TRAIL - Important for soft deletes
        User user = userRepo.findById(userId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction("GOAL_DELETED");
        log.setDetails("Deleted goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // VERIFY EVIDENCE - Manager Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * VERIFY EVIDENCE (Standalone evidence verification)
     * 
     * PURPOSE:
     * Allows manager to verify/reject evidence without immediately
     * approving or rejecting the entire goal completion.
     * 
     * WORKFLOW:
     * 1. Manager clicks evidence link
     * 2. Checks if link works and shows what employee claims
     * 3. Records verification status
     * 4. Can verify evidence but still request additional info
     * 5. Or verify evidence and then approve completion separately
     * 
     * EVIDENCE VERIFICATION STATUSES:
     * - NOT_VERIFIED: Haven't checked yet (initial state)
     * - VERIFIED: Link works and shows expected content
     * - NEEDS_ADDITIONAL_LINK: Need more/better evidence
     * - INVALID_LINK: Link broken or shows wrong content
     * 
     * USE CASES:
     * - Manager wants to check evidence before final decision
     * - Evidence partially valid but needs supplementation
     * - Document manager's evidence review process
     * - Track which evidence links were actually accessed
     * 
     * @param goalId Goal with evidence to verify
     * @param mgrId Manager verifying (from JWT)
     * @param status Verification status (VERIFIED, INVALID_LINK, etc.)
     * @param notes Manager's notes on the evidence
     * @return Updated goal with evidence verification status
     * @throws UnauthorizedException if wrong manager
     */
    public Goal verifyEvidence(Integer goalId, Integer mgrId, String status, String notes) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY CHECK
        if (!goal.getAssignedManager().getUserId().equals(mgrId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // UPDATE EVIDENCE VERIFICATION STATUS
        // Convert string to enum (e.g., "VERIFIED" → EvidenceVerificationStatus.VERIFIED)
        EvidenceVerificationStatus evStatus = EvidenceVerificationStatus.valueOf(status.toUpperCase());
        goal.setEvidenceLinkVerificationStatus(evStatus);
        goal.setEvidenceLinkVerificationNotes(notes);      // Manager's assessment
        User mgr = userRepo.findById(mgrId).orElse(null);
        goal.setEvidenceLinkVerifiedBy(mgr);               // Who verified
        goal.setEvidenceLinkVerifiedDate(LocalDateTime.now());  // When verified
        
        Goal updated = goalRepo.save(goal);
        
        // AUDIT TRAIL - Track evidence verification separately
        AuditLog log = new AuditLog();
        log.setUser(mgr);
        log.setAction("EVIDENCE_VERIFIED");
        log.setDetails("Verified evidence for goal: " + goal.getTitle() + " - Status: " + status);
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REJECT COMPLETION - Manager Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * REJECT GOAL COMPLETION (Manager determines goal not actually completed)
     * 
     * BUSINESS FLOW:
     * 1. Manager reviews completion submission
     * 2. Evidence shows goal NOT completed or quality insufficient
     * 3. Manager rejects completion with specific reason
     * 4. Status changes: PENDING_COMPLETION_APPROVAL → IN_PROGRESS
     * 5. Employee must continue working on goal
     * 6. Creates formal rejection record
     * 
     * DIFFERENCE FROM ADDITIONAL EVIDENCE:
     * - Additional Evidence: "Goal done, need better proof"
     * - Rejection: "Goal NOT done, keep working"
     * 
     * REASONS FOR REJECTION:
     * - Evidence shows partial completion only
     * - Quality below expected standards
     * - Wrong deliverables provided
     * - Evidence fabricated or misleading
     * - Success criteria not met
     * 
     * IMPACT ON EMPLOYEE:
     * - Negative feedback, but constructive
     * - Clear direction on what's missing
     * - Opportunity to actually complete goal
     * - May need timeline extension
     * 
     * WORKFLOW:
     * - Goal goes back to IN_PROGRESS status
     * - Employee can continue working
     * - Can submit completion again when actually done
     * - Rejection is tracked in completion approval table
     * 
     * @param goalId Goal completion being rejected
     * @param mgrId Manager rejecting (from JWT)
     * @param reason Specific explanation of why rejected
     * @return Goal back in IN_PROGRESS status
     * @throws UnauthorizedException if wrong manager
     */
    public Goal rejectCompletion(Integer goalId, Integer mgrId, String reason) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY CHECK
        if (!goal.getAssignedManager().getUserId().equals(mgrId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // UPDATE GOAL STATUS - Back to in progress
        goal.setStatus(GoalStatus.IN_PROGRESS);            // Employee continues working
        goal.setCompletionApprovalStatus(CompletionApprovalStatus.REJECTED);
        goal.setManagerCompletionComments(reason);         // Why rejected
        
        Goal updated = goalRepo.save(goal);
        
        // CREATE FORMAL REJECTION RECORD
        // Stored in completion approval table for tracking
        GoalCompletionApproval approval = new GoalCompletionApproval();
        approval.setGoal(goal);
        approval.setApprovalDecision("REJECTED");
        User mgr = userRepo.findById(mgrId).orElse(null);
        approval.setApprovedBy(mgr);
        approval.setApprovalDate(LocalDateTime.now());
        approval.setManagerComments(reason);
        approval.setEvidenceLinkVerified(false);
        approval.setDecisionRationale("Goal completion rejected");
        approvalRepo.save(approval);
        
        // NOTIFY EMPLOYEE - Needs to address feedback
        Notification notif = new Notification();
        notif.setUser(goal.getAssignedToUser());
        notif.setType(NotificationType.GOAL_COMPLETION_APPROVED);  // Note: Type might be wrong in original code
        notif.setMessage("Your goal '" + goal.getTitle() + "' completion was rejected. Please review feedback.");
        notif.setRelatedEntityType("Goal");
        notif.setRelatedEntityId(goalId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setPriority("HIGH");                         // Important - needs action
        notifRepo.save(notif);
        
        // AUDIT TRAIL
        AuditLog log = new AuditLog();
        log.setUser(mgr);
        log.setAction("GOAL_COMPLETION_REJECTED");
        log.setDetails("Rejected completion for goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
        
        return updated;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // PROGRESS TRACKING - Employee Action
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * ADD PROGRESS UPDATE (Employee logs progress on goal)
     * 
     * PURPOSE:
     * Allows employee to document progress as they work on goal.
     * Creates running log of updates over time.
     * 
     * USE CASES:
     * - Weekly progress updates
     * - Milestone achievements
     * - Blockers encountered
     * - Interim results
     * 
     * IMPLEMENTATION:
     * - Appends new note to existing progress notes
     * - Each note timestamped automatically
     * - Stored as multi-line string (could be refactored to separate table)
     * - Visible to both employee and manager
     * 
     * BENEFITS:
     * - Keeps manager informed of progress
     * - Documents effort over time
     * - Useful for performance reviews
     * - Shows persistence through challenges
     * 
     * @param goalId Goal being updated
     * @param empId Employee adding update (from JWT)
     * @param note Progress note text
     * @throws UnauthorizedException if wrong employee
     */
    public void addProgressUpdate(Integer goalId, Integer empId, String note) {
        Goal goal = getGoalById(goalId);
        
        // SECURITY: Only assigned employee can add progress
        if (!goal.getAssignedToUser().getUserId().equals(empId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // BUILD TIMESTAMPED NOTE
        String timestamp = LocalDateTime.now().toString();
        String newNote = timestamp + ": " + note;
        
        // APPEND TO EXISTING NOTES
        String existingNotes = goal.getProgressNotes();
        if (existingNotes == null || existingNotes.isEmpty()) {
            goal.setProgressNotes(newNote);                // First note
        } else {
            goal.setProgressNotes(existingNotes + "\n" + newNote);  // Append with newline
        }
        
        goalRepo.save(goal);
        
        // AUDIT TRAIL
        User emp = userRepo.findById(empId).orElse(null);
        AuditLog log = new AuditLog();
        log.setUser(emp);
        log.setAction("PROGRESS_ADDED");
        log.setDetails("Added progress update for goal: " + goal.getTitle());
        log.setRelatedEntityType("Goal");
        log.setRelatedEntityId(goalId);
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
    }
    
    /**
     * GET PROGRESS UPDATES (View progress history)
     * 
     * Returns all progress notes for a goal.
     * Shows chronological history of employee's updates.
     * 
     * @param goalId Goal to get progress for
     * @return Multi-line string of all progress notes, or default message
     */
    public String getProgressUpdates(Integer goalId) {
        Goal goal = getGoalById(goalId);
        return goal.getProgressNotes() != null ? goal.getProgressNotes() : "No progress updates yet";
    }
}
```

## GoalController.java - Complete Annotated Version

```java
package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.dto.ApproveCompletionRequest;
import com.project.performanceTrack.dto.CreateGoalRequest;
import com.project.performanceTrack.dto.SubmitCompletionRequest;
import com.project.performanceTrack.entity.Goal;
import com.project.performanceTrack.service.GoalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GOAL CONTROLLER - REST API Endpoints for Goal Management
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PURPOSE:
 * Exposes HTTP endpoints for all goal-related operations.
 * Handles HTTP requests, extracts data, calls service layer, returns responses.
 * 
 * BASE URL: /api/v1/goals
 * 
 * SECURITY:
 * - All endpoints protected by JWT authentication (via JwtAuthFilter)
 * - Role-based authorization using @PreAuthorize annotations
 * - UserId and userRole extracted from JWT token and added to request attributes
 * 
 * ARCHITECTURE PATTERN:
 * Controller → Service → Repository (clean separation of concerns)
 * - Controller: HTTP handling, validation, authorization
 * - Service: Business logic (in GoalService)
 * - Repository: Database operations
 * 
 * RESPONSE FORMAT:
 * All endpoints return ApiResponse<T> wrapper:
 * {
 *   "success": true/false,
 *   "message": "Human-readable message",
 *   "data": { ... actual data ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * 
 * HTTP STATUS CODES:
 * - 200 OK: Successful GET/PUT/DELETE
 * - 201 Created: Successful POST
 * - 400 Bad Request: Validation error, business rule violation
 * - 401 Unauthorized: Not authenticated (no JWT) or wrong user
 * - 403 Forbidden: Authenticated but insufficient permissions
 * - 404 Not Found: Resource doesn't exist
 * - 500 Internal Server Error: Unexpected server error
 */
@RestController
@RequestMapping("/api/v1/goals")  // All endpoints start with /api/v1/goals
public class GoalController {
    
    @Autowired
    private GoalService goalSvc;  // Business logic layer
    
    // ═══════════════════════════════════════════════════════════════════════
    // CREATE GOAL - Employee creates new goal
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * CREATE NEW GOAL
     * 
     * HTTP: POST /api/v1/goals
     * Auth: Required (JWT)
     * Role: EMPLOYEE only
     * 
     * REQUEST BODY:
     * {
     *   "title": "Complete Spring Boot certification",
     *   "desc": "Pass the Spring Professional certification exam",
     *   "cat": "SKILL_DEVELOPMENT",
     *   "pri": "HIGH",
     *   "mgrId": 5,
     *   "startDt": "2024-01-01",
     *   "endDt": "2024-03-31"
     * }
     * 
     * RESPONSE: (201 Created)
     * {
     *   "success": true,
     *   "message": "Goal created",
     *   "data": { ... Goal entity with generated ID ... }
     * }
     * 
     * VALIDATION:
     * - @Valid triggers validation annotations on CreateGoalRequest
     * - Ensures required fields present, dates valid, etc.
     * 
     * SECURITY:
     * - @PreAuthorize ensures only EMPLOYEE role can access
     * - empId extracted from JWT token (cannot be faked)
     * 
     * @param req Goal details from request body
     * @param httpReq Servlet request containing JWT-extracted userId
     * @return ApiResponse containing created goal
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")  // Only employees can create goals
    public ApiResponse<Goal> createGoal(@Valid @RequestBody CreateGoalRequest req,
                                        HttpServletRequest httpReq) {
        // Extract employee ID from JWT token (set by JwtAuthFilter)
        Integer empId = (Integer) httpReq.getAttribute("userId");
        
        // Delegate to service layer for business logic
        Goal goal = goalSvc.createGoal(req, empId);
        
        // Wrap in standard API response format
        return ApiResponse.success("Goal created", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // GET GOALS - Retrieve goals (role-based logic)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * GET GOALS (Role-based retrieval)
     * 
     * HTTP: GET /api/v1/goals
     * Auth: Required (JWT)
     * Role: Any authenticated user
     * 
     * BEHAVIOR BY ROLE:
     * 
     * EMPLOYEE:
     *   - Always returns their own goals
     *   - Cannot view other employees' goals
     *   - Query params ignored for security
     * 
     * MANAGER:
     *   - No params: Returns all goals they manage
     *   - ?userId=X: Returns goals for specific employee they manage
     *   - Can view team members' goals
     * 
     * ADMIN:
     *   - No params: Returns their own goals
     *   - ?userId=X: Returns goals for user X
     *   - ?mgrId=Y: Returns goals managed by manager Y
     *   - Full visibility across organization
     * 
     * EXAMPLES:
     * GET /api/v1/goals
     *   → Employee: Their goals
     *   → Manager: Goals they manage
     * 
     * GET /api/v1/goals?userId=10
     *   → Manager: Goals for employee #10
     *   → Admin: Goals for employee #10
     * 
     * @param httpReq Contains userId and userRole from JWT
     * @param userId Optional: Filter by employee ID
     * @param mgrId Optional: Filter by manager ID (admin only)
     * @return List of goals based on role and filters
     */
    @GetMapping
    public ApiResponse<List<Goal>> getGoals(HttpServletRequest httpReq,
                                            @RequestParam(required = false) Integer userId,
                                            @RequestParam(required = false) Integer mgrId) {
        // Extract user context from JWT
        String role = (String) httpReq.getAttribute("userRole");
        Integer currentUserId = (Integer) httpReq.getAttribute("userId");
        
        List<Goal> goals;
        
        // ROLE-BASED LOGIC
        if (role.equals("EMPLOYEE")) {
            // Employees always see only their own goals (security)
            goals = goalSvc.getGoalsByUser(currentUserId);
            
        } else if (role.equals("MANAGER")) {
            // Managers see goals they manage or specific employee's goals
            if (userId != null) {
                goals = goalSvc.getGoalsByUser(userId);  // Specific employee
            } else {
                goals = goalSvc.getGoalsByManager(currentUserId);  // All managed goals
            }
            
        } else {
            // ADMIN or other roles - flexible filtering
            goals = userId != null ? goalSvc.getGoalsByUser(userId) :      // By employee
                    mgrId != null ? goalSvc.getGoalsByManager(mgrId) :     // By manager
                    goalSvc.getGoalsByUser(currentUserId);                 // Default: own goals
        }
        
        return ApiResponse.success("Goals retrieved", goals);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // GET SINGLE GOAL - Goal details page
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * GET GOAL BY ID
     * 
     * HTTP: GET /api/v1/goals/{goalId}
     * Auth: Required (JWT)
     * Role: Any authenticated user
     * 
     * EXAMPLE:
     * GET /api/v1/goals/123
     * 
     * Returns complete goal details including:
     * - Title, description, dates, status
     * - Evidence links and completion notes
     * - Progress updates history
     * - Approval/rejection history
     * 
     * NOTE: No authorization check here!
     * In production, should verify user has permission to view this goal.
     * Currently any authenticated user can view any goal by ID.
     * 
     * @param goalId Goal ID from URL path
     * @return Goal entity with all details
     */
    @GetMapping("/{goalId}")
    public ApiResponse<Goal> getGoalById(@PathVariable Integer goalId) {
        Goal goal = goalSvc.getGoalById(goalId);
        return ApiResponse.success("Goal retrieved", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // APPROVE GOAL - Manager approves pending goal
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * APPROVE GOAL
     * 
     * HTTP: PUT /api/v1/goals/{goalId}/approve
     * Auth: Required (JWT)
     * Role: MANAGER only
     * 
     * EXAMPLE:
     * PUT /api/v1/goals/123/approve
     * (No request body needed)
     * 
     * EFFECT:
     * - Status changes: PENDING → IN_PROGRESS
     * - Employee can start working on goal
     * - Employee receives approval notification
     * 
     * SECURITY:
     * - Manager ID from JWT
     * - Service layer verifies manager is assigned manager for this goal
     * - Cannot approve goals assigned to other managers
     * 
     * @param goalId Goal ID from URL
     * @param httpReq Contains manager ID from JWT
     * @return Updated goal in IN_PROGRESS status
     */
    @PutMapping("/{goalId}/approve")
    @PreAuthorize("hasRole('MANAGER')")  // Managers only
    public ApiResponse<Goal> approveGoal(@PathVariable Integer goalId,
                                         HttpServletRequest httpReq) {
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        Goal goal = goalSvc.approveGoal(goalId, mgrId);
        return ApiResponse.success("Goal approved", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REQUEST CHANGES - Manager asks for goal revision
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * REQUEST CHANGES TO GOAL
     * 
     * HTTP: PUT /api/v1/goals/{goalId}/request-changes
     * Auth: Required (JWT)
     * Role: MANAGER only
     * 
     * REQUEST BODY:
     * {
     *   "comments": "Please add measurable success criteria and extend deadline by 2 weeks"
     * }
     * 
     * EXAMPLE:
     * PUT /api/v1/goals/123/request-changes
     * 
     * EFFECT:
     * - Sets requestChanges flag to true
     * - Employee receives notification
     * - Employee can now update goal via PUT /goals/{id}
     * 
     * @param goalId Goal ID from URL
     * @param body Map containing "comments" key
     * @param httpReq Contains manager ID from JWT
     * @return Updated goal with requestChanges=true
     */
    @PutMapping("/{goalId}/request-changes")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Goal> requestChanges(@PathVariable Integer goalId,
                                            @RequestBody Map<String, String> body,
                                            HttpServletRequest httpReq) {
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        String comments = body.get("comments");  // Extract comments from JSON
        Goal goal = goalSvc.requestChanges(goalId, mgrId, comments);
        return ApiResponse.success("Change request sent", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // SUBMIT COMPLETION - Employee submits completed goal with evidence
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * SUBMIT GOAL COMPLETION
     * 
     * HTTP: POST /api/v1/goals/{goalId}/submit-completion
     * Auth: Required (JWT)
     * Role: EMPLOYEE only
     * 
     * REQUEST BODY:
     * {
     *   "evLink": "https://drive.google.com/file/d/abc123",
     *   "linkDesc": "Certificate of completion and project deliverables",
     *   "accessInstr": "File is publicly accessible, no login required",
     *   "compNotes": "Completed all course modules, scored 95% on final exam, deployed sample project"
     * }
     * 
     * EXAMPLE:
     * POST /api/v1/goals/123/submit-completion
     * 
     * EFFECT:
     * - Status changes: IN_PROGRESS → PENDING_COMPLETION_APPROVAL
     * - Manager receives high-priority notification
     * - Evidence stored for manager review
     * 
     * @param goalId Goal ID from URL
     * @param req Completion details including evidence
     * @param httpReq Contains employee ID from JWT
     * @return Updated goal awaiting completion approval
     */
    @PostMapping("/{goalId}/submit-completion")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ApiResponse<Goal> submitCompletion(@PathVariable Integer goalId,
                                              @Valid @RequestBody SubmitCompletionRequest req,
                                              HttpServletRequest httpReq) {
        Integer empId = (Integer) httpReq.getAttribute("userId");
        Goal goal = goalSvc.submitCompletion(goalId, req, empId);
        return ApiResponse.success("Completion submitted", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // APPROVE COMPLETION - Manager officially completes goal
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * APPROVE GOAL COMPLETION
     * 
     * HTTP: POST /api/v1/goals/{goalId}/approve-completion
     * Auth: Required (JWT)
     * Role: MANAGER only
     * 
     * REQUEST BODY:
     * {
     *   "mgrComments": "Excellent work! Certificate verified and project meets all requirements."
     * }
     * 
     * EXAMPLE:
     * POST /api/v1/goals/123/approve-completion
     * 
     * EFFECT:
     * - Status changes: PENDING_COMPLETION_APPROVAL → COMPLETED (FINAL)
     * - Employee receives congratulations notification
     * - Goal completion record created
     * - Goal counts toward performance metrics
     * 
     * @param goalId Goal ID from URL
     * @param req Manager's comments on completion
     * @param httpReq Contains manager ID from JWT
     * @return Goal in COMPLETED status (terminal state)
     */
    @PostMapping("/{goalId}/approve-completion")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Goal> approveCompletion(@PathVariable Integer goalId,
                                               @RequestBody ApproveCompletionRequest req,
                                               HttpServletRequest httpReq) {
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        Goal goal = goalSvc.approveCompletion(goalId, req, mgrId);
        return ApiResponse.success("Completion approved", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REQUEST ADDITIONAL EVIDENCE - Manager needs better proof
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * REQUEST ADDITIONAL EVIDENCE
     * 
     * HTTP: POST /api/v1/goals/{goalId}/request-additional-evidence
     * Auth: Required (JWT)
     * Role: MANAGER only
     * 
     * REQUEST BODY:
     * {
     *   "reason": "Evidence link returns 403 error. Please update sharing permissions."
     * }
     * 
     * EFFECT:
     * - Status stays in completion approval workflow
     * - Employee receives notification with specific request
     * - Employee can resubmit with better evidence
     * 
     * @param goalId Goal ID from URL
     * @param body Map containing "reason" key
     * @param httpReq Contains manager ID from JWT
     * @return Updated goal with ADDITIONAL_EVIDENCE_REQUIRED status
     */
    @PostMapping("/{goalId}/request-additional-evidence")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Goal> requestEvidence(@PathVariable Integer goalId,
                                             @RequestBody Map<String, String> body,
                                             HttpServletRequest httpReq) {
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        String reason = body.get("reason");
        Goal goal = goalSvc.requestAdditionalEvidence(goalId, mgrId, reason);
        return ApiResponse.success("Additional evidence requested", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE GOAL - Employee revises goal (only when changes requested)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * UPDATE GOAL
     * 
     * HTTP: PUT /api/v1/goals/{goalId}
     * Auth: Required (JWT)
     * Role: EMPLOYEE only
     * 
     * REQUEST BODY: (Same format as create goal)
     * {
     *   "title": "Updated title",
     *   "desc": "Updated description",
     *   ... etc ...
     * }
     * 
     * SECURITY RESTRICTION:
     * - Can ONLY update when requestChanges flag is true
     * - Service layer enforces this rule
     * - Prevents unauthorized modifications to approved goals
     * 
     * @param goalId Goal ID from URL
     * @param req Updated goal details
     * @param httpReq Contains employee ID from JWT
     * @return Updated goal
     */
    @PutMapping("/{goalId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ApiResponse<Goal> updateGoal(@PathVariable Integer goalId,
                                        @Valid @RequestBody CreateGoalRequest req,
                                        HttpServletRequest httpReq) {
        Integer empId = (Integer) httpReq.getAttribute("userId");
        Goal goal = goalSvc.updateGoal(goalId, req, empId);
        return ApiResponse.success("Goal updated", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // DELETE GOAL - Soft delete (marks as rejected)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * DELETE GOAL
     * 
     * HTTP: DELETE /api/v1/goals/{goalId}
     * Auth: Required (JWT)
     * Role: Any authenticated user (authorization in service layer)
     * 
     * EXAMPLE:
     * DELETE /api/v1/goals/123
     * 
     * AUTHORIZATION:
     * - Employee: Can delete own goals only
     * - Manager/Admin: Can delete any goal
     * 
     * IMPLEMENTATION:
     * - Soft delete (marks as REJECTED)
     * - Does not actually remove from database
     * - Preserves audit trail
     * 
     * @param goalId Goal ID from URL
     * @param httpReq Contains userId and userRole from JWT
     * @return Success message (no data)
     */
    @DeleteMapping("/{goalId}")
    public ApiResponse<Void> deleteGoal(@PathVariable Integer goalId,
                                        HttpServletRequest httpReq) {
        Integer userId = (Integer) httpReq.getAttribute("userId");
        String role = (String) httpReq.getAttribute("userRole");
        goalSvc.deleteGoal(goalId, userId, role);
        return ApiResponse.success("Goal deleted");
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // VERIFY EVIDENCE - Manager verifies evidence quality
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * VERIFY EVIDENCE
     * 
     * HTTP: PUT /api/v1/goals/{goalId}/evidence/verify
     * Auth: Required (JWT)
     * Role: MANAGER only
     * 
     * REQUEST BODY:
     * {
     *   "status": "VERIFIED",
     *   "notes": "Certificate authentic, project repository reviewed and meets standards"
     * }
     * 
     * POSSIBLE STATUSES:
     * - VERIFIED
     * - INVALID_LINK
     * - NEEDS_ADDITIONAL_LINK
     * 
     * @param goalId Goal ID from URL
     * @param body Map with "status" and "notes"
     * @param httpReq Contains manager ID from JWT
     * @return Updated goal with evidence verification status
     */
    @PutMapping("/{goalId}/evidence/verify")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Goal> verifyEvidence(@PathVariable Integer goalId,
                                            @RequestBody Map<String, String> body,
                                            HttpServletRequest httpReq) {
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        String status = body.get("status");
        String notes = body.get("notes");
        Goal goal = goalSvc.verifyEvidence(goalId, mgrId, status, notes);
        return ApiResponse.success("Evidence verified", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REJECT COMPLETION - Manager rejects completion claim
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * REJECT GOAL COMPLETION
     * 
     * HTTP: POST /api/v1/goals/{goalId}/reject-completion
     * Auth: Required (JWT)
     * Role: MANAGER only
     * 
     * REQUEST BODY:
     * {
     *   "reason": "Evidence shows partial completion only. Modules 4-6 not completed."
     * }
     * 
     * EFFECT:
     * - Status changes: PENDING_COMPLETION_APPROVAL → IN_PROGRESS
     * - Employee must continue working
     * - Rejection record created
     * 
     * @param goalId Goal ID from URL
     * @param body Map containing "reason" key
     * @param httpReq Contains manager ID from JWT
     * @return Goal back in IN_PROGRESS status
     */
    @PostMapping("/{goalId}/reject-completion")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<Goal> rejectCompletion(@PathVariable Integer goalId,
                                              @RequestBody Map<String, String> body,
                                              HttpServletRequest httpReq) {
        Integer mgrId = (Integer) httpReq.getAttribute("userId");
        String reason = body.get("reason");
        Goal goal = goalSvc.rejectCompletion(goalId, mgrId, reason);
        return ApiResponse.success("Goal completion rejected", goal);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // PROGRESS TRACKING - Add and view progress updates
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * ADD PROGRESS UPDATE
     * 
     * HTTP: POST /api/v1/goals/{goalId}/progress
     * Auth: Required (JWT)
     * Role: EMPLOYEE only
     * 
     * REQUEST BODY:
     * {
     *   "note": "Completed modules 1-3, started module 4"
     * }
     * 
     * EFFECT:
     * - Appends timestamped note to progress history
     * - Visible to employee and manager
     * 
     * @param goalId Goal ID from URL
     * @param body Map containing "note" key
     * @param httpReq Contains employee ID from JWT
     * @return Success message
     */
    @PostMapping("/{goalId}/progress")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ApiResponse<Void> addProgress(@PathVariable Integer goalId,
                                         @RequestBody Map<String, String> body,
                                         HttpServletRequest httpReq) {
        Integer empId = (Integer) httpReq.getAttribute("userId");
        String progressNote = body.get("note");
        goalSvc.addProgressUpdate(goalId, empId, progressNote);
        return ApiResponse.success("Progress added");
    }
    
    /**
     * GET PROGRESS UPDATES
     * 
     * HTTP: GET /api/v1/goals/{goalId}/progress
     * Auth: Required (JWT)
     * Role: Any authenticated user
     * 
     * EXAMPLE:
     * GET /api/v1/goals/123/progress
     * 
     * RETURNS: Multi-line string with all progress notes
     * 
     * @param goalId Goal ID from URL
     * @return String containing all progress notes
     */
    @GetMapping("/{goalId}/progress")
    public ApiResponse<String> getProgress(@PathVariable Integer goalId) {
        String progress = goalSvc.getProgressUpdates(goalId);
        return ApiResponse.success("Progress retrieved", progress);
    }
}
```

## GoalRepository.java - Annotated Version

```java
package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.Goal;
import com.project.performanceTrack.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GOAL REPOSITORY - Data Access Layer for Goals
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PURPOSE:
 * Provides database operations for Goal entity using Spring Data JPA.
 * No SQL queries needed - Spring generates queries from method names.
 * 
 * SPRING DATA JPA MAGIC:
 * - Extends JpaRepository<Goal, Integer>
 * - Automatically provides: save, findById, findAll, delete, count, etc.
 * - Custom query methods generated from method name patterns
 * 
 * METHOD NAMING CONVENTION:
 * - findBy: Start of query methods
 * - Property names: Follow entity field names (camelCase)
 * - Nested properties: Use underscore (assignedToUser_UserId)
 * - And/Or: Combine multiple conditions
 * 
 * EXAMPLES:
 * findByAssignedToUser_UserId → SELECT * FROM goals WHERE assigned_to_user_id = ?
 * findByStatus → SELECT * FROM goals WHERE status = ?
 * 
 * NO @QUERY ANNOTATIONS NEEDED!
 * Spring generates SQL automatically from method signatures.
 */
@Repository  // Marks as data access component
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    // JpaRepository<Goal, Integer> means:
    // - Entity type: Goal
    // - Primary key type: Integer (goal_id)
    
    /**
     * FIND GOALS BY ASSIGNED USER
     * 
     * USE CASE: Employee dashboard - show all my goals
     * 
     * METHOD NAME BREAKDOWN:
     * - findBy: Query start keyword
     * - AssignedToUser: Navigate to User object (assignedToUser field)
     * - _UserId: Navigate to userId field within User
     * 
     * GENERATED SQL:
     * SELECT g.* FROM goals g
     * JOIN users u ON g.assigned_to_user_id = u.user_id
     * WHERE u.user_id = ?
     * 
     * RETURNS: All goals where goal.assignedToUser.userId equals parameter
     * 
     * @param userId Employee's user ID
     * @return List of goals assigned to this employee (can be empty)
     */
    List<Goal> findByAssignedToUser_UserId(Integer userId);
    
    /**
     * FIND GOALS BY ASSIGNED MANAGER
     * 
     * USE CASE: Manager dashboard - show all goals I'm managing
     * 
     * GENERATED SQL:
     * SELECT g.* FROM goals g
     * JOIN users m ON g.assigned_manager_id = m.user_id
     * WHERE m.user_id = ?
     * 
     * @param managerId Manager's user ID
     * @return List of goals assigned to this manager
     */
    List<Goal> findByAssignedManager_UserId(Integer managerId);
    
    /**
     * FIND GOALS BY STATUS
     * 
     * USE CASES:
     * - Admin: Show all pending goals across organization
     * - Reports: Count completed goals
     * - Dashboard widgets: "3 goals need approval"
     * 
     * GENERATED SQL:
     * SELECT * FROM goals WHERE status = ?
     * 
     * @param status Goal status enum (PENDING, IN_PROGRESS, COMPLETED, etc.)
     * @return List of goals with this status
     */
    List<Goal> findByStatus(GoalStatus status);
    
    /**
     * FIND GOALS BY USER AND STATUS
     * 
     * USE CASES:
     * - Employee: "Show me my IN_PROGRESS goals"
     * - Dashboard: "5 of your goals are completed"
     * - Filters: Combine user + status filters
     * 
     * METHOD NAME: Two conditions joined with "And"
     * 
     * GENERATED SQL:
     * SELECT g.* FROM goals g
     * JOIN users u ON g.assigned_to_user_id = u.user_id
     * WHERE u.user_id = ? AND g.status = ?
     * 
     * @param userId Employee ID
     * @param status Goal status
     * @return Goals matching both user AND status
     */
    List<Goal> findByAssignedToUser_UserIdAndStatus(Integer userId, GoalStatus status);
    
    /**
     * FIND GOALS BY MANAGER AND STATUS
     * 
     * USE CASES:
     * - Manager: "Show me pending goals needing my approval"
     * - Dashboard: "2 goals need completion approval"
     * - Filters: Manager-specific status filtering
     * 
     * GENERATED SQL:
     * SELECT g.* FROM goals g
     * JOIN users m ON g.assigned_manager_id = m.user_id
     * WHERE m.user_id = ? AND g.status = ?
     * 
     * @param managerId Manager ID
     * @param status Goal status
     * @return Goals matching both manager AND status
     */
    List<Goal> findByAssignedManager_UserIdAndStatus(Integer managerId, GoalStatus status);
    
    // ═══════════════════════════════════════════════════════════════════════
    // INHERITED METHODS (from JpaRepository - no need to define)
    // ═══════════════════════════════════════════════════════════════════════
    
    // save(Goal goal) → INSERT or UPDATE goal
    // findById(Integer id) → SELECT goal by ID
    // findAll() → SELECT all goals
    // delete(Goal goal) → DELETE goal
    // count() → COUNT total goals
    // existsById(Integer id) → Check if goal exists
    // ... and many more!
}
```

## GoalCompletionApprovalRepository.java - Annotated Version

```java
package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.GoalCompletionApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GOAL COMPLETION APPROVAL REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PURPOSE:
 * Data access layer for GoalCompletionApproval entity.
 * Stores formal records of manager decisions on goal completions.
 * 
 * WHY SEPARATE TABLE?
 * Goals table tracks current state, but we need historical record of:
 * - All approval/rejection decisions
 * - Manager comments at time of decision
 * - Evidence verification status
 * - Decision rationale
 * 
 * USE CASES:
 * - Performance reviews: Show approval history for employee
 * - Manager reports: How many approvals did manager make?
 * - Analytics: Approval/rejection rates
 * - Audit trail: Who approved what and when
 * - Dispute resolution: Review manager's rationale
 * 
 * ENTITY FIELDS:
 * - approvalId: Primary key
 * - goal: Foreign key to Goal
 * - approvedBy: Manager who made decision
 * - approvalDecision: "APPROVED" or "REJECTED"
 * - approvalDate: When decision was made
 * - managerComments: Manager's explanation
 * - evidenceLinkVerified: Did manager verify the evidence?
 * - decisionRationale: Why this decision?
 */
@Repository
public interface GoalCompletionApprovalRepository extends JpaRepository<GoalCompletionApproval, Integer> {
    // JpaRepository<GoalCompletionApproval, Integer> means:
    // - Entity: GoalCompletionApproval
    // - Primary key type: Integer (approval_id)
    
    /**
     * FIND APPROVALS BY GOAL ID
     * 
     * USE CASE: Goal details page - show approval history
     * 
     * RETURNS: All approval/rejection records for a specific goal
     * A goal might have multiple records if:
     * - Rejected first time, then approved later
     * - Multiple completion attempts
     * 
     * METHOD NAME BREAKDOWN:
     * - findBy: Query start
     * - Goal: Navigate to Goal entity (goal field)
     * - _GoalId: Navigate to goalId within Goal
     * 
     * GENERATED SQL:
     * SELECT * FROM goal_completion_approvals
     * WHERE goal_id = ?
     * ORDER BY approval_date DESC
     * 
     * EXAMPLE USAGE:
     * // Show approval history for goal #123
     * List<GoalCompletionApproval> history = repo.findByGoal_GoalId(123);
     * // Might return:
     * // [
     * //   {decision: "REJECTED", date: "2024-01-15", comments: "Incomplete"},
     * //   {decision: "APPROVED", date: "2024-01-20", comments: "All good!"}
     * // ]
     * 
     * @param goalId Goal ID to find approvals for
     * @return List of all approval records for this goal (chronological order)
     */
    List<GoalCompletionApproval> findByGoal_GoalId(Integer goalId);
    
    /**
     * FIND APPROVALS BY APPROVER (Manager)
     * 
     * USE CASE: Manager performance reports
     * 
     * QUESTIONS THIS ANSWERS:
     * - How many goals has this manager approved?
     * - What's their approval/rejection ratio?
     * - How fast do they review completions?
     * 
     * METHOD NAME BREAKDOWN:
     * - findBy: Query start
     * - ApprovedBy: Navigate to User entity (approvedBy field)
     * - _UserId: Navigate to userId within User
     * 
     * GENERATED SQL:
     * SELECT * FROM goal_completion_approvals
     * WHERE approved_by_user_id = ?
     * ORDER BY approval_date DESC
     * 
     * EXAMPLE USAGE:
     * // Get all approvals made by manager #5
     * List<GoalCompletionApproval> approvals = repo.findByApprovedBy_UserId(5);
     * 
     * // Analytics:
     * int totalApprovals = approvals.size();
     * int approved = approvals.stream()
     *     .filter(a -> a.getApprovalDecision().equals("APPROVED"))
     *     .count();
     * int rejected = totalApprovals - approved;
     * double approvalRate = (double) approved / totalApprovals * 100;
     * 
     * @param userId Manager's user ID
     * @return List of all approvals this manager has made
     */
    List<GoalCompletionApproval> findByApprovedBy_UserId(Integer userId);
    
    // ═══════════════════════════════════════════════════════════════════════
    // POTENTIAL FUTURE QUERY METHODS (not implemented yet)
    // ═══════════════════════════════════════════════════════════════════════
    
    // List<GoalCompletionApproval> findByApprovalDecision(String decision);
    // → Find all APPROVED or all REJECTED records
    
    // List<GoalCompletionApproval> findByApprovalDateBetween(LocalDateTime start, LocalDateTime end);
    // → Find approvals in date range
    
    // List<GoalCompletionApproval> findByEvidenceLinkVerifiedTrue();
    // → Find only approvals where evidence was verified
    
    // List<GoalCompletionApproval> findByApprovedBy_UserIdAndApprovalDecision(Integer userId, String decision);
    // → Find specific manager's approvals or rejections
}
```

---

These comprehensive annotations explain every aspect of the Goal management system, from API endpoints to database queries, complete with business logic explanations, security considerations, workflow details, and real-world use cases!
