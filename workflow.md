PerformanceTrack System - Complete Enhanced Workflow Walkthrough
Let me walk you through the enhanced system with audit trails, manager approval for goal completion, and evidence management in extreme detail.

PHASE 1: Initial Setup (Admin's Job)
Step 1: Admin Logs In

Admin opens browser and navigates to https://performancetrack.company.com
Landing page shows a login form with fields: Email and Password
Admin enters credentials: admin@company.com and password
System sends POST request to backend API: /api/auth/login
Backend validates credentials against User table in database
Database returns User record with Role = "Admin"
Backend creates audit log entry:

AuditID: Auto-generated
UserID: Admin's ID
Action: "LOGIN"
Timestamp: Current date/time
IPAddress: Admin's IP
Details: "Admin logged in successfully"


Backend generates JWT token (authentication token)
Frontend receives token and stores it in browser session memory
User is redirected to Admin Dashboard

Step 2: Admin Creates Review Cycle

Admin sees Admin Dashboard with menu options:

Manage Users
Configure Review Cycles
Generate Reports
View Audit Logs ← New option


Admin clicks "Configure Review Cycles"
System shows list of existing review cycles with columns:

Title
Start Date
End Date
Status
Created By
Last Modified


Admin clicks "+ Create New Review Cycle" button
A modal/form appears with fields:

Title: "Q1 2026 Performance Review"
Start Date: January 1, 2026
End Date: March 31, 2026
Status: Active (dropdown)
Description: Text area for cycle details


Admin fills form and clicks "Save"
Frontend validates all fields are filled
Frontend validates end date is after start date
Frontend validates dates don't overlap with existing active cycles
System sends POST request to /api/review-cycles with data
Backend receives request, validates data
Backend inserts new record into ReviewCycle table:

CycleID: Auto-generated (e.g., 101)
Title: "Q1 2026 Performance Review"
StartDate: 2026-01-01
EndDate: 2026-03-31
Status: "Active"
CreatedBy: Admin's UserID
CreatedDate: Current timestamp


Backend creates audit log:

Action: "CREATE_REVIEW_CYCLE"
Details: "Created review cycle 'Q1 2026 Performance Review' from 2026-01-01 to 2026-03-31"
EntityType: "ReviewCycle"
EntityID: 101


Database confirms successful insertion
Backend returns success response with new CycleID
Frontend shows success message: "Review cycle created successfully!"
Review cycle list refreshes automatically showing new cycle

Step 3: Admin Creates User Accounts

Admin clicks "Manage Users" from menu
System displays user list with columns:

Name
Email
Role
Department
Status
Last Login
Created Date


Admin can filter by department, role, or status
Admin clicks "+ Add New User"
Form appears with fields:

Name: "Rahul Sharma"
Email: "rahul.sharma@company.com"
Role: Employee (dropdown: Employee/Manager/Admin)
Department: "Engineering" (dropdown with predefined departments)
Reporting Manager: Dropdown showing all managers in Engineering
Status: Active (default)


Admin fills form selecting Priya Patel as reporting manager
Admin clicks "Create User"
POST request sent to /api/users
Backend validates:

Email is unique
Email format is valid
Reporting manager exists and has Manager role
Department exists


Backend creates user record in database:

UserID: 501 (auto-generated)
Name: Rahul Sharma
Role: Employee
Email: rahul.sharma@company.com
Department: Engineering
ManagerID: 502 (Priya Patel's ID)
Status: Active
CreatedBy: Admin's UserID
CreatedDate: Current timestamp


Backend generates temporary password using secure random generator
Backend hashes the password with salt
Backend creates audit log:

Action: "CREATE_USER"
Details: "Created employee account for Rahul Sharma in Engineering department"
EntityType: "User"
EntityID: 501


System creates notification record in Notification table:

NotificationID: 1001
UserID: 501
Type: "AccountCreated"
Message: "Your account has been created. Temporary password: [password]. Please log in and change your password immediately."
Status: "Unread"
CreatedDate: Current timestamp


Success message shown to admin with temporary password displayed
Admin can copy password to share with employee
Admin repeats this process for managers, creating user "Priya Patel" with Role = "Manager"
Each user creation generates its own audit log entry

Step 4: Admin Views Audit Logs

Admin clicks "View Audit Logs" from menu
Audit Logs page loads with filtering options:

Date Range: From [date picker] To [date picker]
User: Dropdown of all users (shows "All Users" by default)
Action Type: Dropdown (LOGIN, CREATE_USER, UPDATE_GOAL, etc.)
Entity Type: Dropdown (User, Goal, ReviewCycle, etc.)
Search: Text input for searching details


Admin selects:

Date Range: Last 7 days
Action Type: All


Page displays audit trail table:

Timestamp
User (who performed action)
Action
Entity Type
Entity ID
IP Address
Details
Status (Success/Failed)


Results show paginated list with 50 entries per page:

   | Timestamp           | User          | Action              | Entity      | Details                           |
   |---------------------|---------------|---------------------|-------------|-----------------------------------|
   | 2026-01-16 10:30:15 | Admin User    | CREATE_REVIEW_CYCLE | ReviewCycle | Created review cycle 'Q1 2026...' |
   | 2026-01-16 10:35:22 | Admin User    | CREATE_USER         | User        | Created employee account for...    |
   | 2026-01-16 10:36:10 | Admin User    | CREATE_USER         | User        | Created manager account for...     |

Admin can click any row to see full details in a modal:

Complete JSON of what was changed
Before/After values (if update)
IP Address
Browser/Device information
Success/Error status


Admin can export audit logs:

Click "Export" button
Select format: CSV or PDF
Select date range
System generates file with all audit entries
Download begins automatically


All audit log views are themselves logged:

Action: "VIEW_AUDIT_LOGS"
Details: "Viewed audit logs for date range [dates]"




PHASE 2: Employee Sets Goals with Evidence
Step 1: Employee First Login

Rahul (employee) receives login credentials via email or direct communication
Opens https://performancetrack.company.com
Enters email: rahul.sharma@company.com
Enters temporary password provided by admin
System authenticates:

POST request to /api/auth/login
Backend validates credentials
Checks if password is temporary


Backend creates audit log:

Action: "LOGIN"
UserID: 501
Status: "Success - First Login"
IPAddress: User's IP


JWT token generated with role = "Employee"
System detects first login flag
Redirects to "Change Password" page (mandatory)
Rahul enters:

Current Password: [temporary password]
New Password: [strong password]
Confirm Password: [same strong password]


Frontend validates:

Passwords match
Password meets complexity requirements (min 8 chars, uppercase, lowercase, number, special char)


PUT request to /api/users/501/password
Backend hashes new password
Updates User table removing first login flag
Backend creates audit log:

Action: "CHANGE_PASSWORD"
Details: "User changed password on first login"


Success message shown
User redirected to Employee Dashboard

Step 2: Employee Dashboard View

Dashboard loads with sections:

My Goals (shows goal statistics: Total, Pending Approval, In Progress, Completed, Rejected)
My Reviews (shows active review cycles)
Notifications (shows red badge with "1")
Performance History (past reviews)
Quick Stats Widget:

Goals This Quarter: 0
Completion Rate: 0%
Current Rating: N/A




Frontend makes GET request to /api/notifications?userId=501&status=unread
Backend queries Notification table
Returns notification about account creation
Notification panel shows: "Your account has been created. Please change your password."
Rahul clicks notification, status changes from "Unread" to "Read"
PUT request sent to /api/notifications/1001 with {status: "Read"}
Backend creates audit log:

Action: "READ_NOTIFICATION"
EntityType: "Notification"
EntityID: 1001



Step 3: Creating a Goal with Evidence Fields

Rahul clicks "My Goals" tab
Page loads showing:

Filter options: Status (All/Pending/InProgress/Completed/Rejected)
Sort options: Date, Title, Status
Goal cards in grid or list view


Currently sees empty state: "No goals set yet. Start by creating your first goal!"
Clicks "+ Add New Goal" button
Comprehensive goal creation form appears with sections:
Section 1: Basic Information

Title: [text input, max 100 chars]
Description: [rich text editor, max 500 chars]
Category: Dropdown (Technical, Leadership, Process Improvement, Customer Focus, Innovation)
Priority: Dropdown (High, Medium, Low)

Section 2: Timeline

Start Date: [date picker]
End Date: [date picker]
Milestones: [Optional - add intermediate checkpoints]

Add Milestone button
Each milestone has: Name, Target Date, Description



Section 3: Success Criteria

Measurable Criteria: [textarea]
Expected Outcome: [textarea]
Key Performance Indicators: [textarea with examples shown]

Section 4: Evidence Planning ← New Section

Evidence Type: Checkboxes (Metrics/Reports, Documentation, Code Repository Links, Design Documents, Customer Feedback, Other)
Evidence Description: [textarea] "Describe what evidence you will provide to prove goal completion"
Placeholder text shows: "E.g., Performance monitoring dashboard showing API response times, before/after comparison reports, GitHub repository links showing optimization commits"

Section 5: Alignment

Aligns with Company Goal: Dropdown (list of company objectives)
Department Goal: Dropdown (if applicable)


Rahul fills in comprehensive details:

Title: "Reduce API response time by 30%"
Description: "Optimize database queries and implement caching to improve API performance from average 300ms to 210ms or below. This will enhance user experience and reduce server costs."
Category: Technical
Priority: High
Start Date: January 15, 2026
End Date: March 15, 2026
Milestones:

Milestone 1: "Complete query analysis" - Feb 1, 2026
Milestone 2: "Implement caching layer" - Feb 15, 2026
Milestone 3: "Optimize critical queries" - Feb 28, 2026


Success Criteria: "API response time reduced from 300ms to 210ms or below as measured by APM tool"
Expected Outcome: "Improved user experience, reduced server load by 20%, faster page load times"
Key Performance Indicators: "Average response time, P95 response time, Number of queries optimized, Cache hit rate"
Evidence Type: [✓] Metrics/Reports, [✓] Code Repository Links, [✓] Documentation
Evidence Description: "Will provide: 1) Performance monitoring dashboard screenshots showing before/after metrics, 2) GitHub repository links to optimization commits, 3) Technical documentation of caching implementation, 4) APM tool reports comparing Jan vs March performance"
Aligns with Company Goal: "Improve Product Performance"


Rahul clicks "Save Goal"
Frontend validation checks:

All required fields filled
End date is after start date
Dates are within current review cycle (Q1 2026)
Title is not empty and under char limit
At least one evidence type selected
Evidence description provided


If validation fails, error messages appear next to respective fields
If validation passes, confirmation dialog: "Submit goal for manager approval?"
Rahul confirms
POST request sent to /api/goals with complete goal data
Backend receives request
Backend validates:

User has permission (authenticated and creating goal for themselves)
Manager exists for this employee
Dates are valid
Required fields present


Backend inserts into Goal table:

GoalID: 2001 (auto-generated)
Title: "Reduce API response time by 30%"
Description: [full description]
Category: "Technical"
Priority: "High"
AssignedToUserID: 501
StartDate: 2026-01-15
EndDate: 2026-03-15
Status: "PendingApproval"
SuccessCriteria: [criteria text]
ExpectedOutcome: [outcome text]
EvidenceTypes: "Metrics/Reports,Code Repository Links,Documentation"
EvidencePlannedDescription: [evidence description]
CreatedDate: Current timestamp
LastModifiedDate: Current timestamp


Backend inserts milestones into GoalMilestone table (separate table)
Backend creates audit log:

Action: "CREATE_GOAL"
UserID: 501
EntityType: "Goal"
EntityID: 2001
Details: "Created goal 'Reduce API response time by 30%' with status PendingApproval"
Timestamp: Current time


Backend creates notification for manager (Priya):

NotificationID: 1002
UserID: 502 (Priya's ID)
Type: "GoalSubmittedForApproval"
Message: "Rahul Sharma has submitted a new goal 'Reduce API response time by 30%' for your approval"
RelatedEntityType: "Goal"
RelatedEntityID: 2001
Status: "Unread"
Priority: "Normal"
CreatedDate: Current timestamp


Backend returns success response with GoalID
Frontend shows success message with animation: "Goal submitted successfully! Your manager will review it shortly."
Goal appears in "My Goals" list with status badge "Pending Approval" (yellow/orange color)
Goal card shows:

Title
Status badge
Start and End dates
Progress: 0%
Action buttons: View Details, Edit (disabled until approved), Delete



Step 4: Employee Creates More Goals

Rahul clicks "+ Add New Goal" again
Creates second goal:

Title: "Complete React Advanced Certification"
Description: "Complete comprehensive React Advanced certification from accredited platform to improve frontend development skills"
Category: Technical
Priority: Medium
Dates: Jan 15 - Mar 31, 2026
Milestones:

Complete modules 1-5: Feb 10
Complete modules 6-10: Feb 28
Pass final exam: Mar 25


Evidence Type: [✓] Documentation, [✓] Other
Evidence Description: "Will provide: 1) Link to digital certificate on platform, 2) Screenshot of completed modules, 3) Certificate PDF link from Google Drive"
Aligns with: "Employee Development"


Submits goal (GoalID: 2002)
Audit log created for second goal creation
Rahul creates third goal:

Title: "Mentor 2 Junior Developers"
Description: "Provide guidance and mentorship to Amit Singh and Sneha Gupta in React development and best practices"
Category: Leadership
Priority: Medium
Dates: Jan 20 - Mar 31, 2026
Milestones:

Initial assessment and goal setting with mentees: Jan 25
Monthly progress review #1: Feb 15
Monthly progress review #2: Mar 15


Evidence Type: [✓] Documentation, [✓] Customer Feedback
Evidence Description: "Will provide: 1) Mentoring session logs with dates and topics covered, 2) Mentee feedback forms, 3) Links to mentees' project work showing improvement, 4) Monthly progress report documents"
Aligns with: "Team Development"


Submits goal (GoalID: 2003)
Audit log created for third goal creation
All three goals show status "Pending Approval"
Dashboard updates:

Total Goals: 3
Pending Approval: 3
In Progress: 0
Completed: 0




PHASE 3: Manager Reviews and Approves Goals
Step 1: Manager Login and Dashboard

Priya (manager) logs into system
Audit log created for manager login
Sees Manager Dashboard with enhanced sections:

Team Overview Widget:

Total Team Members: 12
Active Goals: 15
Goals Pending My Approval: 3 (highlighted in orange)
Reviews Pending: 0
Avg Team Performance: 3.8/5


Team Goals (pending approvals)
Goal Completion Requests ← New section
Performance Reviews (employees to review)
Notifications (red badge showing "3")
Team Analytics (charts and graphs)



Step 2: Manager Checks Notifications

Clicks notification bell icon
Dropdown panel shows notifications sorted by priority and date:

"Rahul Sharma has submitted a new goal 'Reduce API response time by 30%' for your approval" (2 hours ago)
"Rahul Sharma has submitted a new goal 'Complete React Advanced Certification' for your approval" (1 hour ago)
"Rahul Sharma has submitted a new goal 'Mentor 2 Junior Developers' for your approval" (1 hour ago)


Each notification shows:

Employee name and avatar
Goal title (truncated)
Time ago
Action button: "Review Goal"


Clicks first notification
System marks notification as read
PUT request to /api/notifications/1002 updates status to "Read"
Audit log created:

Action: "READ_NOTIFICATION"
EntityType: "Notification"
EntityID: 1002


Redirects to Team Goals page with filter auto-applied for Rahul's goals

Step 3: Manager Reviews Goals in Detail

Team Goals page shows comprehensive table:

   | Employee      | Goal Title                            | Category  | Priority | Start Date | End Date   | Status          | Actions        |
   |---------------|---------------------------------------|-----------|----------|------------|------------|-----------------|----------------|
   | Rahul Sharma  | Reduce API response time by 30%       | Technical | High     | Jan 15     | Mar 15     | Pending Approval| Review/Approve |
   | Rahul Sharma  | Complete React Advanced Certification | Technical | Medium   | Jan 15     | Mar 31     | Pending Approval| Review/Approve |
   | Rahul Sharma  | Mentor 2 Junior Developers            | Leadership| Medium   | Jan 20     | Mar 31     | Pending Approval| Review/Approve |

Table features:

Filter by: Employee, Status, Category, Priority, Date Range
Sort by: Any column
Bulk actions: Select multiple goals to approve/reject together
Export to Excel


Priya clicks "Review" button on first goal (API response time)
Detailed goal review modal/page opens showing:
Left Panel - Goal Details:

Goal Title (large, prominent)
Employee: Rahul Sharma (with profile picture)
Department: Engineering
Created Date: Jan 15, 2026 10:30 AM
Status Badge: Pending Approval

Main Content Area:
Section: Basic Information

Description: [full text]
Category: Technical
Priority: High
Timeline: Jan 15 - Mar 15, 2026 (60 days)
Alignment: Improves Product Performance

Section: Success Criteria & Outcomes

Measurable Criteria: [full text]
Expected Outcome: [full text]
Key Performance Indicators: [full text]

Section: Milestones

Table showing all milestones with target dates

Section: Planned Evidence

Evidence Types: Metrics/Reports, Code Repository Links, Documentation
Evidence Description: [full text showing what Rahul plans to provide]

Section: Manager Assessment Area

Feasibility: Dropdown (Highly Feasible, Feasible, Needs Adjustment, Not Feasible)
Resource Requirements: Text area for manager notes
Dependencies: Text area to note any dependencies
Comments: Rich text editor for detailed feedback
Suggested Modifications: Text area (optional)

Action Buttons:

✅ Approve Goal
📝 Request Changes
❌ Reject Goal
💾 Save Draft (save assessment without final decision)


Priya reviews all details carefully
Assesses the goal:

Feasibility: Highly Feasible
Resource Requirements: "Will need access to APM tool dashboard. Coordinate with DevOps for Redis implementation."
Dependencies: "Requires DevOps team support for caching infrastructure setup"
Comments: "Excellent goal aligned with our Q1 performance objectives. The 30% improvement target is ambitious but achievable based on current baseline. Evidence plan is comprehensive and measurable."


Priya clicks "Approve Goal"
Confirmation dialog appears:

"Are you sure you want to approve this goal?"
"The employee will be notified and can begin working on it immediately."
Checkbox: "Send notification to employee" (checked by default)
Text preview of notification message (editable)


Priya confirms
PUT request sent to /api/goals/2001/approve with manager's assessment data
Backend processes approval:

Updates Goal table:

Status: "PendingApproval" → "InProgress"
ApprovedBy: 502 (Priya's UserID)
ApprovedDate: Current timestamp
ManagerAssessment: [JSON of all assessment fields]
LastModifiedDate: Current timestamp


Creates audit log:

Action: "APPROVE_GOAL"
UserID: 502 (Priya)
EntityType: "Goal"
EntityID: 2001
Details: "Approved goal 'Reduce API response time by 30%' submitted by Rahul Sharma (UserID: 501)"
BeforeValue: "Status: PendingApproval"
AfterValue: "Status: InProgress"
Timestamp: Current time


Creates notification for Rahul:

Type: "GoalApproved"
Message: "Great news! Your goal 'Reduce API response time by 30%' has been approved by Priya Patel. You can now start working on it. Manager's comments: [comments]"
Priority: "High"
Status: "Unread"




Backend returns success response
Frontend shows success toast: "Goal approved successfully!"
Goal moves from "Pending Approval" list
Priya's dashboard updates: Goals Pending My Approval: 3 → 2

Step 4: Manager Requests Changes on Second Goal

Priya clicks "Review" on second goal: "Complete React Advanced Certification"
Reviews all details in the same modal format
Notices the goal says "accredited platform" but doesn't specify which one
Fills assessment:

Feasibility: Feasible
Comments: "Good goal for skill development. However, please specify which certification platform you'll use (e.g., Udemy, Coursera, Pluralsight) and provide the course name/URL."


Clicks "Request Changes" button
Additional modal opens:

"What changes would you like to request?"
Required Changes: [rich text editor with Priya's specific feedback]
Severity: Dropdown (Minor Changes, Major Revision, Needs Complete Rework)
Due Date for Resubmission: [date picker]


Priya fills:

Required Changes: "Please update the goal description to include: 1) Specific certification platform name (Udemy/Coursera/etc), 2) Exact course title, 3) Course URL for my reference, 4) Total estimated hours for completion"
Severity: Minor Changes
Due Date: Jan 18, 2026


Clicks "Send Change Request"
PUT request to /api/goals/2002/request-changes
Backend updates Goal table:

Status remains "PendingApproval" but adds flag: "ChangesRequested"
ChangeRequestDetails: [JSON with Priya's feedback]
ChangeRequestedBy: 502
ChangeRequestedDate: Current timestamp
ResubmissionDueDate: 2026-01-18


Creates audit log:

Action: "REQUEST_GOAL_CHANGES"
Details: "Requested changes to goal 'Complete React Advanced Certification'"
AdditionalInfo: Change request details


Creates notification for Rahul:

Type: "GoalChangesRequested"
Message: "Priya Patel has requested changes to your goal 'Complete React Advanced Certification'. Please review the feedback and resubmit by Jan 18, 2026."
Priority: "High"
Status: "Unread"


Creates feedback record in Feedback table:

FeedbackID: 4001
GoalID: 2002
GivenByUserID: 502
FeedbackType: "ChangeRequest"
Comments: [Priya's required changes text]
Date: Current timestamp


Success message shown to Priya
Goal shows "Changes Requested" status with orange/amber badge

Step 5: Manager Approves Third Goal

Priya reviews third goal: "Mentor 2 Junior Developers"
Fills assessment:

Feasibility: Highly Feasible
Comments: "Excellent leadership initiative. Amit and Sneha will benefit greatly from your expertise. Please document your mentoring sessions and track their progress. Consider scheduling regular 1-on-1s bi-weekly."


Clicks "Approve Goal"
Confirmation and approval process same as first goal
Audit log created
Notification sent to Rahul
Status changes to "InProgress"

Step 6: Manager Dashboard Updates

Manager dashboard now shows:

Goals Pending My Approval: 1 (only the one with changes requested)
Team Active Goals: 2 (two approved goals now in progress)


Team Goals section shows summary cards:

Rahul Sharma: 2 In Progress, 1 Changes Requested
Other team members' goal statuses




PHASE 4: Employee Revises Goal and Begins Work
Step 1: Employee Receives Change Request Notification

Rahul logs in on Jan 16
Audit log created for login
Dashboard shows notification badge: 3 unread
Clicks notification bell
Sees three notifications:

"Great news! Your goal 'Reduce API response time by 30%' has been approved..."
"Priya Patel has requested changes to your goal 'Complete React Advanced Certification'..." (marked urgent with red indicator)
"Great news! Your goal 'Mentor 2 Junior Developers' has been approved..."


Clicks on the change request notification
Audit log created for reading notification
Redirected to goal detail page for certification goal

Step 2: Employee Reviews Change Request

Goal detail page shows:

Goal title with "Changes Requested" badge
Original goal details
Change Request Section (highlighted):

Requested By: Priya Patel
Request Date: Jan 15, 2026
Due Date: Jan 18, 2026 (countdown timer showing "2 days remaining")
Severity: Minor Changes
Required Changes: [Priya's detailed feedback]




Two action buttons:

"Edit & Resubmit Goal"
"Withdraw Goal"


Rahul clicks "Edit & Resubmit Goal"

Step 3: Employee Edits Goal

Goal editing form loads (same as creation form) with all fields pre-filled
Form shows banner at top: "Manager Feedback: [Priya's comments]"
Rahul updates:

Description: "Complete React Advanced Developer certification from Udemy platform to improve frontend development skills. Course: 'Complete React Developer in 2026' by Maximilian Schwarzmüller"
Evidence Description: Updated to include "Will provide: 1) Link to Udemy certificate upon completion (https://udemy.com/certificate/...), 2) Screenshot of course completion showing 100% progress, 3) Certificate PDF uploaded to Google Drive with shareable link"
Adds new field in description: "Estimated time: 40 hours total over 10 weeks"


Clicks "Resubmit Goal"
Confirmation dialog: "Submit updated goal for manager re-approval?"
Rahul confirms
PUT request to /api/goals/2002/resubmit
Backend updates Goal table:

Description: [updated text]
EvidencePlannedDescription: [updated text]
Status: "PendingApproval" (removes "ChangesRequested" flag)
ResubmittedDate: Current timestamp
RevisionCount: Increments by 1
LastModifiedDate: Current timestamp


Creates audit log:

Action: "RESUBMIT_GOAL"
Details: "Resubmitted goal after addressing change request"
RevisionNumber: 2


Creates notification for Priya:

Type: "GoalResubmitted"
Message: "Rahul Sharma has resubmitted the goal 'Complete React Advanced Certification' with requested changes"


Creates feedback record:

FeedbackType: "EmployeeResponse"
Comments: "Updated goal with specific platform (Udemy), course name, and estimated hours as requested"


Success message: "Goal resubmitted successfully! Your manager will review it shortly."
Goal status shows "Pending Re-approval"

Step 4: Manager Approves Revised Goal

Priya receives notification about resubmission
Reviews updated goal
Sees revision history showing:

Version 1: Original submission (Jan 15)
Version 2: Resubmission after changes (Jan 16)
Side-by-side comparison of what changed


Satisfied with updates
Fills new assessment: "Perfect! All required details included. Good choice on the Udemy course - it's comprehensive."
Approves goal
**Audit
Continue7:56 AMlog created**
8. Notification sent to Rahul
9. Status changes to "InProgress"
Step 5: Employee Begins Working on Goals

Rahul's dashboard now shows:

Total Goals: 3
In Progress: 3
Pending Approval: 0
Completed: 0


Goal list shows all three with "In Progress" status
Each goal card now shows:

Progress bar (0%)
"Update Progress" button
"Mark as Completed" button (grayed out until substantial progress)
Days remaining
Milestones widget




PHASE 5: Employee Updates Progress and Marks Goal Complete with Evidence
Step 1: Employee Tracks Progress (Mid-February)

Rahul logs in on Feb 14, 2026
Goes to "My Goals" section
Clicks on goal: "Reduce API response time by 30%"
Goal detail page shows:

Current Status: In Progress
Progress: 0%
Start Date: Jan 15
End Date: Mar 15
Days Elapsed: 30 / 60 days
Milestones:

✅ Complete query analysis (Feb 1) - Completed
✅ Implement caching layer (Feb 15) - Completed (today)
⏳ Optimize critical queries (Feb 28) - Pending




Clicks "Update Progress" button
Progress update modal opens:
Progress Update Form:
Section: Progress Percentage

Current Progress: Slider (0-100%) or input field
Rahul sets: 70%

Section: Work Done

Description: Rich text editor
Rahul enters: "Completed database query analysis identifying 25 slow queries. Implemented Redis caching layer for frequently accessed data. Achieved 25% response time reduction so far (300ms → 225ms). Optimized 10 critical queries using indexing and query rewriting."

Section: Milestone Updates

Shows milestone checklist
Rahul checks:

✅ Complete query analysis
✅ Implement caching layer



Section: Challenges & Blockers

Any challenges?: Textarea
Rahul enters: "Encountered some cache invalidation complexity which took extra time to resolve. DevOps team was very helpful."

Section: Next Steps

What's next?: Textarea
Rahul enters: "Will focus on optimizing remaining 15 queries. Expect to complete by Feb 28. Will monitor performance for one week before final assessment."

Section: Evidence (Optional for progress updates)

Can attach interim evidence if available
Rahul adds: "Performance monitoring dashboard showing current improvement: https://monitoring.company.com/dashboard/api-performance"


Clicks "Save Progress Update"
POST request to /api/goals/2001/progress
Backend creates GoalProgress record:

ProgressID: Auto-generated
GoalID: 2001
UpdateDate: Current timestamp
ProgressPercentage: 70
WorkDone: [text]
Challenges: [text]
NextSteps: [text]
InterimEvidence: [links]
UpdatedBy: 501


Backend updates Goal table:

CurrentProgress: 70
LastProgressUpdate: Current timestamp
Updates milestone statuses in GoalMilestone table


Creates audit log:

Action: "UPDATE_GOAL_PROGRESS"
Details: "Updated progress to 70% for goal 'Reduce API response time by 30%'"


Creates notification for manager (optional, based on settings):

Type: "GoalProgressUpdate"
Message: "Rahul Sharma updated progress on goal 'Reduce API response time by 30%' to 70%"
Priority: "Low"


Success message shown
Goal card updates showing 70% progress bar
Timeline widget shows progress trajectory

Step 2: Employee Completes Work (Early March)

Rahul completes all optimization work by March 5
Final performance metrics show:

Original: 300ms average response time
Current: 205ms average response time
Improvement: 31.7% (exceeding 30% target)


Prepares comprehensive evidence:

Takes screenshots of performance dashboard
Uploads screenshots to company Google Drive folder
Gets shareable links
Prepares technical documentation
Uploads to Google Drive
Gets shareable links
Collects GitHub commit links for optimization work
Generates APM report showing performance trends
Exports as PDF, uploads to Drive



Step 3: Employee Marks Goal as Complete and Submits Evidence

Rahul goes to goal detail page
Clicks "Mark as Completed" button (now active since progress > 80%)
Goal Completion Submission Form opens:
Header: "Submit Goal Completion for Approval"
Subtext: "Your manager will review the evidence before approving completion"
Section 1: Final Progress Summary

Final Progress: 100% (auto-set)
Completion Date: [date picker] - Rahul selects March 5, 2026
Overall Achievement Summary: [rich text editor]
Rahul enters: "Successfully reduced API response time by 31.7%, exceeding the target of 30%. Implemented Redis caching layer, optimized 25 database queries through indexing and query rewriting, and established monitoring dashboards for ongoing performance tracking. The optimization work resulted in improved user experience and reduced server load by 22%."

Section 2: Success Criteria Met

Original Criteria: [shows original success criteria]
How Criteria Was Met: [rich text editor]
Rahul details: "Target: Reduce from 300ms to 210ms or below. Achieved: Reduced to 205ms average. Measurement: APM tool (New Relic) monitored over 30-day period post-optimization showing consistent performance improvement."

Section 3: Evidence Submission ← Critical New Section
Planned Evidence Types (shows what was originally planned)

✓ Metrics/Reports
✓ Code Repository Links
✓ Documentation

Evidence Item #1

Evidence Type: Metrics/Reports
Evidence Title: "API Performance Comparison Report"
Description: "30-day performance comparison showing before/after metrics from New Relic APM"
Evidence Format: Document Link
Link/URL: [text input]

Rahul enters: "https://drive.google.com/file/d/1a2b3c4d5e/view?usp=sharing"


Verification Instructions: "Dashboard shows date range Jan 1-31 (before) vs Feb 15-Mar 15 (after) with average response times, P95/P99 percentiles, and request volume"
[Button: + Add Another Evidence Item]

Evidence Item #2

Evidence Type: Code Repository Links
Evidence Title: "Database Query Optimization Commits"
Description: "GitHub repository showing all optimization commits with code changes"
Evidence Format: Repository Link
Link/URL: "https://github.com/company/api-service/pull/456"

Additional Links:

"https://github.com/company/api-service/commit/abc123"
"https://github.com/company/api-service/commit/def456"




Verification Instructions: "Pull request #456 contains all optimization work. Review commits from Feb 1-28. Focus on files: db/queries.sql, cache/redis-config.js, models/optimization.js"

Evidence Item #3

Evidence Type: Documentation
Evidence Title: "Caching Implementation Technical Doc"
Description: "Comprehensive documentation of Redis caching architecture and implementation"
Evidence Format: Document Link
Link/URL: "https://drive.google.com/file/d/9z8y7x6w/view?usp=sharing"
Verification Instructions: "Document includes architecture diagrams, implementation details, cache invalidation strategy, and performance impact analysis"

Evidence Item #4

Evidence Type: Metrics/Reports
Evidence Title: "Performance Monitoring Dashboard Screenshots"
Description: "Before and after screenshots from New Relic dashboard"
Evidence Format: Image/Document Link
Link/URL: "https://drive.google.com/drive/folders/before-after-screenshots"
Verification Instructions: "Folder contains: 1) January baseline screenshots, 2) March post-optimization screenshots, 3) Comparison overlay image highlighting improvements"

[Button: + Add Another Evidence Item]
Section 4: Additional Notes

Lessons Learned: [textarea]
Rahul enters: "Redis cache invalidation requires careful strategy design. Working with DevOps early in the process was crucial. Database indexing had more impact than initially expected."
Unexpected Challenges: [textarea]
Rahul enters: "Initial cache implementation caused some data staleness issues which required additional week to resolve properly."
Future Recommendations: [textarea]
Rahul enters: "Recommend applying similar optimization patterns to other microservices. Consider implementing automated performance regression testing."

Section 5: Manager Review Request

Checkbox: ☑ "I confirm all evidence links are accessible and properly documented"
Checkbox: ☑ "I have verified all evidence links work and lead to correct resources"
Manager's Name: Priya Patel (auto-filled from reporting structure)
Special Instructions for Manager: [textarea]
Rahul enters: "Please review the GitHub PR for code quality. The performance dashboard shows real-time data - metrics may vary slightly but trend should be clear."


Rahul reviews all evidence links one more time by clicking each one to verify they work
Clicks "Submit for Manager Approval"
Confirmation Dialog:

"Are you sure you want to submit this goal for completion approval?"
"Your manager will review the evidence before final approval"
"You can view status updates in your notifications"
[Cancel] [Submit for Approval]


Rahul clicks "Submit for Approval"
POST request to /api/goals/2001/submit-completion with all evidence data
Backend processing:

Validates all required evidence types from original plan are submitted
Checks all URLs are properly formatted
Creates GoalCompletion record:

CompletionID: Auto-generated (e.g., 5001)
GoalID: 2001
SubmittedBy: 501
SubmissionDate: 2026-03-05
CompletionDate: 2026-03-05
AchievementSummary: [text]
SuccessCriteriaMet: [text]
LessonsLearned: [text]
Challenges: [text]
FutureRecommendations: [text]
ManagerInstructions: [text]
ApprovalStatus: "PendingManagerApproval"


Creates multiple GoalEvidence records (one for each evidence item):
Evidence Record 1:

EvidenceID: 6001
CompletionID: 5001
GoalID: 2001
EvidenceType: "Metrics/Reports"
EvidenceTitle: "API Performance Comparison Report"
Description: [text]
EvidenceFormat: "Document Link"
EvidenceURL: "https://drive.google.com/file/d/1a2b3c4d5e/view?usp=sharing"
VerificationInstructions: [text]
SubmittedDate: Current timestamp
VerificationStatus: "PendingVerification"

Evidence Record 2-4: Similar structure for other evidence items
Updates Goal table:

Status: "InProgress" → "CompletedPendingApproval"
CompletionSubmittedDate: Current timestamp
CurrentProgress: 100
LastModifiedDate: Current timestamp


Creates comprehensive audit log:

Action: "SUBMIT_GOAL_COMPLETION"
UserID: 501
EntityType: "Goal"
EntityID: 2001
Details: "Submitted goal completion with 4 evidence items for manager approval"
AdditionalData: JSON containing:

Evidence count: 4
Evidence types submitted
All evidence URLs
Completion date


BeforeValue: "Status: InProgress, Progress: 70%"
AfterValue: "Status: CompletedPendingApproval, Progress: 100%"
Timestamp: Current timestamp


Creates high-priority notification for Priya:

NotificationID: 1010
UserID: 502
Type: "GoalCompletionSubmitted"
Message: "Rahul Sharma has marked goal 'Reduce API response time by 30%' as completed and submitted evidence for your review and approval"
RelatedEntityType: "Goal"
RelatedEntityID: 2001
Priority: "High"
Status: "Unread"
ActionRequired: true
ActionURL: "/goals/2001/review-completion"
CreatedDate: Current timestamp




Backend returns success response with completion submission details
Frontend shows success animation and message:

"Goal completion submitted successfully! 🎉"
"Your manager will review the evidence and approve."
"You'll be notified once the review is complete."


Goal status updates to show badge: "Awaiting Completion Approval" (blue/purple color)
Goal card displays:

Progress: 100% (green bar)
Status: Awaiting Completion Approval
Submitted: Mar 5, 2026
Evidence Items: 4
Action button: "View Submission" (to see what was submitted)


Dashboard widget updates:

In Progress: 3 → 2
Awaiting Approval: 0 → 1




PHASE 6: Manager Reviews and Approves/Rejects Goal Completion
Step 1: Manager Receives Completion Notification

Priya logs in on March 6, 2026
Audit log created for login
Dashboard shows:

Notification badge: 1 (red, indicating action required)
Goal Completion Requests widget: 1 Pending Review (highlighted)


Sees notification: "Rahul Sharma has marked goal 'Reduce API response time by 30%' as completed..."
Clicks notification
Audit log created for reading notification
Redirected to Goal Completion Review page

Step 2: Manager Reviews Goal Completion Submission

Goal Completion Review Page loads with comprehensive layout:
Header Section:

Goal Title: "Reduce API response time by 30%"
Employee: Rahul Sharma (with profile picture and department)
Submitted Date: March 5, 2026
Original Timeline: Jan 15 - Mar 15, 2026
Completion Date: March 5, 2026 (10 days early)
Status Badge: Pending Manager Approval

Left Panel - Original Goal Information:

Original Description
Original Success Criteria
Original KPIs
Milestones (all showing completed)
Planned Evidence Types
Manager's Original Assessment (from approval)

Main Content Area - Completion Submission:
Tab 1: Achievement Summary (Active by default)

Final Progress: 100%
Overall Achievement Summary:

Shows Rahul's achievement summary text
Word count and detail level indicator


Success Criteria Evaluation:

Original Criteria: [shown in box]
How Met: [Rahul's explanation]
Side-by-side comparison for easy verification


Additional Achievements:

Lessons Learned section
Challenges Faced section
Future Recommendations section



Tab 2: Evidence Review ← Critical Tab

Evidence Summary Box:

Total Evidence Items Submitted: 4
Planned Evidence Types: 3
All Planned Types Submitted: ✓ Yes
Manager Review Status: 0/4 Verified


Evidence Items List:
Evidence Item #1:

Header Bar:

📊 Metrics/Reports | "API Performance Comparison Report"
Verification Status: ⏳ Not Yet Verified
[Button: Verify Evidence] [Button: Mark as Issues]


Details (Expandable):

Description: "30-day performance comparison showing before/after metrics from New Relic APM"
Format: Document Link
Evidence Link:

URL: https://drive.google.com/file/d/1a2b3c4d5e/view?usp=sharing
[Button: Open Link in New Tab] [Button: Copy Link]


Verification Instructions: "Dashboard shows date range Jan 1-31 (before) vs Feb 15-Mar 15 (after) with average response times, P95/P99 percentiles, and request volume"
Submitted: March 5, 2026 10:45 AM


Manager Verification Section:

Evidence Status: Dropdown (Not Verified, Verified - Acceptable, Verified - Excellent, Issues Found, Invalid)
Verification Notes: [textarea for manager's comments]
Verified Date: [auto-filled when status changed]



Evidence Item #2:

💻 Code Repository Links | "Database Query Optimization Commits"
Similar detailed structure as above
Multiple links displayed:

Main PR: https://github.com/company/api-service/pull/456
Commit 1: https://github.com/company/api-service/commit/abc123
Commit 2: https://github.com/company/api-service/commit/def456


Each link has [Open] button

Evidence Item #3:

📄 Documentation | "Caching Implementation Technical Doc"
Same structure

Evidence Item #4:

📊 Metrics/Reports | "Performance Monitoring Dashboard Screenshots"
Same structure



Tab 3: Progress History

Timeline showing all progress updates
Each update expandable to see details
Visual progress graph

Tab 4: Goal Timeline

Gantt-chart style view
Shows planned vs actual timeline
Milestone completion dates


Priya begins systematic evidence review
Clicks on Evidence Item #1
Clicks "Open Link in New Tab" button
New browser tab opens showing the Google Drive document
Priya reviews the performance comparison report:

Verifies date ranges (Jan 1-31 vs Feb 15-Mar 15)
Checks baseline metrics: 300ms average
Checks post-optimization metrics: 205ms average
Calculates improvement: 31.7%
Reviews P95 and P99 percentiles showing improvement
Checks request volume consistency
Verifies New Relic branding/authenticity


Returns to PerformanceTrack tab
In Evidence Item #1's verification section:

Selects Evidence Status: "Verified - Excellent"
Verification Notes: "Comprehensive performance report clearly showing 31.7% improvement. Data from New Relic APM is authentic and well-documented. Date ranges are appropriate for before/after comparison. Metrics exceed target of 30% reduction."
System auto-fills Verified Date: March 6, 2026 11:15 AM


Clicks "Save Verification" button
System updates:

EvidenceID 6001 record:

VerificationStatus: "Verified - Excellent"
VerifiedBy: 502
VerifiedDate: Current timestamp
VerificationNotes: [Priya's notes]


Creates audit log:

Action: "VERIFY_GOAL_EVIDENCE"
Details: "Verified evidence item 'API Performance Comparison Report' as Excellent"
EntityType: "GoalEvidence"
EntityID: 6001




Evidence item turns green with checkmark ✓
Summary updates: Manager Review Status: 1/4 Verified

Step 3: Manager Reviews Remaining Evidence

Priya clicks on Evidence Item #2 (GitHub commits)
Clicks "Open Link in New Tab" for the main PR
Reviews the pull request on GitHub:

Checks commit messages
Reviews code changes in query optimization files
Verifies indexing implementations
Checks caching layer code
Reviews Redis configuration
Looks at code quality and comments
Checks tests were added


Clicks additional commit links to review specific changes
Returns to PerformanceTrack
Verifies Evidence Item #2:

Status: "Verified - Acceptable"
Notes: "Code changes are solid and follow best practices. All 25 optimizations are documented in commits. Caching implementation is well-structured. Would have liked to see more unit tests, but overall excellent work."


Audit log created
Evidence marked verified (2/4)
Priya clicks on Evidence Item #3 (Technical Documentation)
Opens Google Drive document link
Reviews 12-page technical documentation:

Architecture diagrams present and clear
Implementation details comprehensive
Cache invalidation strategy well-documented
Performance impact analysis included
Future maintenance notes included


Returns to PerformanceTrack
Verifies Evidence Item #3:

Status: "Verified - Excellent"
Notes: "Outstanding documentation. Architecture diagrams are clear and professional. Implementation details will be valuable for team reference. Cache invalidation strategy shows deep understanding of the challenges."


Audit log created
Evidence marked verified (3/4)
Priya clicks on Evidence Item #4 (Screenshots)
Opens Google Drive folder link
Folder contains:

"January-Baseline-Dashboard.png"
"March-Optimized-Dashboard.png"
"Comparison-Overlay.png"
"Weekly-Trend-Graph.png"


Priya views each screenshot:

Verifies New Relic interface authenticity
Checks timestamps in screenshots match claimed dates
Validates metrics shown match reported numbers
Confirms screenshots are genuine and unaltered


Returns to PerformanceTrack
Verifies Evidence Item #4:

Status: "Verified - Excellent"
Notes: "Clear visual evidence of improvement. Screenshots properly timestamped and authentic. Comparison overlay is particularly effective at showing impact."


Audit log created
Evidence marked verified (4/4)
All evidence items now show green checkmarks
Summary shows: Manager Review Status: 4/4 Verified ✓

Step 4: Manager Provides Final Completion Approval

With all evidence verified, Priya moves to final decision section
Manager's Completion Decision Panel appears at bottom:
Overall Assessment:

Goal Achievement Level: Dropdown

Did Not Meet Goal
Partially Met Goal
Met Goal
Exceeded Goal ← Priya selects this


Performance Rating for this Goal: Scale 1-5

Priya selects: 5 (Outstanding)


Completion Approval: Radio buttons

⭕ Approve Completion (Goal successfully completed)
⭕ Request Additional Evidence (Need more proof)
⭕ Reject Completion (Goal not adequately completed)


Manager's Final Comments: [Rich text editor]
Priya writes:



     Exceptional work, Rahul! You not only met the goal but exceeded it significantly:
     
     Achievements:
     - Exceeded target: 31.7% improvement vs 30% target
     - Completed 10 days ahead of schedule
     - Comprehensive documentation for team knowledge sharing
     - Clean, well-tested code implementation
     
     Evidence Quality:
     - All evidence thoroughly documented and accessible
     - Performance metrics from reliable APM tool
     - Code repository shows professional implementation
     - Technical documentation is exemplary
     
     This work will have lasting positive impact on user experience and system performance. 
     Excellent example of technical excellence and project management.

Impact Assessment: [Textarea]
Priya writes: "Direct impact: 31.7% faster API responses improving user experience across all features. Indirect impact: Documentation and code patterns can be replicated for other microservices. Estimated cost savings: $5,000/month in reduced server load."
Development Areas (Optional): [Textarea]
Priya writes: "For future projects, consider adding automated performance regression tests to catch degradation early."


Priya reviews her assessment
Clicks "Approve Goal Completion" button (large, prominent, green)
Final Confirmation Dialog:

"Approve Goal Completion?"
"This will mark the goal as successfully completed."
"The employee will be notified and this will contribute to their performance review."
Summary shown:

Achievement Level: Exceeded Goal
Rating: 5 - Outstanding
Evidence Items Verified: 4/4


Checkbox: ☑ "Include detailed feedback in notification"
Checkbox: ☑ "Add to employee's performance achievements"
[Cancel] [Confirm Approval]


Priya clicks "Confirm Approval"
System processing:

PUT request to /api/goals/2001/approve-completion
Backend updates GoalCompletion record:

ApprovalStatus: "PendingManagerApproval" → "Approved"
ApprovedBy: 502
ApprovalDate: 2026-03-06
AchievementLevel: "Exceeded"
ManagerRating: 5
ManagerComments: [Priya's detailed feedback]
ImpactAssessment: [text]
DevelopmentAreas: [text]


Backend updates Goal table:

Status: "CompletedPendingApproval" → "Completed"
CompletionApprovedDate: Current timestamp
FinalAchievementLevel: "Exceeded"
ManagerCompletionRating: 5
LastModifiedDate: Current timestamp


Backend updates all GoalEvidence records (already verified)
Creates comprehensive audit log:

Action: "APPROVE_GOAL_COMPLETION"
UserID: 502 (Priya)
EntityType: "Goal"
EntityID: 2001
Details: "Approved goal completion with rating 5/5. Achievement level: Exceeded Goal. All 4 evidence items verified."
AdditionalData: JSON containing:

Achievement level: "Exceeded"
Rating: 5
Evidence verified: 4/4
Approval date
Manager comments preview


BeforeValue: "Status: CompletedPendingApproval"
AfterValue: "Status: Completed, Rating: 5"
Timestamp: Current timestamp
IPAddress: Manager's IP


Creates celebratory notification for Rahul:

NotificationID: 1011
UserID: 501
Type: "GoalCompletionApproved"
Message: "🎉 Congratulations! Your goal 'Reduce API response time by 30%' has been approved as COMPLETED with Outstanding rating (5/5)! Your manager provided detailed feedback."
Priority: "High"
Status: "Unread"
CelebratoryFlag: true (triggers special UI animation)
ActionURL: "/goals/2001/completion-details"
CreatedDate: Current timestamp


Creates feedback record:

FeedbackID: 4010
GoalID: 2001
GivenByUserID: 502
FeedbackType: "CompletionApproval"
Comments: [Full manager feedback]
Rating: 5
Date: Current timestamp


Updates employee's performance metrics aggregate:

Table: EmployeePerformanceMetrics
Increments: CompletedGoalsCount
Updates: AverageGoalRating
Adds to: ExceededGoalsCount


Triggers achievement badge if applicable:

Checks if this is employee's first "Exceeded" goal
If yes, creates AchievementBadge record: "Overachiever"




Backend returns success response with celebration data
Frontend shows success animation with confetti effect: "Goal Completion Approved! 🎉"
Manager dashboard updates:

Goal Completion Requests: 1 → 0
Recent Activity widget shows completion approval


Success toast message: "You've successfully approved Rahul's goal completion!"

Step 5: Alternative Flow - Manager Requests Additional Evidence
If Priya had found evidence insufficient:

After reviewing evidence, if Priya finds issues:
In Evidence Item #2, instead of "Verified":

Selects Status: "Issues Found"
Verification Notes: "Cannot access GitHub repository - permission denied. Please ensure the repository is shared with manager email or provide public link."


Saves verification
In Manager's Completion Decision Panel:

Selects: "Request Additional Evidence"
Additional Evidence Request Form appears:

Specific Items Needing Attention: [Checkboxes of evidence items]

Priya checks: Evidence Item #2


Detailed Instructions: [Rich text editor]
Priya writes: "I cannot access the GitHub repository. Please either: 1) Add my email (priya.patel@company.com) as a collaborator on the private repo, OR 2) If repo must stay private, provide detailed screenshots of the PR showing all commits, files changed, and code review comments."
Due Date for Resubmission: [date picker]
Priya sets: March 10, 2026
Severity: Minor/Major
Priya selects: Minor




Clicks "Request Additional Evidence"
Confirmation dialog appears
Priya confirms
System processing:

GoalCompletion record:

ApprovalStatus: "AdditionalEvidenceRequired"
AdditionalEvidenceRequestedDate: Current timestamp
ResubmissionDueDate: 2026-03-10


Goal status: "CompletedPendingApproval" (unchanged, but flagged)
Creates audit log:

Action: "REQUEST_ADDITIONAL_EVIDENCE"
Details: "Requested additional evidence for goal completion - GitHub repository access issue"


Creates notification for Rahul:

Type: "AdditionalEvidenceRequested"
Message: "Additional evidence needed for goal 'Reduce API response time by 30%'. Please review manager's feedback and resubmit by March 10."
Priority: "High"


Creates feedback record with request details


Rahul receives notification
Can click "View Request" to see exact issues
Can update specific evidence items
Resubmits only the problematic evidence
Audit log created for resubmission
Manager receives notification to re-review
Process continues...

Step 6: Alternative Flow - Manager Rejects Completion
If Priya finds goal was not actually completed:

If after reviewing evidence, Priya determines goal not met:
Verifies evidence but finds it doesn't prove goal achievement:

Evidence Item #1 shows only 15% improvement (not 30%)


In Manager's Completion Decision Panel:

Selects: "Reject Completion"
Rejection Reason Form appears:

Primary Reason: Dropdown

Goal not adequately achieved
Evidence does not support claims
Success criteria not met
Quality issues


Detailed Explanation: [Required rich text editor]
Priya writes: "Based on the performance reports, the improvement achieved was 15%, which does not meet the 30% target specified in the goal. While good progress was made, the goal's success criteria have not been met. Recommendation




Continue8:00 AM: Continue optimization work and resubmit when 30% target is achieved."
- Next Steps for Employee: [Textarea]
- Priya writes: "Continue working on optimization. Focus on: 1) Further query optimization, 2) Expanding caching coverage, 3) Monitor for 2 more weeks to ensure sustained improvement. Feel free to schedule 1-on-1 to discuss approach."
- Allow Resubmission: Radio
- Yes, employee can continue working and resubmit
- No, mark goal as not achieved (impacts review)
- Priya selects: Yes, allow resubmission
- New Target Date (if resubmission allowed): March 20, 2026
4. Clicks "Reject Completion"
5. Severe confirmation dialog:

"Reject Goal Completion?"
"This will send the goal back to In Progress status."
"Employee can continue working and resubmit."
"Are you sure the evidence doesn't support completion?"
[Cancel] [Yes, Reject Completion]


Priya confirms
System processing:

GoalCompletion record:

ApprovalStatus: "Rejected"
RejectedBy: 502
RejectedDate: Current timestamp
RejectionReason: [text]
ResubmissionAllowed: true
NewTargetDate: 2026-03-20


Goal table:

Status: "CompletedPendingApproval" → "InProgress"
CurrentProgress: 100 → 70 (reset to pre-completion level)
CompletionRejectedCount: +1


Creates detailed audit log:

Action: "REJECT_GOAL_COMPLETION"
Details: "Rejected goal completion - evidence showed only 15% improvement vs 30% target"
Severity: "Medium"


Creates sensitive notification for Rahul:

Type: "GoalCompletionRejected"
Message: "Your manager has reviewed goal 'Reduce API response time by 30%' and determined it needs more work before completion. Detailed feedback provided. New target date: March 20."
Tone: Supportive, not punitive


Creates feedback record with constructive guidance


Employee receives notification with supportive messaging
Can review detailed feedback
Can continue work
Can resubmit when ready
All resubmission creates new audit logs


PHASE 7: Employee Views Completion Approval
Step 1: Employee Receives Celebration Notification

Rahul logs in on March 6 afternoon
Audit log created
Landing page shows special celebration animation (confetti, success badge)
Notification badge shows: 1 (with special gold/celebration color)
Auto-popup modal appears:

🎉 "Goal Achievement Unlocked!"
"Your goal 'Reduce API response time by 30%' has been approved as COMPLETED!"
"Achievement Level: Exceeded Goal"
"Manager Rating: 5/5 - Outstanding"
[Button: View Details] [Button: Dismiss]


Rahul clicks "View Details"

Step 2: Employee Reviews Completion Details

Goal Completion Details page loads:
Header:

✅ GOAL COMPLETED
"Reduce API response time by 30%"
Achievement Badge: "⭐ EXCEEDED GOAL"
Rating: 5/5 - Outstanding

Achievement Timeline:

Visual timeline showing:

Goal Created: Jan 15
Approved by Manager: Jan 16
Progress Updates: Feb 14 (70%)
Completed: March 5
Approved: March 6
Total Duration: 50 days (10 days early)



Your Achievement Summary:

Shows Rahul's original completion submission
Progress: 100%
All submitted evidence listed

Manager's Evaluation:

Achievement Level: Exceeded Goal
Rating: 5 out of 5 - Outstanding
Manager's Detailed Feedback:

[Shows Priya's full feedback]


Impact Assessment:

[Shows impact notes]


Development Areas:

[Shows growth suggestions]



Evidence Verification Status:

Shows all 4 evidence items
Each marked with green ✓ Verified by Manager
Can click to see manager's verification notes for each

Performance Impact:

"This achievement will be included in your Q1 2026 performance review"
"Rating contributes to overall performance score"
"Achievement badge unlocked: Overachiever 🏆"

Actions:

[Download Achievement Certificate (PDF)]
[Share Success (generates shareable summary)]
[View All My Goals]


Rahul reads through manager's detailed, positive feedback
Feels accomplished and motivated
Clicks "Download Achievement Certificate"
System generates PDF certificate:

Company logo
Employee name
Goal title
Achievement level
Rating
Manager signature (digital)
Date of completion
Unique certificate ID


PDF downloads automatically
Audit log created:

Action: "DOWNLOAD_ACHIEVEMENT_CERTIFICATE"
EntityType: "Goal"
EntityID: 2001



Step 3: Dashboard Updates

Rahul navigates to dashboard
Dashboard widgets show updated stats:

Goals Summary:

Total Goals: 3
In Progress: 2
Completed: 1
Completion Rate: 33%
Average Rating: 5.0


Recent Achievements:

Shows completed goal with rating and date


Performance Snapshot:

Q1 Goals: 3 Active
Completed On Time: 1
Achievement Badges: 1 🏆




My Goals section shows:

First goal: ✅ Completed - Exceeded (5/5)
Second goal: 🔄 In Progress (React certification)
Third goal: 🔄 In Progress (Mentoring)




PHASE 8: Review Cycle - Self Assessment with Evidence
Step 1: Review Period Begins

March 20, 2026 - Review cycle end approaching
System runs scheduled background job (cron job) daily at 9:00 AM
Backend checks ReviewCycle table for cycles ending within 11 days
Finds "Q1 2026 Performance Review" (ends March 31)
Queries User table for all active employees
For each employee:

Creates notification:

Type: "ReviewReminder"
Message: "Q1 2026 Performance Review deadline is March 31. Please complete your self-assessment including evidence of your achievements."
Priority: "High"
ActionRequired: true


Audit log created for each notification sent


Notification batch sent to all 150 employees

Step 2: Employee Receives Review Reminder

Rahul logs in on March 21
Audit log created
Sees prominent banner: "Action Required: Q1 Performance Review Due March 31"
Notification shows review reminder
Dashboard shows Review Card:

"Q1 2026 Performance Review"
Status: Pending Your Self-Assessment
Deadline: March 31, 2026 (10 days remaining)
Progress: 0%
[Button: Start Self-Assessment]


Clicks notification or "Start Self-Assessment" button
Audit log created:

Action: "START_SELF_ASSESSMENT"
EntityType: "ReviewCycle"
EntityID: 101



Step 3: Employee Completes Self-Assessment with Evidence

Self-Assessment Form loads with multiple sections:
Header:

"Q1 2026 Performance Review - Self Assessment"
Review Period: January 1 - March 31, 2026
Your Manager: Priya Patel
Submission Deadline: March 31, 2026
Auto-save: Enabled (saves draft every 2 minutes)

Section 1: Goal Achievement Review

Auto-populated with all goals from Q1:

Goal 1: Reduce API response time by 30%

Status: ✅ Completed (Approved by Manager)
Manager Rating: 5/5
Your Achievement Summary: [Pre-filled from completion submission]
Evidence Submitted: 4 items (all verified)
Manager Feedback: [Shows Priya's feedback]
Reflection: [Editable textarea]

Rahul adds: "This goal pushed me to learn advanced optimization techniques. The challenge of cache invalidation taught me valuable architectural lessons. I'm proud of exceeding the target and creating documentation that the team can use."


[Checkbox] ☑ Include this achievement in self-assessment highlights

Goal 2: Complete React Advanced Certification

Status: 🔄 In Progress (85% complete)
Your Progress: [Textarea]

Rahul enters: "Completed 9 out of 10 modules. Scheduled final exam for March 28. Currently averaging 92% on all module quizzes. Gained strong understanding of React hooks, context API, and performance optimization."


Reason if not completed: [Textarea]

Rahul enters: "Originally planned for full completion by March 31. On track to complete by March 28. No significant blockers."


Evidence of Progress: [Evidence submission fields - similar to goal completion]

Evidence Item #1:

Type: Documentation
Title: "Course Progress Screenshot"
Link: "https://drive.google.com/file/d/course-progress-screenshot"
Description: "Udemy dashboard showing 9/10 modules completed with quiz scores"




[Checkbox] ☑ Plan to complete in Q2

Goal 3: Mentor 2 Junior Developers

Status: ✅ Completed (Pending Manager Approval for completion)
Your Achievement: [Textarea]

Rahul enters: "Successfully mentored Amit Singh and Sneha Gupta throughout Q1. Conducted bi-weekly 1-on-1 sessions, reviewed their code, provided guidance on React best practices, and supported their project work. Both mentees showed significant improvement in code quality and confidence."


Evidence Submitted: [Shows submitted evidence]

3 evidence items (awaiting manager review)


Reflection: [Textarea]

Rahul adds: "Mentoring taught me patience and improved my communication skills. Helping others learn reinforced my own understanding. Very rewarding experience."



Goal Achievement Summary Widget:

Total Goals Set: 3
Completed and Approved: 1
In Progress: 1
Pending Approval: 1
Average Manager Rating: 5.0

Section 2: Key Accomplishments Beyond Goals

"List significant achievements not covered in formal goals"
[Button: + Add Accomplishment]

Accomplishment #1:

Title: "Led Technical Debt Reduction Initiative"
Description: [Rich text editor]

Rahul writes: "Identified and resolved 15 critical technical debt items in the codebase. Refactored legacy authentication module, updated deprecated dependencies, and improved code test coverage from 45% to 68%."


Impact: [Textarea]

Rahul writes: "Reduced bug count by 30%, improved code maintainability, faster onboarding for new team members"


Evidence: [Optional evidence fields]

Link: "https://github.com/company/api-service/projects/tech-debt"
Description: "GitHub project board showing all technical debt items addressed"


Time Period: January - March 2026

Accomplishment #2:

Title: "Contributed to Cross-Team API Standards"
Description: "Participated in architecture guild meetings. Contributed to company-wide API design standards documentation. Shared API optimization learnings with backend team."
Impact: "Improved API consistency across teams, knowledge sharing"
Evidence: "https://drive.google.com/company-api-standards-doc"

Section 3: Core Competencies Self-Rating

Instructions: "Rate yourself on core competencies (1-5 scale)"
Rating scale: 1=Needs Development, 2=Developing, 3=Proficient, 4=Advanced, 5=Expert

Technical Skills:

Backend Development: 4 (Advanced)
Database Optimization: 4 (Advanced)
System Architecture: 3 (Proficient)
Code Quality: 4 (Advanced)
Comments box for each competency to justify rating

Professional Skills:

Communication: 4 (Advanced)
Collaboration: 4 (Advanced)
Problem Solving: 4 (Advanced)
Time Management: 4 (Advanced)
Initiative: 4 (Advanced)

Leadership Skills:

Mentoring: 4 (Advanced)
Technical Leadership: 3 (Proficient)
Knowledge Sharing: 4 (Advanced)

Section 4: Challenges & Learning

Challenges Faced:

[Textarea] Rahul writes: "Main challenge was balancing optimization work with regular feature development. Cache invalidation complexity required more research than anticipated. Mentoring while maintaining own productivity required time management adjustment."


How You Overcame Them:

[Textarea] Rahul writes: "Created detailed project plan with time blocks. Sought help from DevOps team early. Scheduled dedicated mentoring hours. Improved efficiency through better planning."


Key Learnings:

[Textarea] Rahul writes: "Learned advanced Redis caching strategies, database query optimization, importance of documentation, mentoring improves own understanding, proactive communication prevents blockers."


Skills Developed:

[Tag input] Rahul adds: Redis, Database Indexing, Performance Monitoring, Technical Writing, Mentoring, Code Review



Section 5: Overall Self-Rating

Overall Performance Rating: [Scale 1-5]

Rahul selects: 4 (Exceeds Expectations)


Justification: [Required textarea, min 100 words]

Rahul writes: "I believe I exceeded expectations this quarter based on:
Goal Achievement: Completed critical API optimization goal 10 days early and exceeded target by 1.7%. Quality of work recognized by manager with 5/5 rating. Comprehensive documentation benefits entire team.
Additional Contributions: Led technical debt reduction improving codebase health. Actively contributed to company standards. Successfully mentored two junior developers.
Growth: Developed new technical skills in caching and optimization. Improved leadership abilities through mentoring. Enhanced communication through documentation.
Impact: Work directly improved user experience and reduced costs. Knowledge sharing amplified individual contribution.
While React certification isn't complete, I'm 85% done and on track for early Q2 completion. Overall, I delivered significant value beyond base expectations."



Section 6: Development & Future Goals

Areas for Development:

[Textarea] Rahul writes: "Want to develop: System architecture design skills, public speaking for tech talks, automated testing expertise, deeper understanding of distributed systems"


Support Needed:

[Textarea] Rahul writes: "Would benefit from: Architecture design reviews with senior engineers, opportunity to present at team tech talks, advanced distributed systems course, shadowing architecture decisions"


Q2 2026 Goal Ideas:

[Can add preliminary goal ideas]
Rahul adds:

"Design and implement distributed caching strategy for microservices"
"Present technical talk on API optimization to engineering team"
"Achieve 80% test coverage for core modules"
"Complete advanced system design course"





Section 7: Feedback for Manager/Organization

What's working well:

[Textarea] Rahul writes: "Clear communication from manager, good balance of autonomy and support, team collaboration is excellent, tools and resources are adequate"


Suggestions for improvement:

[Textarea] Rahul writes: "Would appreciate more architectural design discussions as learning opportunity, bi-weekly 1-on-1s sometimes get cancelled - prefer keeping schedule, access to more advanced training platforms"



Section 8: Review & Submit

Review checklist:

☑ All goals reviewed and reflected upon
☑ Accomplishments documented with evidence
☑ Competencies self-rated
☑ Overall rating justified
☑ Development areas identified
☑ All required fields completed


Character count: 2,847 / 3,000 recommended
Evidence items attached: 6
Completion: 100%
Submission Confirmation:

Checkbox: ☑ "I confirm this self-assessment is complete and accurate"
Checkbox: ☑ "I understand my manager will review and discuss this with me"
Note: "You can save as draft and return later, or submit now"




Rahul reviews entire self-assessment
Clicks "Save Draft" multiple times during completion (auto-saved anyway)
Audit log created for each manual save:

Action: "SAVE_SELF_ASSESSMENT_DRAFT"


After final review, clicks "Submit Self-Assessment"
Final confirmation dialog:

"Submit Self-Assessment?"
"Once submitted, you cannot edit until after manager review."
"Your manager will be notified immediately."
Summary stats shown:

Goals: 3 (1 completed, 1 in progress, 1 pending)
Additional Accomplishments: 2
Evidence Items: 6
Overall Self-Rating: 4/5


[Cancel] [Submit Assessment]


Rahul clicks "Submit Assessment"
POST request to /api/performance-reviews with complete assessment data
Backend processing:

Creates PerformanceReview record:

ReviewID: 3001
CycleID: 101
UserID: 501
SubmissionDate: 2026-03-21
SelfAssessment: [Full JSON of all responses]
SelfRating: 4
GoalsAchievementSummary: [Summary]
AdditionalAccomplishments: [JSON array]
CompetencySelfRatings: [JSON object]
ChallengesAndLearning: [Text]
DevelopmentAreas: [Text]
FutureGoals: [Array]
Feedback: [Text]
EvidenceCount: 6
Status: "AwaitingManagerReview"
ManagerFeedback: null
ManagerRating: null
FinalRating: null


Creates SelfAssessmentEvidence records for each evidence item
Creates comprehensive audit log:

Action: "SUBMIT_SELF_ASSESSMENT"
UserID: 501
EntityType: "PerformanceReview"
EntityID: 3001
Details: "Submitted Q1 2026 self-assessment with self-rating 4/5, 3 goals reviewed, 2 additional accomplishments, 6 evidence items"
AdditionalData: JSON with statistics
Timestamp: Current timestamp
WordCount: 2847


Creates high-priority notification for Priya:

Type: "SelfAssessmentSubmitted"
Message: "Rahul Sharma has submitted their Q1 2026 self-assessment for your review. Self-rating: 4/5 - Exceeds Expectations. 6 evidence items attached."
Priority: "High"
ActionRequired: true
ActionURL: "/performance-reviews/3001"


Updates ReviewCycle statistics:

Increments completed self-assessments counter




Success screen appears:

"Self-Assessment Submitted Successfully! ✓"
"Thank you for completing your Q1 2026 self-assessment"
"Your manager will review and schedule a discussion"
"Next Steps:"

Your manager will review (typically within 5 business days)
You'll receive notification when review is ready
A performance discussion meeting will be scheduled


[View Submitted Assessment] [Return to Dashboard]


Dashboard updates:

Q1 Review status: "Submitted - Awaiting Manager Review"
Progress indicator: Self-Assessment ✓ | Manager Review ⏳ | Discussion ⏳ | Finalized ⏳




PHASE 9: Manager Conducts Performance Review
Step 1: Manager Receives Self-Assessment Notification

Priya logs in on March 22
Audit log created
Dashboard shows:

Performance Reviews widget: 1 Pending Review (highlighted)
Notification: Rahul's self-assessment submitted


Clicks notification
Audit log created
Redirected to Performance Review page

Step 2: Manager Reviews Employee Self-Assessment

Performance Review Page loads with comprehensive interface:
Header Section:

"Q1 2026 Performance Review: Rahul Sharma"
Review Period: Jan 1 - Mar 31, 2026
Department: Engineering
Employee Self-Rating: 4/5 - Exceeds Expectations
Submitted: March 21, 2026
Manager Review Status: Pending

Left Sidebar - Quick Navigation:

Overview
Goals Review (3)
Accomplishments (2)
Competencies
Self-Reflection
Evidence (6 items)
Manager Assessment ← Current focus area

Main Content - Tabbed Interface:
Tab 1: Employee Self-Assessment (Default view)

Shows complete self-assessment submitted by Rahul
All sections displayed in read-only, well-formatted view
Evidence items shown with [Open Link] buttons
Manager can add inline comments/notes to any section

Tab 2: Goals Performance Analysis

Table showing all goals:

GoalStatusSelf-RatingManager Completion RatingEvidenceTargetActualVarianceAPI Response TimeCompletedN/A5/54 items✓30%31.7%+1.7% ✓React CertificationIn ProgressN/AN/A1 item100%85%-15% ⏳Mentor DevelopersPending ApprovalN/APending3 items100%100%Complete ⏳

Summary Statistics:

Goal Completion Rate: 33% (1/3 completed and approved)
Goals Pending Approval: 1
Goals In Progress: 1
Average Manager Rating (completed goals): 5.0
Goals Exceeded Target: 1


Each row expandable to show full details

Tab 3: Evidence Review

Similar to goal completion evidence review
Shows all 6 evidence items across goals and accomplishments
Manager can verify each item
Tracking: 0/6 reviewed

Tab 4: Competency Comparison

Side-by-side grid:

CompetencyEmployee Self-RatingManager RatingVarianceManager NotesBackend Development4 - Advanced[Dropdown]TBD[Textarea]Database Optimization4 - Advanced[Dropdown]TBD[Textarea]Communication4 - Advanced[Dropdown]TBD[Textarea]...............

Variance column auto-calculates when manager adds rating
Color coding: Green (agree), Yellow (±1), Red (±2+)


Priya systematically reviews each section
Starts with Goals Performance Analysis tab
Clicks on first goal to expand details
Reviews achievement against original goal
Adds manager notes in notes field

Step 3: Manager Reviews and Verifies Evidence

Priya clicks "Evidence Review" tab
Evidence Review Interface (similar to goal completion evidence review):
Evidence Item #1 (from completed API goal):

Type: 📊 Metrics/Reports
Title: "API Performance Comparison Report"
Linked to: Goal - Reduce API response time
Status: ✓ Previously Verified (during goal completion approval)
Verified by: Priya Patel on March 6, 2026
Verification Notes: [Shows previous notes]
Action: [Re-verify] [View] [Notes OK]

Evidence Items #2-4: Similar (already verified during goal completion)
Evidence Item #5 (new - from additional accomplishment):

Type: 💻 Code Repository Link
Title: "Technical Debt Reduction Project Board"
Linked to: Additional Accomplishment - Tech Debt Initiative
Link: https://github.com/company/api-service/projects/tech-debt
Description: "GitHub project board showing 15 technical debt items addressed"
Status: ⏳ Not Yet Verified
[Button: Open Link] [Button: Verify]


Priya clicks "Open Link" for Evidence #5
Reviews GitHub project board:

Sees 15 items marked "Done"
Reviews commits linked to each item
Checks code quality improvements
Verifies test coverage increase


Returns to PerformanceTrack
Clicks "Verify" on Evidence #5
Verification form:

Status: Dropdown → Selects "Verified - Excellent"
Notes: "Comprehensive technical debt cleanup. All 15 items properly documented and completed. Test coverage improvement from 45% to 68% is significant and measurable. Excellent initiative."


Saves verification
Audit log created:

Action: "VERIFY_REVIEW_EVIDENCE"
EntityID: Evidence #5's ID


Evidence #5 turns green with checkmark
Repeats for Evidence #6 (API Standards contribution):

Opens Drive link
Reviews API standards document
Sees Rahul's contributions in edit history
Verifies quality and value
Marks as "Verified - Acceptable"
Notes: "Good contribution to team standards. Participated actively in discussions. Shared valuable optimization insights."


Audit log created
All evidence now verified: 6/6 ✓

Step 4: Manager Provides Competency Ratings

Priya switches to "Competency Comparison" tab
Reviews employee self-ratings
For each competency, provides manager rating:
Backend Development:

Employee: 4 (Advanced)
Manager: 4 (Advanced)
Variance: 0 (Green - Agreement)
Manager Notes: "Demonstrated advanced backend skills through API optimization project. Clean code, proper architecture."

Database Optimization:

Employee: 4 (Advanced)
Manager: 5 (Expert)
Variance: +1 (Manager rates higher)
Manager Notes: "Exceeded advanced level. The 31% performance improvement and comprehensive approach to query optimization demonstrates expert-level capability."

Communication:

Employee: 4 (Advanced)
Manager: 4 (Advanced)
Variance: 0
Manager Notes: "Excellent written communication through documentation. Good in meetings. Clear status updates."

Mentoring:

Employee: 4 (Advanced)
Manager: 4 (Advanced)
Variance: 0
Manager Notes: "Strong mentoring skills. Both mentees showed improvement. Structured approach with regular sessions."


Continues through all 12 competencies
System auto-calculates:

Total competencies: 12
Agreement (0 variance): 9
Manager rated higher: 2
Manager rated lower: 1
Average variance: +0.17 (slight positive)



Step 5: Manager Completes Performance Assessment

Priya navigates to "Manager Assessment" section
Manager Performance Assessment Form loads:
Section 1: Overall Performance Rating

Manager's Performance Rating: [Scale 1-5]

1 - Needs Improvement
2 - Below Expectations
3 - Meets Expectations
4 - Exceeds Expectations ← Priya selects
5 - Outstanding


Rating Justification: [Required rich text editor, min 200 words]
Priya writes comprehensive assessment:



     Rahul demonstrated exceptional performance in Q1 2026 and clearly exceeded expectations:
     
     GOAL ACHIEVEMENT:
     - API Optimization: Exceptional work. Not only met but exceeded the 30% target with 31.7% improvement. Completed 10 days ahead of schedule. The comprehensive documentation and clean code implementation showcase professional excellence. My rating: 5/5.
     
     - React Certification: Good progress at 85% completion. On track to finish early Q2. Proactive about learning and skill development.
     
     - Mentoring: Successfully mentored two junior developers with structured approach. Both mentees showed measurable improvement. Demonstrates emerging leadership capabilities.
     
     ADDITIONAL CONTRIBUTIONS:
     - Technical Debt Initiative: Took initiative without being asked. Reduced bug count by 30%, improved test coverage from 45% to 68%. Significant impact on code quality and team velocity.
     
     - Cross-Team Collaboration: Active participation in architecture guild. Shared knowledge across teams. Contributed to company-wide standards.
     
     COMPETENCIES:
     - Technical Skills: Advanced to expert level in database optimization. Solid advanced level across other technical areas.
     - Professional Skills: Strong communication, collaboration, and problem-solving.
     - Leadership: Demonstrated leadership through mentoring and knowledge sharing.
     
     GROWTH:
     - Rapid skill development in caching strategies and performance optimization
     - Improved leadership through mentoring
     - Enhanced communication through documentation
     
     AREAS FOR DEVELOPMENT:
     - Agree with self-assessment on system architecture design
     - Encourage public speaking opportunities
     - Support advanced distributed systems learning
     
     Rahul's work this quarter delivered immediate value through performance improvements and long-term value through documentation and knowledge sharing. His initiative on technical debt and mentoring shows strong potential for increased responsibility.
Section 2: Strengths

Key Strengths Demonstrated: [Textarea]
Priya lists:

Technical Excellence: Expert-level database optimization and caching implementation
Initiative: Proactively identified and addressed technical debt
Quality Focus: Comprehensive documentation and testing
Collaboration: Effective mentoring and cross-team knowledge sharing
Continuous Learning: Pursuing certification while delivering projects
Results Orientation: Consistently delivers measurable outcomes



Section 3: Development Areas

Areas for Growth: [Textarea]
Priya writes:

System Architecture Design: Ready for more architectural responsibility. Recommend involving in design reviews.
Public Speaking: Build confidence through team tech talks before external presentations.
Test Automation: Deepen expertise in automated testing strategies and frameworks.
Strategic Thinking: Develop longer-term technical vision alongside tactical execution.



Section 4: Specific Feedback on Self-Assessment

Comments on Employee Self-Reflection: [Textarea]
Priya notes:

Self-assessment is accurate and well-reasoned
Appropriate self-rating of 4/5
Good self-awareness about strengths and development needs
Appreciate honest reflection on challenges
Development goals for Q2 are ambitious and aligned with growth path



Section 5: Goal-by-Goal Manager Comments

System shows each goal with textarea for comments
Priya provides specific feedback for each (already provided during goal completion approvals, but can add more context here)

Section 6: Accomplishments Recognition

Recognition for Additional Contributions: [Textarea]
Priya writes:
Continue8:03 AM
Technical Debt Initiative deserves special recognition. This wasn't part of formal goals but had major impact. Shows ownership and initiative - key leadership qualities.

API Standards contribution demonstrates thought leadership and team citizenship.

Section 7: Performance Improvement Plan (if needed)

Required if rating is 1 or 2
Optional for rating 3+
Priya skips (not needed for Exceeds rating)

Section 8: Development Support & Resources

Support to Provide: [Textarea]
Priya commits:

Monthly architecture design review sessions
Opportunity to present API optimization learnings at next team tech talk
Access to advanced distributed systems course (Udemy or Coursera)
Shadow senior architect in Q2 for 2-3 design discussions
Continued support for mentoring work



Section 9: Q2 Goals Discussion

Initial thoughts on Q2 goals: [Textarea]
Priya writes:

Distributed caching strategy goal aligns well with demonstrated expertise
Tech talk presentation excellent for developing public speaking
80% test coverage ambitious but achievable
System design course - approved, will arrange access
Suggest additional goal: Lead design of one new feature end-to-end



Section 10: Manager Recommendations

Recommended for: [Checkboxes]

☑ Merit increase (based on performance)
☑ Bonus eligibility
☐ Promotion consideration
☑ High potential talent pool
☑ Advanced training opportunities
☐ Performance improvement plan


Additional Recommendations: [Textarea]
Priya adds: "Strong candidate for Senior Engineer promotion in 6-9 months if trajectory continues. Recommend increased architectural responsibilities in Q2 to build experience for promotion case."

Section 11: Review Discussion Planning

Preferred Discussion Format:

⦿ In-person meeting
⚪ Video call
⚪ Hybrid


Estimated Discussion Time: 45 minutes
Key Topics for Discussion: [Textarea]

Priya lists:

Celebrate Q1 achievements
Discuss career growth path
Align on Q2 goals
Architecture opportunities
Compensation discussion




Proposed Meeting Dates: [Date suggestions]

Option 1: March 27, 2:00 PM
Option 2: March 28, 10:00 AM
Option 3: March 28, 3:00 PM






Priya reviews entire assessment
Checks completion checklist:

☑ All evidence verified (6/6)
☑ Competencies rated (12/12)
☑ Overall rating provided with justification
☑ Strengths and development areas documented
☑ Recommendations made
☑ Discussion meeting planned


Clicks "Save Draft" (can return later if needed)
Audit log created:

Action: "SAVE_MANAGER_REVIEW_DRAFT"



Step 6: Manager Submits Performance Review

After final review, Priya clicks "Submit Manager Review"
Confirmation Dialog:

"Submit Performance Review for Rahul Sharma?"
"This will:"

Finalize your performance assessment
Notify the employee to schedule review discussion
Lock certain fields (can be reopened if needed)
Forward to admin for approval if required


Summary shown:

Manager Rating: 4/5 - Exceeds Expectations
Variance from Self-Rating: 0 (Agreement)
Goals Reviewed: 3
Evidence Verified: 6/6
Competencies Rated: 12/12
Recommendations: Merit increase, Bonus, High potential


[Cancel] [Submit Review]


Priya clicks "Submit Review"
PUT request to /api/performance-reviews/3001/manager-review
Backend processing:

Updates PerformanceReview record:

ManagerFeedback: [Complete JSON of all manager responses]
ManagerRating: 4
ManagerSubmissionDate: 2026-03-22
Status: "ManagerReviewComplete"
CompetencyManagerRatings: [JSON object]
Strengths: [Text]
DevelopmentAreas: [Text]
ManagerRecommendations: [JSON]
ReviewDiscussionScheduled: false


Creates multiple Feedback records:

One for overall review
One for each major section
All linked to ReviewID 3001


Creates comprehensive audit log:

Action: "SUBMIT_MANAGER_REVIEW"
UserID: 502 (Priya)
EntityType: "PerformanceReview"
EntityID: 3001
Details: "Submitted manager review for Rahul Sharma - Q1 2026. Manager rating: 4/5, matches employee self-rating. All evidence verified. Recommended for merit increase and bonus."
AdditionalData: JSON with full review statistics
Timestamp: Current timestamp
WordCount: 456 (manager feedback)


Creates notification for Rahul:

Type: "ManagerReviewComplete"
Message: "Your manager has completed your Q1 2026 performance review. Manager rating: 4/5 - Exceeds Expectations. Please schedule a review discussion meeting."
Priority: "High"
ActionRequired: true
ActionURL: "/performance-reviews/3001"


Creates notification for admin (if admin approval required):

Type: "ReviewReadyForApproval"
Message: "Manager review completed for Rahul Sharma (Engineering). Awaiting final admin approval."


Updates ReviewCycle statistics:

Increments completed manager reviews counter
Updates completion percentage




Success message: "Performance review submitted successfully! Rahul will be notified to schedule a discussion."
Manager dashboard updates:

Performance Reviews Pending: 1 → 0
Reviews Completed This Cycle: +1


Automated follow-up:

System creates calendar invitation placeholder
Sends to both Rahul and Priya with suggested meeting times




PHASE 10: Review Discussion & Finalization
Step 1: Employee Receives Manager Review Notification

Rahul logs in on March 23
Audit log created
Sees notification: "Your manager has completed your Q1 2026 performance review..."
Dashboard shows Review status: Manager Review ✓ | Discussion ⏳
Clicks notification
Audit log created
Redirected to Performance Review Results page

Step 2: Employee Reviews Manager Feedback

Performance Review Results Page loads:
Header:

"Q1 2026 Performance Review Results"
Your Self-Rating: 4/5 - Exceeds Expectations
Manager Rating: 4/5 - Exceeds Expectations
Rating Alignment: ✓ Aligned (Green indicator)

Summary Dashboard:

Visual comparison chart showing self vs manager ratings
Goal achievement statistics
Competency ratings comparison
Evidence verification status: All Verified ✓

Content Tabs:
Tab 1: Manager's Overall Assessment

Shows Priya's complete performance assessment
Rating justification
Strengths highlighted
Development areas listed
Recommendations shown (where appropriate)

Tab 2: Side-by-Side Comparison

Left column: Your self-assessment
Right column: Manager's assessment
Highlights agreements and differences
Competency rating comparison grid

Tab 3: Goals & Evidence Feedback

For each goal, shows manager's comments
Evidence verification status and notes
Goal-specific ratings

Tab 4: Development Plan

Manager's suggested development areas
Proposed support and resources
Q2 goal suggestions from manager

Action Section:

"Schedule Review Discussion Meeting"
Shows manager's proposed meeting times:

⦿ March 27, 2:00 PM
⚪ March 28, 10:00 AM
⚪ March 28, 3:00 PM
⚪ Suggest different time


[Button: Confirm Meeting Time]
Note: "Discussion meetings typically last 45 minutes"


Rahul reads through all manager feedback carefully
Feels validated seeing rating alignment
Appreciates detailed, constructive feedback
Reviews development suggestions
Excited about Q2 opportunities mentioned

Step 3: Employee Schedules Review Discussion

Rahul selects: March 27, 2:00 PM
Adds optional note: "Thank you for the detailed feedback. Looking forward to discussing Q2 goals and career growth."
Clicks "Confirm Meeting Time"
POST request to /api/performance-reviews/3001/schedule-meeting
Backend processing:

Updates PerformanceReview record:

ReviewDiscussionScheduled: true
ReviewDiscussionDate: 2026-03-27 14:00
EmployeeNotes: [Rahul's note]


Creates audit log:

Action: "SCHEDULE_REVIEW_DISCUSSION"


Creates notification for Priya:

Type: "ReviewMeetingScheduled"
Message: "Rahul Sharma has confirmed review discussion for March 27 at 2:00 PM"


Creates calendar event (if calendar integration available):

Title: "Q1 2026 Performance Review Discussion - Rahul Sharma"
Attendees: Rahul, Priya
Duration: 45 minutes
Description: Includes review summary and discussion topics


Or creates in-app meeting reminder for both parties


Confirmation shown: "Meeting scheduled for March 27, 2:00 PM. Calendar invitation sent."
Dashboard updates: Discussion ✓ (scheduled)

Step 4: Review Discussion Meeting Occurs

March 27, 2:00 PM - Rahul and Priya meet
Discussion agenda (not in system, but tracked after):

Celebrate achievements
Discuss performance rating
Review evidence and feedback
Align on development areas
Plan Q2 goals
Discuss career path
Address any questions


Meeting lasts 50 minutes (longer than planned due to productive discussion)
Key outcomes (documented after meeting):

Rahul agrees with performance assessment
Excited about architecture opportunities
Will present tech talk in April
Q2 goals aligned
Discussed potential promotion timeline



Step 5: Manager Finalizes Review Post-Discussion

After meeting, Priya logs into system
Audit log created
Goes to Rahul's performance review
Clicks "Finalize Review" button
Post-Discussion Form appears:
Meeting Completed:

Meeting Date: March 27, 2026 (pre-filled)
Actual Duration: 50 minutes
Meeting Format: In-person
Employee Attendance: ✓ Attended

Discussion Summary:

Key Points Discussed: [Textarea]

Priya documents: "Reviewed Q1 achievements in detail. Employee expressed pride in API optimization work. Discussed technical debt initiative impact. Aligned on development areas. Planned Q2 goals together. Discussed senior engineer promotion path and timeline."


Employee Questions/Concerns: [Textarea]

"Asked about promotion timeline and criteria. Clarified expectations for senior engineer level. Requested specific architecture projects to build experience."


Manager Commitments: [Textarea]

"Committed to monthly architecture sessions, tech talk opportunity in April, distributed systems course access, and quarterly career development discussions."



Agreement & Acknowledgment:

Employee Agrees with Assessment: ⦿ Yes ⚪ No ⚪ Partially
If No/Partially, explain: [Textarea - N/A for Rahul]
Employee Signed Off: ✓ Yes (verbally during meeting)

Final Performance Rating:

Confirmed Rating: 4/5 - Exceeds Expectations
Any rating changes after discussion?: No

Action Items:

[Button: + Add Action Item]
Action Item #1:

Task: "Schedule first architecture design session"
Owner: Priya
Due Date: April 5, 2026


Action Item #2:

Task: "Prepare tech talk outline on API optimization"
Owner: Rahul
Due Date: April 10, 2026


Action Item #3:

Task: "Enroll in distributed systems course"
Owner: Rahul
Due Date: April 1, 2026




Clicks "Mark Review as Finalized"
Final Confirmation:

"Finalize Performance Review?"
"This will:"

Lock the review (admin can still modify)
Trigger compensation review workflow
Update employee performance history
Close this review cycle for this employee


[Cancel] [Finalize]


Priya confirms
PUT request to /api/performance-reviews/3001/finalize
Backend processing:

Updates PerformanceReview record:

Status: "ManagerReviewComplete" → "Finalized"
FinalRating: 4
DiscussionCompleted: true
DiscussionDate: 2026-03-27
DiscussionSummary: [Text]
EmployeeAgreement: "Yes"
FinalizedBy: 502
FinalizedDate: 2026-03-27


Creates action items in ActionItems table
Creates comprehensive audit log:

Action: "FINALIZE_PERFORMANCE_REVIEW"
Details: "Finalized Q1 2026 performance review for Rahul Sharma. Final rating: 4/5. Employee agreed with assessment. 3 action items created."
EntityType: "PerformanceReview"
EntityID: 3001
AdditionalData: Complete review summary


Updates employee's EmployeePerformanceHistory table:

Adds Q1 2026 review record
Updates performance trend data
Updates average rating


Triggers compensation review workflow (if enabled):

Creates CompensationReview record
Notifies HR/Admin


Creates confirmation notification for Rahul:

Type: "ReviewFinalized"
Message: "Your Q1 2026 performance review has been finalized. Final rating: 4/5 - Exceeds Expectations. Review discussion notes and action items are available."


Creates notification for admin:

Type: "ReviewReadyForFinalApproval"
Message: "Finalized review ready for admin approval: Rahul Sharma - Q1 2026"




Success message: "Performance review finalized successfully!"
Review status changes to "Finalized" with green checkmark
Dashboard shows: Self-Assessment ✓ | Manager Review ✓ | Discussion ✓ | Finalized ✓


PHASE 11: Admin Reviews and Generates Reports
Step 1: Admin Views Pending Approvals

Admin logs in on March 28
Audit log created
Dashboard shows:

Reviews Awaiting Final Approval: 25
Reviews Completed: 125/150
Completion Rate: 83%


Admin clicks "Reviews Awaiting Approval"
List page shows reviews pending admin sign-off:

Rahul Sharma - Engineering - 4/5
[Other employees...]


Clicks on Rahul's review

Step 2: Admin Reviews Performance Review

Admin Review Interface loads:

Shows complete review: self-assessment, manager review, discussion notes
Evidence verification status
Recommendations
Rating justification


Admin reviews for:

Consistency with company standards
Completeness
Fair ratings
Proper documentation


Everything looks good
Admin clicks "Approve Review"
Optional admin notes: "Excellent comprehensive review. Clear evidence of exceeding expectations. Approve recommendations."
Confirms approval
Backend processing:

Updates PerformanceReview:

AdminApproved: true
ApprovedByAdmin: Admin's UserID
AdminApprovalDate: 2026-03-28
FinalStatus: "Approved"


Creates audit log:

Action: "ADMIN_APPROVE_REVIEW"
Details: "Admin approved final performance review for Rahul Sharma Q1 2026"


Locks review from further edits (unless admin unlocks)


Review fully complete

Step 3: Admin Views Audit Logs for Review Cycle

Admin wants to review all activity for Q1 cycle
Clicks "View Audit Logs" from admin menu
Audit Log Interface (enhanced view for admin):
Filter Panel:

Date Range: Q1 2026 (Jan 1 - Mar 31)
User: All Users
Action Type: All
Entity Type: All
Review Cycle: Q1 2026 Performance Review
Department: All
Risk Level: All (Normal, Elevated, High)
Status: All (Success, Failed, Suspicious)
[Button: Apply Filters] [Button: Reset] [Button: Save Filter Preset]


Applies filter for Q1 review cycle
Audit Log Results display with advanced features:
Summary Statistics:

Total Actions Logged: 4,523
Unique Users: 150
Action Types: 47 different actions
Failed Actions: 12 (review to ensure not suspicious)
Suspicious Activity: 0

Detailed Audit Trail Table:
TimestampUserDepartmentActionEntityEntity IDIP AddressStatusDetails2026-03-28 09:15:33Admin UserAdminADMIN_APPROVE_REVIEWPerformanceReview300110.0.1.5SuccessAdmin approved...2026-03-27 14:50:22Priya PatelEngineeringFINALIZE_PERFORMANCE_REVIEWPerformanceReview300110.0.2.15SuccessFinalized Q1...2026-03-27 14:05:10Rahul SharmaEngineeringSCHEDULE_REVIEW_DISCUSSIONPerformanceReview300110.0.3.42SuccessScheduled meeting...2026-03-22 16:30:45Priya PatelEngineeringSUBMIT_MANAGER_REVIEWPerformanceReview300110.0.2.15SuccessSubmitted manager...2026-03-21 11:20:15Rahul SharmaEngineeringSUBMIT_SELF_ASSESSMENTPerformanceReview300110.0.3.42SuccessSubmitted self...2026-03-06 11:15:22Priya PatelEngineeringAPPROVE_GOAL_COMPLETIONGoal200110.0.2.15SuccessApproved goal...2026-03-05 10:45:33Rahul SharmaEngineeringSUBMIT_GOAL_COMPLETIONGoal200110.0.3.42SuccessSubmitted completion..............................
Advanced Features:

Click any row to see full JSON details
Export button: Download filtered results as CSV/PDF
Timeline view: Visual timeline of all actions
User activity heatmap: See when users are most active
Anomaly detection: System highlights unusual patterns

Drill-Down Options:

Click on a user to see all their actions
Click on an entity to see its complete history
Click on an action type to see all instances
Click on IP address to see all actions from that IP


Admin clicks on Rahul's user name
Sees Complete User Activity Log:

All actions by Rahul during Q1
Login history
Goal creation, updates, completion submissions
Self-assessment submission
Meeting scheduling
Every interaction logged


Admin reviews specific action by clicking row
Detailed Action View Modal:

   Audit Log Entry #15423
   
   Timestamp: 2026-03-05 10:45:33.127 UTC
   User: Rahul Sharma (UserID: 501)
   Department: Engineering
   Role: Employee
   
   Action: SUBMIT_GOAL_COMPLETION
   Entity Type: Goal
   Entity ID: 2001
   Entity Title: "Reduce API response time by 30%"
   
   IP Address: 10.0.3.42
   User Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)...
   Session ID: sess_abc123xyz789
   Location: Pune, Maharashtra, IN
   
   Status: Success
   
   Details: "Submitted goal completion with 4 evidence items for manager approval"
   
   Before State:
   {
     "status": "InProgress",
     "progress": 70,
     "completionSubmitted": false
   }
   
   After State:
   {
     "status": "CompletedPendingApproval",
     "progress": 100,
     "completionSubmitted": true,
     "evidenceCount": 4
   }
   
   Additional Data:
   {
     "evidenceTypes": ["Metrics/Reports", "Code Repository Links", "Documentation"],
     "completionDate": "2026-03-05",
     "achievementLevel": "Exceeded"
   }
   
   Related Audit Logs:
   - Evidence verification (4 entries) - March 6
   - Manager approval - March 6
   
   [Button: View Related Logs] [Button: Export Entry] [Button: Close]

Admin can trace complete history of any entity
Can see who did what, when, from where, and why
Full accountability and transparency

Step 4: Admin Generates Comprehensive Reports

Admin clicks "Analytics & Reporting" from menu
Enhanced Analytics Dashboard loads:
Overview Widgets:

Q1 Review Cycle Status:

Total Employees: 150
Self-Assessments Submitted: 148 (99%)
Manager Reviews Completed: 145 (97%)
Reviews Finalized: 126 (84%)
Admin Approved: 25 (17% - in progress)


Goal Statistics:

Total Goals Set: 425
Goals Completed: 187 (44%)
Goals In Progress: 215 (51%)
Goals Pending Approval: 23 (5%)
Average Completion Rate: 44%


Rating Distribution:

Pie chart showing:

5 (Outstanding): 18%
4 (Exceeds): 42%
3 (Meets): 35%
2 (Below): 4%
1 (Needs Improvement): 1%




Evidence Metrics:

Total Evidence Items Submitted: 892
Evidence Verified: 890 (99.8%)
Average Evidence per Goal Completion: 4.2
Most Common Evidence Type: Metrics/Reports (45%)



Interactive Visualizations:

Performance trend line chart over time
Department comparison bar chart
Goal completion funnel
Evidence type distribution
Rating calibration scatter plot


Admin clicks "Generate Custom Report"
Report Builder Interface:
Step 1: Select Report Type

⦿ Performance Review Summary
⚪ Goal Achievement Analysis
⚪ Competency Assessment Report
⚪ Evidence Audit Report
⚪ Department Comparison
⚪ Custom Query Report

Step 2: Select Scope

Review Cycle: Q1 2026 Performance Review
Department: [Multi-select dropdown]

Admin selects: Engineering


Include: [Checkboxes]

☑ Individual performance summaries
☑ Goal achievement details
☑ Evidence verification status
☑ Rating distribution
☑ Competency analysis
☑ Recommendations summary
☑ Audit trail summary
☑ Comparative analytics



Step 3: Data Fields

Available Fields: [Drag & drop interface]

Employee Name, Department, Manager, Self-Rating, Manager Rating, Final Rating, Goals Count, Goals Completed, Evidence Count, Strengths, Development Areas, Recommendations, etc.


Selected Fields: [Reorderable list]

Admin customizes which fields to include



Step 4: Filters & Criteria

Performance Rating: All
Goal Completion Rate: All
Show Only: [Checkboxes]

☐ Top Performers
☐ High Potential
☑ Recommended for Promotion
☐ Performance Concerns



Step 5: Format & Output

Format:

⦿ PDF (Detailed Report)
⚪ Excel (Data Export)
⚪ PowerPoint (Executive Summary)


Include Charts: ☑ Yes
Include Audit Summary: ☑ Yes
Confidentiality Level: Confidential - Manager+ Only


Admin clicks "Generate Report"
POST request to /api/reports/generate with all parameters
Backend processing:

Queries PerformanceReview table filtered by:

CycleID: 101
Department: Engineering


Joins with User, Goal, GoalEvidence, Feedback tables
Calculates aggregate metrics:

Average ratings by department
Goal completion statistics
Evidence verification rates
Rating distribution
Competency averages
Recommendation summaries


Queries AuditLog table for summary statistics
Generates visualizations (charts, graphs)
Compiles into PDF using reporting library
Creates Report record in database:

ReportID: 5001
ReportType: "Performance Review Summary"
Scope: "Department: Engineering"
ReviewCycleID: 101
GeneratedBy: Admin UserID
GeneratedDate: 2026-03-28
Parameters: [JSON of all report settings]
Metrics: [JSON of calculated metrics]
FileURL: "https://storage.company.com/reports/q1-2026-engineering.pdf"
AccessLevel: "Confidential"


Creates audit log:

Action: "GENERATE_REPORT"
Details: "Generated Q1 2026 Performance Review Summary for Engineering department"
EntityType: "Report"
EntityID: 5001




Report generation progress bar shows
After 15 seconds, report ready
PDF Report Generated with sections:
Executive Summary:

Department: Engineering
Review Period: Q1 2026
Total Employees: 45
Reviews Completed: 44/45 (98%)
Average Performance Rating: 3.9/5

Performance Distribution:

Outstanding (5): 8 employees (18%)
Exceeds (4): 19 employees (43%)
Meets (3): 15 employees (34%)
Below (2): 2 employees (5%)
Needs Improvement (1): 0 employees (0%)
Chart/graph visualization

Goal Achievement:

Total Goals: 125
Completed: 58 (46%)
In Progress: 63 (50%)
Not Started: 4 (3%)
Average Goals per Employee: 2.8
Average Completion Rate: 46%

Evidence Metrics:

Total Evidence Items: 287
Verified Evidence: 285 (99.3%)
Average Evidence per Completed Goal: 4.1
Evidence Type Distribution: [Chart]

Top Performers:

List of employees rated 4-5
Key achievements highlighted
Includes Rahul Sharma with summary

Promotion Recommendations:

Number of employees recommended: 5
Names and justifications (if appropriate access level)

Development Needs:

Common development areas across department
Training recommendations

Competency Analysis:

Average competency ratings by skill
Strengths and gaps identified

Audit Summary:

Total audit log entries for review cycle: 1,247 (Engineering only)
All reviews properly documented: Yes
Evidence verification compliance: 99.3%
Timeline compliance: 98%

Appendix:

Individual employee summaries (if included)
Detailed methodology
Data sources


Admin clicks "Download Report"
PDF downloads to admin's computer
Admin can also click "Share Report" to send to executives
Audit log created for report download:

Action: "DOWNLOAD_REPORT"
EntityID: 5001




ADDITIONAL WORKFLOWS
Workflow: Employee Uploads Evidence to Drive and Submits Link
When employee needs to submit evidence that isn't a simple link:

Employee has screenshots/documents for evidence
Opens Google Drive (external to system)
Creates/navigates to folder structure:

"PerformanceTrack Evidence"

"Q1 2026"

"API Performance Goal"

Creates subfolder for organizing








Uploads files:

"Performance-Dashboard-Before.png"
"Performance-Dashboard-After.png"
"Technical-Documentation.pdf"


Right-clicks on file/folder
Selects "Get shareable link"
Ensures link sharing is set to:

"Anyone with the link can view" (for manager access)
OR "Restricted - Only people added can access" (then adds manager's email)


Copies the shareable link
Returns to PerformanceTrack
In evidence submission form:

Evidence Type: Documentation
Title: "Performance Dashboard Screenshots and Technical Doc"
Link/URL: [Pastes Google Drive link]
Description: "Folder contains before/after screenshots and comprehensive technical documentation PDF"
Verification Instructions: "Review all 3 files in folder"


Submits evidence
Manager later clicks link, which opens Google Drive in new tab
Manager reviews all files
Returns to PerformanceTrack and verifies evidence

Important Notes on Evidence Links:

System only stores the URL/link, not the actual files
Files remain in employee's Google Drive
Employee responsible for maintaining access
If link breaks/access revoked, evidence becomes unverifiable
System validates link format but not content accessibility
Manager can report broken links, triggering notification to employee


Workflow: Manager Denial of Goal Completion with Message
Alternative to approval - manager denies completion:

Manager reviews goal completion submission
Reviews all evidence
Finds evidence insufficient or goal not actually met
In Manager's Completion Decision Panel:

Selects: ⦿ Reject Completion


Rejection Form appears:

Primary Reason: [Dropdown]

Manager selects: "Evidence does not support claims"


Detailed Explanation: [Required]

Manager writes: "The performance dashboard shows only




Continue8:06 AM15% improvement, not the 30% target specified. While progress has been made, the goal's success criteria state 'reduce from 300ms to 210ms or below.' Current metrics show 255ms average, which is a 15% improvement. This is good progress but does not meet the defined goal."

Specific Evidence Issues: [Checkboxes of evidence items]

Checks: Evidence Item #1 - Performance Report
Notes: "Report clearly shows 255ms current average, not 210ms"


Next Steps: [Textarea]

"Continue optimization work. Additional 15% improvement needed. Suggest:

Expand caching to additional endpoints
Review and optimize remaining database queries
Consider CDN for static assets
Monitor for sustained improvement over 2 weeks
Resubmit when 210ms target achieved"




Allow Resubmission: ⦿ Yes
New Target Date: April 15, 2026
Manager Support Offered: "Available for architecture review session to identify optimization opportunities"


Manager clicks "Reject Completion"
Confirmation dialog with warning
Manager confirms
Backend updates:

GoalCompletion status: "Rejected"
Goal status: back to "InProgress"
Progress: 70% (resets from 100%)
Creates detailed rejection record


Audit log:

Action: "REJECT_GOAL_COMPLETION"
Severity: "Medium"
Details: Full rejection reasoning


Employee notification:

Type: "GoalCompletionRejected"
Message: Constructive, supportive tone
Includes manager's feedback and next steps


Employee can view detailed feedback
Can continue working on goal
Can resubmit when criteria actually met
All subsequent resubmission creates new audit logs


This completes the comprehensive, enhanced workflow for the PerformanceTrack system with:

Full audit trail at every step
Manager approval for goal completions
Evidence submission with links to external storage
Detailed evidence verification process
Complete performance review cycle
Admin oversight and reporting
Accountability and transparency throughout

Every significant action is logged, every decision is tracked, and every piece of evidence is verified - creating a robust, audit-ready performance management system.
