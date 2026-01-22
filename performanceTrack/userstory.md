# PerformanceTrack - Project Work Breakdown Structure

## Team Members & Module Assignments

### **Team Distribution:**
1. **Kashish** - Project Setup & Infrastructure (Lead)
2. **Rudra, Sharen, Ankit (3:3:1)** - Goal & Feedback Module
3. **Akashat, Ankit (3:1)** - Performance Reports & Analytics Module
4. **Pratik, Kashish, Ankit (3:3:1)** - Performance Review & Notification Module
5. **Ankit** - User Management, Authentication, Audit Logs & Integration

---

## 📋 EPIC 1: PROJECT SETUP & INFRASTRUCTURE
**Owner: Kashish (Lead)**

### User Story 1.1: Initialize Spring Boot Project
**As a** developer  
**I want** a properly configured Spring Boot project  
**So that** the team can start development with the right dependencies

**Tasks:**
- [ ] Create Spring Boot 3.2+ project with Maven
- [ ] Configure project structure (packages: controller, service, repository, entity, dto, config, security, util, exception, enums)
- [ ] Setup application.properties/yml with profiles (dev, test, prod)
- [ ] Add core dependencies to pom.xml:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - MySQL/PostgreSQL Driver
  - Lombok
  - JWT (io.jsonwebtoken:jjwt-api:0.12.3)
  - ModelMapper/MapStruct
- [ ] Create .gitignore file
- [ ] Setup README.md with project overview

**Acceptance Criteria:**
- ✅ Project builds successfully with `mvn clean install`
- ✅ All packages created with proper naming convention
- ✅ Dependencies resolved without conflicts

---

### User Story 1.2: Database Configuration & Schema Creation
**As a** developer  
**I want** database configured and schema created  
**So that** entities can persist data correctly

**Tasks:**
- [ ] Configure MySQL/PostgreSQL connection in application.properties
- [ ] Setup Hibernate properties (ddl-auto, show-sql, format-sql)
- [ ] Create database schema script (schema.sql)
- [ ] Create initial data script (data.sql) with:
  - 1 Admin user (admin@company.com / admin123)
  - 1 Manager user (priya@company.com / manager123)  
  - 1 Employee user (rahul@company.com / employee123)
  - 1 Active Review Cycle (Q1 2026)
- [ ] Test database connectivity
- [ ] Document database setup instructions

**Acceptance Criteria:**
- ✅ Application connects to database successfully
- ✅ Tables created automatically from entities
- ✅ Sample data loaded on startup
- ✅ Can perform CRUD operations

---

### User Story 1.3: Security & JWT Configuration
**As a** developer  
**I want** JWT authentication configured  
**So that** APIs are secured with role-based access

**Tasks:**
- [ ] Create SecurityConfig class with security filter chain
- [ ] Implement JwtAuthFilter extending OncePerRequestFilter
- [ ] Create JwtUtil class for token generation/validation
- [ ] Configure CORS settings
- [ ] Disable CSRF for stateless API
- [ ] Setup password encoder (BCrypt)
- [ ] Configure public endpoints (/api/v1/auth/**)
- [ ] Test JWT token generation and validation

**Acceptance Criteria:**
- ✅ Login generates valid JWT token
- ✅ Protected endpoints require Authorization header
- ✅ Token validation works correctly
- ✅ Role-based access control functioning

---

### User Story 1.4: Exception Handling & Response Structure
**As a** developer  
**I want** centralized exception handling  
**So that** all APIs return consistent error responses

**Tasks:**
- [ ] Create custom exceptions:
  - ResourceNotFoundException
  - UnauthorizedException
  - BadRequestException
- [ ] Implement GlobalExceptionHandler with @RestControllerAdvice
- [ ] Create ApiResponse<T> wrapper class
- [ ] Handle validation exceptions (MethodArgumentNotValidException)
- [ ] Configure error messages and HTTP status codes
- [ ] Test exception handling for all scenarios

**Acceptance Criteria:**
- ✅ All exceptions return consistent JSON structure
- ✅ Validation errors show field-specific messages
- ✅ Appropriate HTTP status codes returned
- ✅ No stack traces exposed in production

---

## 📋 EPIC 2: AUTHENTICATION & USER MANAGEMENT
**Owners: Ankit (Lead)**

### User Story 2.1: User Authentication APIs
**As a** user  
**I want** to login and logout  
**So that** I can access the system securely

**Tasks - Ankit:**
- [ ] Create User entity with fields (userId, name, email, passwordHash, role, department, manager, status)
- [ ] Create UserRole enum (ADMIN, MANAGER, EMPLOYEE)
- [ ] Create UserStatus enum (ACTIVE, INACTIVE)
- [ ] Implement UserRepository
- [ ] Create LoginRequest and LoginResponse DTOs
- [ ] Implement AuthService with login/logout/changePassword methods
- [ ] Create AuthController with endpoints:
  - POST /api/v1/auth/login
  - POST /api/v1/auth/logout
  - PUT /api/v1/auth/change-password
- [ ] Add audit logging for authentication events
- [ ] Write unit tests for AuthService

**Acceptance Criteria:**
- ✅ Login with email/password returns JWT token
- ✅ Invalid credentials return 401 Unauthorized
- ✅ Logout creates audit log
- ✅ Password change validates old password
- ✅ Postman tests pass

---

### User Story 2.2: User Management APIs (Admin Only)
**As an** admin  
**I want** to manage users  
**So that** I can create, update, and view employee accounts

**Tasks - Ankit:**
- [ ] Create CreateUserRequest DTO with validation annotations
- [ ] Implement UserService with CRUD methods
- [ ] Create UserController with endpoints:
  - GET /api/v1/users (Admin/Manager)
  - GET /api/v1/users/{userId}
  - POST /api/v1/users (Admin only)
  - PUT /api/v1/users/{userId} (Admin only)
  - GET /api/v1/users/{userId}/team (Manager)
- [ ] Add @PreAuthorize annotations for role-based access
- [ ] Implement manager-employee relationship
- [ ] Create notification for new user account
- [ ] Write integration tests

**Acceptance Criteria:**
- ✅ Admin can create users with any role
- ✅ Managers can view their team members
- ✅ Email uniqueness enforced
- ✅ Password encrypted with BCrypt
- ✅ Manager assignment works correctly

---

## 📋 EPIC 3: GOAL MANAGEMENT MODULE
**Owners: Rudra (Lead - 3 stories), Sharen (3 stories), Ankit (1 story)**

### User Story 3.1: Goal Creation (Employee)
**As an** employee  
**I want** to create goals  
**So that** my manager can review and approve them

**Tasks - Rudra:**
- [ ] Create Goal entity with all fields (status, priority, category, dates, evidence fields, approval fields)
- [ ] Create GoalStatus enum (PENDING, IN_PROGRESS, PENDING_COMPLETION_APPROVAL, COMPLETED, REJECTED)
- [ ] Create GoalPriority enum (HIGH, MEDIUM, LOW)
- [ ] Create GoalCategory enum (TECHNICAL, LEADERSHIP, BUSINESS, PERSONAL_DEVELOPMENT, TEAM_COLLABORATION)
- [ ] Create CompletionApprovalStatus enum
- [ ] Create EvidenceVerificationStatus enum
- [ ] Implement GoalRepository with custom queries
- [ ] Create CreateGoalRequest DTO with validation
- [ ] Implement GoalService.createGoal() method
- [ ] Create GoalController with POST /api/v1/goals endpoint
- [ ] Add notification creation for manager
- [ ] Add audit logging
- [ ] Write unit tests

**Acceptance Criteria:**
- ✅ Employee can create goal with required fields
- ✅ Goal status set to PENDING
- ✅ Manager receives notification
- ✅ Validation enforces end date > start date
- ✅ Goal linked to employee and manager

---

### User Story 3.2: Goal Approval Workflow (Manager)
**As a** manager  
**I want** to approve or request changes to goals  
**So that** employees have clear, approved objectives

**Tasks - Rudra:**
- [ ] Implement GoalService.approveGoal() method
- [ ] Implement GoalService.requestChanges() method
- [ ] Add GoalController endpoints:
  - PUT /api/v1/goals/{goalId}/approve
  - PUT /api/v1/goals/{goalId}/request-changes
- [ ] Create Feedback entity for change request comments
- [ ] Implement FeedbackRepository
- [ ] Update goal status to IN_PROGRESS on approval
- [ ] Set requestChanges flag when changes requested
- [ ] Create notifications for employee
- [ ] Add audit logging for approvals
- [ ] Write integration tests

**Acceptance Criteria:**
- ✅ Manager can approve pending goals
- ✅ Approved goal status changes to IN_PROGRESS
- ✅ Manager can request changes with comments
- ✅ Employee receives notifications
- ✅ Only assigned manager can approve

---

### User Story 3.3: Goal Update After Change Request (Employee)
**As an** employee  
**I want** to update goals when changes are requested  
**So that** I can resubmit for approval

**Tasks - Rudra:**
- [ ] Implement GoalService.updateGoal() method
- [ ] Add PUT /api/v1/goals/{goalId} endpoint
- [ ] Validate goal has requestChanges=true
- [ ] Reset requestChanges flag after update
- [ ] Set resubmittedDate timestamp
- [ ] Create notification for manager
- [ ] Add audit logging
- [ ] Write tests for update workflow

**Acceptance Criteria:**
- ✅ Employee can update only when changes requested
- ✅ Goal reverts to PENDING status
- ✅ Manager notified of resubmission
- ✅ Cannot update approved goals

---

### User Story 3.4: Goal Progress Tracking (Employee)
**As an** employee  
**I want** to add progress updates to my goals  
**So that** I can document my work

**Tasks - Sharen:**
- [ ] Implement GoalService.addProgressUpdate() method
- [ ] Implement GoalService.getProgressUpdates() method
- [ ] Add GoalController endpoints:
  - POST /api/v1/goals/{goalId}/progress
  - GET /api/v1/goals/{goalId}/progress
- [ ] Store progress notes with timestamp
- [ ] Format progress notes display
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Employee can add progress notes
- ✅ Each note has timestamp
- ✅ Progress visible to employee and manager
- ✅ Multiple updates stored correctly

---

### User Story 3.5: Goal Completion Submission (Employee)
**As an** employee  
**I want** to submit goal completion with evidence  
**So that** my manager can verify and approve

**Tasks - Sharen:**
- [ ] Create SubmitCompletionRequest DTO (evidenceLink, linkDescription, accessInstructions, completionNotes)
- [ ] Implement GoalService.submitCompletion() method
- [ ] Add POST /api/v1/goals/{goalId}/submit-completion endpoint
- [ ] Update goal status to PENDING_COMPLETION_APPROVAL
- [ ] Set completion submitted date
- [ ] Set evidenceLinkVerificationStatus to NOT_VERIFIED
- [ ] Set completionApprovalStatus to PENDING
- [ ] Create high-priority notification for manager
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Employee can submit completion with evidence link
- ✅ Only IN_PROGRESS goals can be submitted
- ✅ Manager receives notification with high priority
- ✅ Completion date recorded

---

### User Story 3.6: Evidence Verification & Completion Approval (Manager)
**As a** manager  
**I want** to verify evidence and approve goal completion  
**So that** employees get credit for completed work

**Tasks - Sharen:**
- [ ] Create ApproveCompletionRequest DTO
- [ ] Create GoalCompletionApproval entity
- [ ] Implement GoalCompletionApprovalRepository
- [ ] Implement GoalService.verifyEvidence() method
- [ ] Implement GoalService.approveCompletion() method
- [ ] Implement GoalService.requestAdditionalEvidence() method
- [ ] Implement GoalService.rejectCompletion() method
- [ ] Add GoalController endpoints:
  - PUT /api/v1/goals/{goalId}/evidence/verify
  - POST /api/v1/goals/{goalId}/approve-completion
  - POST /api/v1/goals/{goalId}/request-additional-evidence
  - POST /api/v1/goals/{goalId}/reject-completion
- [ ] Update goal status to COMPLETED on approval
- [ ] Create GoalCompletionApproval record
- [ ] Set evidenceLinkVerificationStatus appropriately
- [ ] Create notifications for employee
- [ ] Add audit logging
- [ ] Write comprehensive tests

**Acceptance Criteria:**
- ✅ Manager can verify evidence with notes
- ✅ Manager can approve completion with comments
- ✅ Manager can request additional evidence
- ✅ Manager can reject completion with reason
- ✅ Goal status updated correctly
- ✅ Completion approval record created

---

### User Story 3.7: Goal Listing & Filtering APIs
**As a** user  
**I want** to view and filter goals  
**So that** I can track my goals or my team's goals

**Tasks - Ankit:**
- [ ] Add GoalController endpoints:
  - GET /api/v1/goals (with filters)
  - GET /api/v1/goals/{goalId}
  - DELETE /api/v1/goals/{goalId}
- [ ] Implement role-based goal filtering:
  - Employee sees own goals
  - Manager sees team goals
  - Admin sees all goals
- [ ] Add query parameters (userId, managerId, status)
- [ ] Implement soft delete (set status to REJECTED)
- [ ] Add audit logging for delete
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Users see appropriate goals based on role
- ✅ Filtering by user/manager works
- ✅ Goal details retrieved correctly
- ✅ Soft delete prevents permanent deletion

---

## 📋 EPIC 4: FEEDBACK MODULE
**Owners: Rudra, Sharen, Ankit**

### User Story 4.1: Feedback Management
**As a** manager or employee  
**I want** to provide feedback on goals and reviews  
**So that** constructive comments are documented

**Tasks - Ankit:**
- [ ] Create Feedback entity (feedbackId, goal, review, givenByUser, comments, feedbackType, date)
- [ ] Implement FeedbackRepository with queries
- [ ] Create FeedbackController with endpoints:
  - GET /api/v1/feedback (with filters)
  - POST /api/v1/feedback
- [ ] Link feedback to goals or reviews
- [ ] Support feedback types (POSITIVE, CONSTRUCTIVE, CHANGE_REQUEST)
- [ ] Add timestamp for feedback
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Users can create feedback on goals
- ✅ Users can create feedback on reviews
- ✅ Feedback filterable by goal/review
- ✅ Feedback types enforced

---

## 📋 EPIC 5: PERFORMANCE REVIEW MODULE
**Owners: Pratik (Lead - 3 stories), Kashish (3 stories), Ankit (1 story)**

### User Story 5.1: Review Cycle Management (Admin)
**As an** admin  
**I want** to create and manage review cycles  
**So that** performance reviews happen in defined periods

**Tasks - Pratik:**
- [ ] Create ReviewCycle entity (cycleId, title, startDate, endDate, status, requiresCompletionApproval, evidenceRequired)
- [ ] Create ReviewCycleStatus enum (ACTIVE, CLOSED)
- [ ] Implement ReviewCycleRepository
- [ ] Create CreateReviewCycleRequest DTO
- [ ] Implement ReviewCycleService with CRUD methods
- [ ] Create ReviewCycleController with endpoints:
  - GET /api/v1/review-cycles
  - GET /api/v1/review-cycles/{cycleId}
  - GET /api/v1/review-cycles/active
  - POST /api/v1/review-cycles (Admin)
  - PUT /api/v1/review-cycles/{cycleId} (Admin)
- [ ] Add @PreAuthorize for admin-only operations
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Admin can create review cycles
- ✅ Only one active cycle at a time
- ✅ Cycle dates validated (end > start)
- ✅ Public endpoint to get active cycle

---

### User Story 5.2: Self-Assessment Submission (Employee)
**As an** employee  
**I want** to submit my self-assessment  
**So that** my manager can review my performance

**Tasks - Pratik:**
- [ ] Create PerformanceReview entity (reviewId, cycle, user, selfAssessment, employeeSelfRating, managerFeedback, managerRating, status, timestamps)
- [ ] Create PerformanceReviewStatus enum (PENDING, SELF_ASSESSMENT_COMPLETED, COMPLETED, COMPLETED_AND_ACKNOWLEDGED)
- [ ] Implement PerformanceReviewRepository with queries
- [ ] Create SelfAssessmentRequest DTO
- [ ] Implement PerformanceReviewService.submitSelfAssessment()
- [ ] Implement PerformanceReviewService.updateSelfAssessmentDraft()
- [ ] Create PerformanceReviewGoals linking entity
- [ ] Implement PerformanceReviewGoalsRepository
- [ ] Link completed goals to review
- [ ] Create PerformanceReviewController with endpoints:
  - POST /api/v1/performance-reviews
  - PUT /api/v1/performance-reviews/{reviewId}/draft
- [ ] Create notification for manager
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Employee can submit self-assessment for active cycle
- ✅ Self-assessment stored as JSON string
- ✅ Employee can update draft before manager reviews
- ✅ Completed goals linked to review
- ✅ Manager notified of submission

---

### User Story 5.3: Manager Review Submission (Manager)
**As a** manager  
**I want** to complete employee performance reviews  
**So that** feedback is formally documented

**Tasks - Pratik:**
- [ ] Create ManagerReviewRequest DTO (managerFeedback, managerRating, ratingJustification, compensationRecommendations, nextPeriodGoals)
- [ ] Implement PerformanceReviewService.submitManagerReview()
- [ ] Add PUT /api/v1/performance-reviews/{reviewId} endpoint
- [ ] Validate self-assessment completed
- [ ] Update review status to COMPLETED
- [ ] Set review completed date
- [ ] Create notification for employee
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Manager can submit review after self-assessment
- ✅ Manager feedback stored as JSON
- ✅ Compensation recommendations included
- ✅ Employee notified of completed review
- ✅ Review status updated to COMPLETED

---

### User Story 5.4: Review Acknowledgment (Employee)
**As an** employee  
**I want** to acknowledge my performance review  
**So that** I confirm I've read it

**Tasks - Kashish:**
- [ ] Implement PerformanceReviewService.acknowledgeReview()
- [ ] Add POST /api/v1/performance-reviews/{reviewId}/acknowledge endpoint
- [ ] Update review status to COMPLETED_AND_ACKNOWLEDGED
- [ ] Set acknowledged date
- [ ] Allow employee response/comments
- [ ] Create notification for manager
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Employee can acknowledge completed review
- ✅ Employee can add response comments
- ✅ Acknowledgment date recorded
- ✅ Manager notified of acknowledgment

---

### User Story 5.5: Review Listing & Retrieval APIs
**As a** user  
**I want** to view performance reviews  
**So that** I can track my reviews or my team's reviews

**Tasks - Kashish:**
- [ ] Add PerformanceReviewController endpoints:
  - GET /api/v1/performance-reviews (with filters)
  - GET /api/v1/performance-reviews/{reviewId}
- [ ] Implement role-based review filtering:
  - Employee sees own reviews
  - Manager sees team reviews
  - Admin sees all reviews
- [ ] Add query parameters (userId, cycleId, status)
- [ ] Include linked goals in response
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Users see appropriate reviews based on role
- ✅ Filtering by user/cycle works
- ✅ Review details include all sections
- ✅ Linked goals displayed

---

### User Story 5.6: Review Cycle Workflow Testing
**As a** QA tester  
**I want** complete end-to-end review cycle tested  
**So that** the workflow functions correctly

**Tasks - Kashish:**
- [ ] Create integration test for complete review cycle:
  1. Admin creates cycle
  2. Employee completes goals
  3. Employee submits self-assessment
  4. Manager submits review
  5. Employee acknowledges
- [ ] Test notification flow
- [ ] Test audit logging
- [ ] Test error scenarios
- [ ] Document test results

**Acceptance Criteria:**
- ✅ Complete workflow test passes
- ✅ All notifications generated
- ✅ All audit logs created
- ✅ Error handling works

---

### User Story 5.7: Review Analytics Integration
**As a** manager  
**I want** review data used in analytics  
**So that** I can see performance trends

**Tasks - Ankit:**
- [ ] Ensure PerformanceReview data accessible to ReportService
- [ ] Add review metrics to dashboard
- [ ] Test review data in reports
- [ ] Document review-to-analytics flow

**Acceptance Criteria:**
- ✅ Review data appears in analytics
- ✅ Ratings aggregated correctly
- ✅ Department comparisons work

---

## 📋 EPIC 6: NOTIFICATION MODULE
**Owners: Pratik (Lead - 3 stories), Kashish (3 stories), Ankit (1 story)**

### User Story 6.1: Notification System Setup
**As a** developer  
**I want** a notification system  
**So that** users are informed of important events

**Tasks - Pratik:**
- [ ] Create Notification entity (notificationId, user, type, message, relatedEntityType, relatedEntityId, status, priority, actionRequired, timestamps)
- [ ] Create NotificationType enum (ACCOUNT_CREATED, GOAL_SUBMITTED, GOAL_APPROVED, GOAL_CHANGE_REQUESTED, GOAL_RESUBMITTED, GOAL_COMPLETION_SUBMITTED, GOAL_COMPLETION_APPROVED, ADDITIONAL_EVIDENCE_REQUIRED, SELF_ASSESSMENT_SUBMITTED, PERFORMANCE_REVIEW_COMPLETED, REVIEW_ACKNOWLEDGED, REVIEW_REMINDER)
- [ ] Create NotificationStatus enum (UNREAD, READ)
- [ ] Implement NotificationRepository with queries
- [ ] Document notification creation patterns
- [ ] Write utility methods for notification creation

**Acceptance Criteria:**
- ✅ Notification entity created with all fields
- ✅ Enums defined for types and status
- ✅ Repository has query methods
- ✅ Notification creation documented

---

### User Story 6.2: Notification Creation in Workflows
**As a** developer  
**I want** notifications created automatically  
**So that** users are informed of relevant events

**Tasks - Kashish:**
- [ ] Add notification creation in GoalService for:
  - Goal submitted (to manager)
  - Goal approved (to employee)
  - Changes requested (to employee)
  - Goal resubmitted (to manager)
  - Completion submitted (to manager)
  - Completion approved (to employee)
  - Additional evidence needed (to employee)
- [ ] Add notification creation in PerformanceReviewService for:
  - Self-assessment submitted (to manager)
  - Manager review completed (to employee)
  - Review acknowledged (to manager)
- [ ] Add notification creation in UserService for:
  - Account created (to new user)
- [ ] Set appropriate priority levels
- [ ] Set actionRequired flag where needed
- [ ] Write tests for each notification type

**Acceptance Criteria:**
- ✅ All workflow events create notifications
- ✅ Notifications linked to correct users
- ✅ Priority and actionRequired set correctly
- ✅ Related entity information captured

---

### User Story 6.3: Notification Retrieval APIs
**As a** user  
**I want** to view my notifications  
**So that** I stay informed of important updates

**Tasks - Kashish:**
- [ ] Create NotificationController with endpoints:
  - GET /api/v1/notifications (with status filter)
  - PUT /api/v1/notifications/{notificationId}
  - PUT /api/v1/notifications/mark-all-read
- [ ] Implement notification filtering by status
- [ ] Order notifications by date (newest first)
- [ ] Update notification status to READ
- [ ] Set read date timestamp
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Users see their notifications only
- ✅ Filtering by UNREAD/READ works
- ✅ Mark as read updates status
- ✅ Mark all as read works correctly
- ✅ Notifications ordered by date

---

### User Story 6.4: Notification Badge Count
**As a** user  
**I want** to see unread notification count  
**So that** I know when I have new notifications

**Tasks - Pratik:**
- [ ] Add query method to count unread notifications
- [ ] Add GET /api/v1/notifications/count endpoint
- [ ] Return unread count
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Count endpoint returns accurate unread count
- ✅ Count updates when notifications marked read

---

### User Story 6.5: Notification Action Links
**As a** user  
**I want** notifications to link to related items  
**So that** I can quickly navigate to relevant content

**Tasks - Pratik:**
- [ ] Ensure relatedEntityType and relatedEntityId set
- [ ] Document entity type patterns:
  - "Goal" → goalId
  - "PerformanceReview" → reviewId
  - "User" → userId
- [ ] Add examples in API documentation
- [ ] Write tests

**Acceptance Criteria:**
- ✅ All notifications have entity links
- ✅ Entity types documented
- ✅ Frontend can construct URLs from links

---

### User Story 6.6: Notification Testing & Integration
**As a** QA tester  
**I want** comprehensive notification testing  
**So that** notifications work reliably

**Tasks - Kashish:**
- [ ] Create integration tests for all notification types
- [ ] Test notification creation in all workflows
- [ ] Test notification retrieval and filtering
- [ ] Test mark as read functionality
- [ ] Test notification count accuracy
- [ ] Document notification test results

**Acceptance Criteria:**
- ✅ All notification types tested
- ✅ Integration tests pass
- ✅ No duplicate notifications created
- ✅ Test coverage > 80%

---

### User Story 6.7: Notification Cleanup & Maintenance
**As an** admin  
**I want** old notifications cleaned up  
**So that** database doesn't grow indefinitely

**Tasks - Ankit:**
- [ ] Design notification retention policy (e.g., 90 days)
- [ ] Create scheduled task to delete old read notifications
- [ ] Add admin endpoint to manually trigger cleanup
- [ ] Add logging for cleanup operations
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Cleanup task runs on schedule
- ✅ Only old read notifications deleted
- ✅ Cleanup logged properly
- ✅ Manual trigger works

---

## 📋 EPIC 7: REPORTS & ANALYTICS MODULE
**Owners: Akashat (Lead - 3 stories), Ankit (1 story)**

### User Story 7.1: Report Entity & Infrastructure
**As a** developer  
**I want** report generation infrastructure  
**So that** analytics reports can be created

**Tasks - Akashat:**
- [ ] Create Report entity (reportId, scope, metrics, format, generatedBy, generatedDate, filePath)
- [ ] Implement ReportRepository
- [ ] Create ReportService with method stubs
- [ ] Create ReportController with endpoints:
  - GET /api/v1/reports
  - GET /api/v1/reports/{reportId}
  - POST /api/v1/reports/generate
- [ ] Add @PreAuthorize for admin/manager only
- [ ] Add audit logging
- [ ] Write basic tests

**Acceptance Criteria:**
- ✅ Report entity created
- ✅ Basic CRUD endpoints working
- ✅ Role-based access enforced

---

### User Story 7.2: Dashboard Metrics API
**As a** user  
**I want** role-specific dashboard metrics  
**So that** I see relevant performance data

**Tasks - Akashat:**
- [ ] Implement ReportService.getDashboardMetrics()
- [ ] Create role-specific metric calculation:
  - **Employee:** totalGoals, completedGoals, inProgressGoals, pendingGoals, completionRate
  - **Manager:** teamSize, totalTeamGoals, pendingApprovals, pendingCompletions
  - **Admin:** totalUsers, totalGoals, totalReviews, completedGoals
- [ ] Add GET /api/v1/reports/dashboard endpoint
- [ ] Return appropriate metrics based on user role
- [ ] Write tests for each role

**Acceptance Criteria:**
- ✅ Each role sees appropriate metrics
- ✅ Calculations accurate
- ✅ Metrics update in real-time
- ✅ Performance optimized

---

### User Story 7.3: Goal Analytics API
**As a** manager/admin  
**I want** goal analytics  
**So that** I can track goal completion trends

**Tasks - Akashat:**
- [ ] Implement ReportService.getGoalAnalytics()
- [ ] Calculate metrics:
  - Total goals
  - Goals by status (pending, in_progress, pending_completion, completed, rejected)
  - Completion rate percentage
  - Goals by priority
  - Goals by category
- [ ] Add GET /api/v1/reports/goal-analytics endpoint
- [ ] Support optional filtering by department, date range
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Goal breakdown by status accurate
- ✅ Completion rate calculated correctly
- ✅ Filtering works
- ✅ Charts can be generated from data

---

### User Story 7.4: Performance Summary API
**As a** manager/admin  
**I want** performance review summaries  
**So that** I can analyze review data

**Tasks - Akashat:**
- [ ] Implement ReportService.getPerformanceSummary()
- [ ] Calculate metrics:
  - Total reviews
  - Average self-rating
  - Average manager rating
  - Rating distribution
  - Reviews by status
- [ ] Add GET /api/v1/reports/performance-summary endpoint
- [ ] Support filtering by cycleId, department
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Summary includes all metrics
- ✅ Averages calculated correctly
- ✅ Filtering by cycle works
- ✅ Department filtering works

---

### User Story 7.5: Department Performance API
**As an** admin  
**I want** department-level performance data  
**So that** I can compare departments

**Tasks - Ankit:**
- [ ] Implement ReportService.getDepartmentPerformance()
- [ ] Calculate per-department metrics:
  - Employee count
  - Total goals
  - Completed goals
  - Completion rate
  - Average review rating
- [ ] Add GET /api/v1/reports/department-performance endpoint
- [ ] Return list of department metrics
- [ ] Write tests

**Acceptance Criteria:**
- ✅ All departments included
- ✅ Metrics accurate per department
- ✅ Departments sortable by completion rate
- ✅ Charts can be generated

---

### User Story 7.6: Report Generation API
**As a** manager/admin  
**I want** to generate downloadable reports  
**So that** I can share performance data

**Tasks - Ankit:**
- [ ] Implement ReportService.generateReport()
- [ ] Support report formats (PDF, Excel, CSV)
- [ ] Create file generation logic (placeholder)
- [ ] Store report metadata in database
- [ ] Generate file path
- [ ] Add audit logging
- [ ] Write tests

**Acceptance Criteria:**
- ✅ Report metadata stored
- ✅ File path generated
- ✅ Supported formats validated
- ✅ Audit log created

---

## 📋 EPIC 8: AUDIT LOGGING MODULE
**Owner: Ankit**

### User Story 8.1: Audit Log System
**As an** admin  
**I want** comprehensive audit logs  
**So that** all system actions are tracked

**Tasks - Ankit:**
- [ ] Create AuditLog entity (auditId, user, action, details, relatedEntityType, relatedEntityId, ipAddress, status, timestamp)
- [ ] Implement AuditLogRepository with queries
- [ ] Add audit logging to all service methods
- [ ] Create AuditLogController with endpoints:
  - GET /api/v1/audit-logs (with filters)
  - POST /api/v1/audit-logs/export
- [ ] Support filtering by userId, action, date range
- [ ] Implement export functionality (CSV)
- [ ] Add @PreAuthorize for admin only
- [ ] Write tests

**Acceptance Criteria:**
- ✅ All actions logged with timestamps
- ✅ User and action captured
- ✅ Related entities linked
- ✅ Filtering works correctly
- ✅ Export generates CSV file
- ✅ Only admins can access

---

## 📋 EPIC 9: INTEGRATION & TESTING
**Owner: All Team Members**

### User Story 9.1: Postman Collection Creation
**As a** tester  
**I want** comprehensive Postman collection  
**So that** all APIs can be tested easily

**Tasks - Ankit (Lead):**
- [ ] Create Postman collection with all 47 APIs
- [ ] Organize by modules (Auth, Users, Goals, Reviews, Notifications, Reports, Audit)
- [ ] Add environment variables (baseUrl, adminToken, managerToken, employeeToken)
- [ ] Add pre-request scripts for token management
- [ ] Add test scripts for response validation
- [ ] Document testing workflow (15 phases)
- [ ] Export collection as JSON
- [ ] Write testing guide document

**Acceptance Criteria:**
- ✅ All 47 APIs in collection
- ✅ Variables configured correctly
- ✅ Test scripts validate responses
- ✅ Documentation complete
- ✅ Collection importable by team

---

### User Story 9.2: Unit Testing
**As a** developer  
**I want** unit tests for all services  
**So that** business logic is verified

**Tasks - All Developers:**
- [ ] Write unit tests for all service methods
- [ ] Use JUnit 5 and Mockito
- [ ] Achieve >80% code coverage
- [ ] Test success and error scenarios
- [ ] Mock repository dependencies
- [ ] Run tests with `mvn test`

**Acceptance Criteria:**
- ✅ All service methods tested
- ✅ Code coverage >80%
- ✅ All tests pass
- ✅ Error scenarios covered

---

### User Story 9.3: Integration Testing
**As a** developer  
**I want** integration tests for APIs  
**So that** end-to-end workflows are verified

**Tasks - All Developers:**
- [ ] Write integration tests using @SpringBootTest
- [ ] Test complete workflows:
  - Goal creation → approval → completion → approval
  - Self-assessment → manager review → acknowledgment
- [ ] Use test database (H2 or TestContainers)
- [ ] Test role-based access control
- [ ] Test error scenarios (401, 403, 404)
- [ ] Run with `mvn verify`

**Acceptance Criteria:**
- ✅ Key workflows tested end-to-end
- ✅ Security tests pass
- ✅ All tests isolated (no data leakage)
- ✅ Tests run automatically in CI

---

### User Story 9.4: Code Review & Quality
**As a** team  
**I want** code review process  
**So that** code quality is maintained

**Tasks - All Developers:**
- [ ] Setup Git branching strategy (feature branches)
- [ ] Create pull request templates
- [ ] Conduct peer code reviews
- [ ] Run SonarQube/SonarLint analysis
- [ ] Fix code smells and vulnerabilities
- [ ] Ensure naming conventions followed
- [ ] Document complex logic

**Acceptance Criteria:**
- ✅ All code peer-reviewed
- ✅ No critical SonarQube issues
- ✅ Naming conventions consistent
- ✅ Documentation adequate

---

## 📋 EPIC 10: DOCUMENTATION & DEPLOYMENT
**Owner: Ankit (Lead), All Team Members**

### User Story 10.1: API Documentation
**As a** developer  
**I want** comprehensive API documentation  
**So that** APIs are easy to understand and use

**Tasks - Ankit:**
- [ ] Add Springdoc OpenAPI dependency
- [ ] Configure Swagger UI
- [ ] Add @Operation annotations to controllers
- [ ] Add @Schema annotations to DTOs
- [ ] Document request/response examples
- [ ] Add authentication documentation
- [ ] Test Swagger UI at /swagger-ui.html
- [ ] Export OpenAPI spec

**Acceptance Criteria:**
- ✅ Swagger UI accessible
- ✅ All endpoints documented
- ✅ Request/response examples present
- ✅ Authentication documented

---

### User Story 10.2: README & Setup Guide
**As a** new developer  
**I want** setup instructions  
**So that** I can run the project locally

**Tasks - Kashish:**
- [ ] Write comprehensive README.md with:
  - Project overview
  - Tech stack
  - Prerequisites
  - Database setup instructions
  - Application.properties configuration
  - How to run the application
  - How to run tests
  - Postman collection import instructions
  - API endpoint summary
  - Team member responsibilities
- [ ] Add architecture diagram
- [ ] Add database schema diagram
- [ ] Document environment variables

**Acceptance Criteria:**
- ✅ README covers all setup steps
- ✅ New developer can setup in <30 minutes
- ✅ Diagrams included
- ✅ Troubleshooting section present

---

### User Story 10.3: Deployment Configuration
**As a** DevOps engineer  
**I want** deployment configuration  
**So that** application can be deployed to production

**Tasks - Ankit:**
- [ ] Create application-prod.properties
- [ ] Configure production database settings
- [ ] Setup logging configuration (Logback)
- [ ] Create Docker file (optional)
- [ ] Document deployment steps
- [ ] Setup health check endpoint
- [ ] Configure CORS for production
- [ ] Document security checklist

**Acceptance Criteria:**
- ✅ Production config created
- ✅ Logging configured
- ✅ Deployment documented
- ✅ Security checklist complete

---

## 📊 PROJECT TIMELINE & MILESTONES

### Week 1: Foundation
- **Kashish:** Complete EPIC 1 (Project Setup & Infrastructure) - 4 stories
- **Ankit:** Complete EPIC 2 (Authentication & User Management) - 2 stories
- **Milestone:** Project builds, database connected, login works

### Week 2: Core Modules
- **Rudra:** Complete User Stories 3.1, 3.2, 3.3 (Goal creation & approval)
- **Sharen:** Complete User Stories 3.4, 3.5, 3.6 (Goal progress & completion)
- **Pratik:** Complete User Stories 5.1, 5.2, 5.3 (Review cycle & self-assessment)
- **Ankit:** Support all teams with integration
- **Milestone:** Goal and Review workflows functional

### Week 3: Supporting Features
- **Kashish:** Complete User Stories 5.4, 5.5, 5.6 (Review completion & testing)
- **Pratik:** Complete User Stories 6.1, 6.2, 6.4, 6.5 (Notification system)
- **Kashish:** Complete User Stories 6.3, 6.6 (Notification APIs & testing)
- **Akashat:** Complete User Stories 7.1, 7.2, 7.3, 7.4 (Analytics APIs)
- **Ankit:** Complete User Stories 3.7, 4.1, 7.5, 7.6 (Supporting features)
- **Milestone:** All features implemented

### Week 4: Quality & Deployment
- **All:** Complete EPIC 9 (Integration & Testing)
- **Ankit, Kashish:** Complete EPIC 10 (Documentation & Deployment)
- **Ankit:** Complete EPIC 8 (Audit Logging)
- **Milestone:** Production-ready application

---

## 📋 TASK DISTRIBUTION SUMMARY

### Kashish (7 stories total)
- EPIC 1: 4 stories (Project Setup)
- EPIC 5: 3 stories (Review completion & testing)
- EPIC 6: 2 stories (Notification APIs & testing)
- EPIC 10: 1 story (README)

### Ankit (10 stories total)
- EPIC 2: 2 stories (Authentication & User Management)
- EPIC 3: 1 story (Goal listing)
- EPIC 4: 1 story (Feedback)
- EPIC 5: 1 story (Review analytics integration)
- EPIC 6: 1 story (Notification cleanup)
- EPIC 7: 2 stories (Department performance & report generation)
- EPIC 8: 1 story (Audit logging)
- EPIC 9: 1 story (Postman collection)
- EPIC 10: 2 stories (API docs & deployment)

### Rudra (3 stories)
- EPIC 3: 3 stories (Goal creation, approval, update)

### Sharen (3 stories)
- EPIC 3: 3 stories (Goal progress, completion submission, evidence verification)

### Pratik (5 stories)
- EPIC 5: 3 stories (Review cycle, self-assessment, manager review)
- EPIC 6: 2 stories (Notification system setup & badge count)

### Akashat (4 stories)
- EPIC 7: 4 stories (Report infrastructure, dashboard metrics, goal analytics, performance summary)

---

## 🎯 ACCEPTANCE CRITERIA FOR PROJECT COMPLETION

### Functional Requirements ✓
- [ ] All 47 APIs implemented and working
- [ ] Complete goal workflow (creation → approval → completion → approval)
- [ ] Complete review workflow (cycle → self-assessment → manager review → acknowledgment)
- [ ] Notification system functional for all events
- [ ] Analytics dashboards return accurate data
- [ ] Audit logging captures all actions
- [ ] Role-based access control enforced

### Technical Requirements ✓
- [ ] Code coverage >80%
- [ ] All integration tests pass
- [ ] No critical SonarQube issues
- [ ] Swagger UI functional
- [ ] Postman collection complete with 47 APIs
- [ ] Database schema optimized
- [ ] Exception handling consistent

### Documentation Requirements ✓
- [ ] README complete with setup instructions
- [ ] API documentation complete
- [ ] Postman testing guide complete
- [ ] Code comments adequate
- [ ] Architecture diagrams included

### Deployment Requirements ✓
- [ ] Application runs locally without errors
- [ ] Production configuration ready
- [ ] Logging configured
- [ ] Security checklist complete

---

## 📞 TEAM COMMUNICATION

### Daily Standups (15 min)
- What did you complete yesterday?
- What will you work on today?
- Any blockers?

### Weekly Progress Review
- Demo completed features
- Review code quality metrics
- Adjust timeline if needed

### Communication Channels
- GitHub: Code reviews and pull requests
- Slack/Teams: Daily communication
- Meetings: Standup, weekly review, blockers

---

## 🚀 SUCCESS METRICS

1. **Velocity:** All stories completed within 4 weeks
2. **Quality:** Code coverage >80%, zero critical bugs
3. **Functionality:** All 47 APIs working as per Postman tests
4. **Teamwork:** All team members contributing equally
5. **Documentation:** Complete and accurate documentation

---

**PROJECT START DATE:** [To be decided]  
**PROJECT END DATE:** [4 weeks from start]  
**TOTAL USER STORIES:** 45  
**TOTAL APIS:** 47

**Good luck, team! Let's build an excellent Performance Management System! 🎉**