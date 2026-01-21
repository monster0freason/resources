# PerformanceTrack System - Revised Complete Workflow Walkthrough

Let me walk you through how this enhanced system would work in extreme detail, from the moment someone opens the website to completing a full performance review cycle with all corrections.

---

## **PHASE 1: Initial Setup (Admin's Job)**

### Step 1: Admin Logs In
1. Admin opens browser and navigates to `https://performancetrack.company.com`
2. Landing page shows a login form with fields: Email and Password
3. Admin enters credentials: `admin@company.com` and password
4. System sends POST request to backend API: `/api/auth/login`
5. Backend validates credentials against User table in database
6. Database returns User record with Role = "Admin"
7. Backend generates JWT token (authentication token)
8. Backend creates audit log entry:
   - AuditID: 9001 (auto-generated)
   - UserID: 500
   - Action: "Login"
   - Details: "Admin logged in successfully"
   - IPAddress: "192.168.1.100"
   - Timestamp: "2026-01-10 09:00:00"
9. Frontend receives token and stores it in browser memory
10. User is redirected to Admin Dashboard

### Step 2: Admin Dashboard View
1. Admin sees Admin Dashboard with enhanced menu options:
   - Manage Users
   - Configure Review Cycles
   - Generate Reports
   - **Audit Logs** (NEW)
   - System Settings
2. Dashboard displays quick stats:
   - Total Active Users: 150
   - Active Review Cycles: 1
   - Pending Approvals: 25
   - System Health: All systems operational

### Step 3: Admin Views Audit Logs (NEW FEATURE)
1. Admin clicks "Audit Logs" from menu
2. System sends GET request to `/api/audit-logs`
3. Backend retrieves recent audit entries from AuditLog table
4. Audit Logs page loads showing filterable table with columns:
   - Timestamp
   - User Name
   - User Role
   - Action Type
   - Details
   - IP Address
   - Status
5. Admin sees recent entries:
   | Timestamp | User | Role | Action | Details | IP Address | Status |
   |-----------|------|------|--------|---------|------------|--------|
   | 2026-01-10 09:00:00 | Admin User | Admin | Login | Admin logged in | 192.168.1.100 | Success |
   | 2026-01-09 16:45:00 | Priya Patel | Manager | GoalApproval | Approved goal ID 2005 | 192.168.1.105 | Success |
   | 2026-01-09 14:30:00 | Rahul Sharma | Employee | GoalSubmit | Submitted goal for completion | 192.168.1.110 | Success |

6. Admin can use filters:
   - Date Range: From [date picker] To [date picker]
   - User: [dropdown with all users]
   - Action Type: [dropdown: Login, Logout, GoalCreate, GoalApproval, ReviewSubmit, etc.]
   - Status: [dropdown: Success, Failed, Pending]
7. Admin selects date range: Jan 1 - Jan 10, 2026
8. Clicks "Apply Filters"
9. System sends GET request: `/api/audit-logs?startDate=2026-01-01&endDate=2026-01-10`
10. Filtered results display showing all activity in that period
11. Admin can click "Export" button to download audit logs as CSV or PDF
12. Export dialog appears with options:
    - Format: CSV / PDF / Excel
    - Include: All Columns / Selected Columns
13. Admin selects PDF and clicks "Download"
14. System generates PDF report with audit trail
15. PDF downloads showing comprehensive audit information

### Step 4: Admin Creates Review Cycle
1. Admin clicks "Configure Review Cycles"
2. System shows list of existing review cycles
3. Admin clicks "+ Create New Review Cycle" button
4. A modal/form appears with fields:
   - Title: "Q1 2026 Performance Review"
   - Start Date: January 1, 2026
   - End Date: March 31, 2026
   - Status: Active (dropdown)
   - **Goal Completion Requires Approval**: Yes (checkbox - NEW)
   - **Evidence Required for Goals**: Required (checkbox - NEW)
5. Admin fills form:
   - Title: "Q1 2026 Performance Review"
   - Start Date: January 1, 2026
   - End Date: March 31, 2026
   - Status: Active
   - Goal Completion Requires Approval: ✓ (checked)
   - Evidence Required for Goals: ✓ (checked)
6. Admin clicks "Save"
7. Frontend validates all fields are filled
8. System sends POST request to `/api/review-cycles` with data:
   ```json
   {
     "title": "Q1 2026 Performance Review",
     "startDate": "2026-01-01",
     "endDate": "2026-03-31",
     "status": "Active",
     "requiresCompletionApproval": true,
     "evidenceRequired": true
   }
   ```
9. Backend receives request, validates data
10. Backend inserts new record into ReviewCycle table:
    - CycleID: 101 (auto-generated)
    - Title: "Q1 2026 Performance Review"
    - StartDate: 2026-01-01
    - EndDate: 2026-03-31
    - Status: "Active"
    - RequiresCompletionApproval: true (NEW)
    - EvidenceRequired: true (NEW)
11. Backend creates audit log entry:
    - Action: "ReviewCycleCreated"
    - Details: "Created review cycle 'Q1 2026 Performance Review'"
    - UserID: 500
12. Database confirms successful insertion
13. Backend returns success response with new CycleID
14. Frontend shows success message: "Review cycle created successfully!"
15. Review cycle list refreshes automatically showing new cycle

### Step 5: Admin Creates User Accounts
1. Admin clicks "Manage Users" from menu
2. System displays user list (currently empty or has existing users)
3. Admin clicks "+ Add New User"
4. Form appears with fields:
   - Name: "Rahul Sharma"
   - Email: "rahul.sharma@company.com"
   - **Password: [password input field]** (NEW - Admin sets password)
   - **Confirm Password: [password input field]** (NEW)
   - Role: Employee (dropdown: Employee/Manager/Admin)
   - Department: "Engineering"
   - Manager: [dropdown showing all managers in department]
   - Status: Active
5. Admin fills form:
   - Name: "Rahul Sharma"
   - Email: "rahul.sharma@company.com"
   - **Password: "Welcome@2026"** (Admin creates initial password)
   - **Confirm Password: "Welcome@2026"**
   - Role: Employee
   - Department: "Engineering"
   - Manager: "Priya Patel" (selected from dropdown)
   - Status: Active
6. Admin clicks "Create User"
7. Frontend validates:
   - All required fields are filled
   - Password and Confirm Password match
   - Password meets minimum requirements (8+ characters)
   - Email is unique in system
8. POST request sent to `/api/users`
9. Backend creates user record in database:
   - UserID: 501 (auto-generated)
   - Name: Rahul Sharma
   - Role: Employee
   - Email: rahul.sharma@company.com
   - PasswordHash: [hashed version of "Welcome@2026"]
   - Department: Engineering
   - ManagerID: 502
   - Status: Active
   - CreatedDate: 2026-01-10 09:30:00
10. System creates notification record in Notification table:
    - NotificationID: 1001
    - UserID: 501
    - Type: "AccountCreated"
    - Message: "Your account has been created. You can now log in using your email and the password provided to you separately."
    - Status: "Unread"
    - CreatedDate: "2026-01-10 09:30:00"
11. Backend creates audit log entry:
    - Action: "UserCreated"
    - Details: "Created user account for Rahul Sharma (Employee)"
    - UserID: 500
12. Success message shown to admin: "User created successfully! Username: rahul.sharma@company.com"
13. User list refreshes showing new user
14. **Note**: Admin provides login credentials (email and password) to Rahul through external means (email, phone, in-person, etc.) - this is outside the system
15. Admin repeats this process for managers, creating user "Priya Patel" with Role = "Manager"

---

## **PHASE 2: Employee Sets Goals (Simplified - No Evidence at Creation)**

### Step 1: Employee First Login
1. Rahul receives login credentials from admin through external communication channel (not part of this system)
2. Opens `https://performancetrack.company.com`
3. Enters email: "rahul.sharma@company.com" and password: "Welcome@2026" (provided by admin)
4. System sends POST request to `/api/auth/login`
5. Backend validates credentials against User table
6. Backend creates audit log entry:
   - Action: "Login"
   - Details: "Employee Rahul Sharma logged in"
   - UserID: 501
7. JWT token generated with role = "Employee"
8. Rahul is redirected to **Employee Dashboard**

### Step 2: Employee Dashboard View
1. Dashboard loads with sections:
   - **My Goals** (currently empty)
   - **My Reviews** (shows active review cycles)
   - **Notifications** (shows red badge with "1")
   - **Performance History**
2. Frontend makes GET request to `/api/notifications?userId=501&status=unread`
3. Backend queries Notification table
4. Returns notification about account creation
5. Notification panel shows: "Your account has been created. You can now log in..."
6. Rahul clicks notification to mark as read
7. Notification detail modal appears showing full message
8. Rahul clicks "Mark as Read"
9. PUT request sent to `/api/notifications/1001` with `{status: "Read"}`
10. Backend updates Notification table
11. Backend creates audit log entry for notification read action
12. Notification badge count decreases from 1 to 0

### Step 3: Creating a Goal (Simplified - No Evidence Required at Creation)
1. Rahul clicks "My Goals" tab
2. System displays goals dashboard with tabs:
   - All Goals (0)
   - Pending Approval (0)
   - In Progress (0)
   - Pending Completion Approval (0) (NEW TAB)
   - Completed (0)
3. Sees empty state: "No goals set yet. Start by creating your first goal!"
4. Clicks "+ Add New Goal" button
5. **Simplified goal creation form appears:**
   
   **Goal Creation Form:**
   
   **Section 1: Goal Details**
   - Title: [text input]
   - Description: [rich text area with formatting options]
   - Category: [dropdown: Technical/Behavioral/Professional Development/Other]
   - Priority: [dropdown: High/Medium/Low]
   
   **Section 2: Timeline**
   - Start Date: [date picker]
   - End Date: [date picker]
   
   **Section 3: Manager Assignment**
   - Assigned Manager: Priya Patel (auto-filled from user profile, read-only)
   
   - Status: Pending (auto-set, not editable)
   
   **Note**: Evidence will be required when you mark this goal as complete.

6. Rahul fills in the form:
   - Title: "Reduce API response time by 30%"
   - Description: "Optimize database queries and implement caching to improve API performance from average 300ms to 210ms. This includes Redis cache implementation, query optimization, and connection pooling improvements."
   - Category: Technical
   - Priority: High
   - Start Date: January 15, 2026
   - End Date: March 15, 2026
   - Assigned Manager: Priya Patel

7. Rahul clicks "Save Goal"

8. Frontend validation checks:
   - All required fields are filled
   - End date is after start date
   - Dates are within current review cycle (Jan 1 - Mar 31)
   - Description has minimum 50 characters

9. Validation passes, POST request sent to `/api/goals`:
   ```json
   {
     "title": "Reduce API response time by 30%",
     "description": "Optimize database queries and implement...",
     "category": "Technical",
     "priority": "High",
     "assignedToUserId": 501,
     "assignedManagerId": 502,
     "startDate": "2026-01-15",
     "endDate": "2026-03-15",
     "status": "Pending"
   }
   ```

10. Backend receives request
11. Validates user authentication and authorization
12. Validates user is creating goal for themselves
13. Inserts into Goal table:
    - GoalID: 2001 (auto-generated)
    - Title: "Reduce API response time by 30%"
    - Description: "Optimize database queries and implement..."
    - Category: "Technical"
    - Priority: "High"
    - AssignedToUserID: 501
    - AssignedManagerID: 502
    - StartDate: 2026-01-15
    - EndDate: 2026-03-15
    - Status: "Pending"
    - EvidenceLink: null (will be added later when goal is completed)
    - CompletionApprovalStatus: null (will be used later)
    - CreatedDate: 2026-01-15 10:00:00

14. Backend creates notification for manager (Priya):
    - Gets manager from AssignedManagerID field (502)
    - Inserts notification:
      - NotificationID: 1002
      - UserID: 502
      - Type: "GoalSubmitted"
      - Message: "Rahul Sharma has submitted a new goal 'Reduce API response time by 30%' for approval"
      - RelatedEntityType: "Goal"
      - RelatedEntityID: 2001
      - Status: "Unread"
      - Priority: "High" (matches goal priority)
      - CreatedDate: 2026-01-15 10:00:00

15. Backend creates audit log entry:
    - AuditID: 9010
    - UserID: 501
    - Action: "GoalCreated"
    - Details: "Created goal 'Reduce API response time by 30%' (ID: 2001)"
    - RelatedEntityType: "Goal"
    - RelatedEntityID: 2001
    - Timestamp: 2026-01-15 10:00:00

16. Backend returns success response with GoalID: 2001
17. Frontend shows success message: "Goal created successfully! Waiting for manager approval."
18. Goal appears in "My Goals" list under "Pending Approval" tab with:
    - Status badge: "Pending" (yellow color)
    - Priority: High (red flag)
    - Timeline: Jan 15 - Mar 15, 2026
    - Progress: Not started

### Step 4: Employee Creates Additional Goals
1. Rahul clicks "+ Add New Goal" again
2. Creates second goal:
   - Title: "Complete React Advanced Certification"
   - Description: "Complete Udemy React Advanced course including all assignments and final project. Focus on hooks, context API, and performance optimization."
   - Category: Professional Development
   - Priority: Medium
   - Start Date: January 15, 2026
   - End Date: March 31, 2026
3. Saves goal
4. System follows same workflow as Goal 1
5. Goal appears with GoalID: 2002, Status: Pending

6. Rahul creates third goal:
   - Title: "Mentor 2 junior developers"
   - Description: "Provide weekly mentoring sessions to Amit Patel and Sneha Kumar on React development and best practices. Track progress and provide feedback."
   - Category: Behavioral
   - Priority: Medium
   - Start Date: January 20, 2026
   - End Date: March 31, 2026
7. Saves goal
8. Goal appears with GoalID: 2003, Status: Pending

9. Rahul's "My Goals" dashboard now shows:
   - Pending Approval tab (3 goals)
   - All showing yellow "Pending" status

---

## **PHASE 3: Manager Reviews and Approves Goals**

### Step 1: Manager Login and Dashboard
1. Priya (manager) opens browser and navigates to login page
2. Enters credentials: priya.patel@company.com and password (provided by admin)
3. System authenticates user
4. Backend creates audit log entry for login
5. JWT token generated with role = "Manager"
6. Redirected to **Manager Dashboard**
7. Dashboard displays sections:
   - **Team Goals** (showing count: 3 pending approvals)
   - **Performance Reviews** (showing count: 0 pending)
   - **Goal Completion Approvals** (showing count: 0 pending) (NEW)
   - **Team Members** (list of 8 direct reports)
   - **Notifications** (red badge showing "3")
   - **Analytics** (team performance metrics)

### Step 2: Manager Checks Notifications
1. Priya sees notification badge with count "3"
2. Clicks notification bell icon in top right
3. Notification dropdown panel slides down showing:
   | Time | Type | Message | Priority |
   |------|------|---------|----------|
   | 5 min ago | GoalSubmitted | Rahul Sharma has submitted "Mentor 2 junior developers" | Medium |
   | 7 min ago | GoalSubmitted | Rahul Sharma has submitted "Complete React Advanced Certification" | Medium |
   | 10 min ago | GoalSubmitted | Rahul Sharma has submitted "Reduce API response time by 30%" | High |

4. Priya clicks on the first (most recent) notification
5. System marks notification as read (PUT request to `/api/notifications/1002`)
6. Backend updates Notification status to "Read"
7. Backend creates audit log entry for notification read
8. System redirects to Team Goals page, automatically filtered for Rahul's goals

### Step 3: Manager Reviews Goals
1. Team Goals page loads with enhanced interface
2. Filter panel at top shows:
   - Employee: [dropdown - currently showing "Rahul Sharma"]
   - Status: [dropdown - currently showing "Pending"]
   - Priority: [dropdown - showing "All"]
   - Date Range: [date pickers]
3. Goals table displays:
   | Employee | Goal Title | Category | Priority | Start Date | End Date | Status | Actions |
   |----------|-----------|----------|----------|------------|----------|--------|---------|
   | Rahul Sharma | Reduce API response time by 30% | Technical | High 🔴 | Jan 15 | Mar 15 | Pending | [View Details] |
   | Rahul Sharma | Complete React Advanced Certification | Prof Dev | Medium 🟡 | Jan 15 | Mar 31 | Pending | [View Details] |
   | Rahul Sharma | Mentor 2 junior developers | Behavioral | Medium 🟡 | Jan 20 | Mar 31 | Pending | [View Details] |

4. Priya clicks "View Details" on the first goal (API performance)
5. Goal details modal opens showing comprehensive information:

   **Goal Details Modal:**
   
   **Header Section:**
   - Goal ID: 2001
   - Status: Pending Approval
   - Created: Jan 15, 2026 10:00 AM
   
   **Employee Information:**
   - Employee: Rahul Sharma
   - Department: Engineering
   - Role: Senior Software Engineer
   
   **Goal Information:**
   - Title: Reduce API response time by 30%
   - Description: [Full description text]
   - Category: Technical
   - Priority: High
   - Timeline: Jan 15, 2026 - Mar 15, 2026 (60 days)
   
   **Manager Actions:**
   - ✅ Approve Goal (green button)
   - 📝 Request Changes (orange button)
   - ❌ Reject Goal (red button)
   - 💬 Add Comment (text area below)

6. Priya reviews the goal carefully:
   - Reads description
   - Checks if timeline is realistic (60 days seems reasonable)
   - Considers team priorities and workload

7. Priya decides to approve this goal
8. Clicks "✅ Approve Goal" button
9. Confirmation dialog appears:
   - "Are you sure you want to approve this goal?"
   - "Once approved, Rahul Sharma can begin working on this goal."
   - [Cancel] [Confirm Approval]
10. Priya clicks "Confirm Approval"

11. PUT request sent to `/api/goals/2001/approve`:
    ```json
    {
      "action": "approve",
      "approvedBy": 502,
      "approvalComments": "",
      "approvedDate": "2026-01-15T14:30:00"
    }
    ```

12. Backend receives request
13. Validates manager has authority to approve this goal (checks if manager ID matches AssignedManagerID)
14. Updates Goal table:
    - Status: "Pending" → "InProgress"
    - ApprovedBy: 502
    - ApprovedDate: 2026-01-15 14:30:00
    - LastModifiedDate: 2026-01-15 14:30:00

15. Backend creates notification for Rahul:
    - NotificationID: 1005
    - UserID: 501
    - Type: "GoalApproved"
    - Message: "Your goal 'Reduce API response time by 30%' has been approved by Priya Patel. You can now start working on it!"
    - RelatedEntityType: "Goal"
    - RelatedEntityID: 2001
    - Status: "Unread"
    - Priority: "High"
    - CreatedDate: 2026-01-15 14:30:00

16. Backend creates audit log entry:
    - AuditID: 9020
    - UserID: 502
    - Action: "GoalApproved"
    - Details: "Approved goal 'Reduce API response time by 30%' (ID: 2001) for Rahul Sharma"
    - RelatedEntityType: "Goal"
    - RelatedEntityID: 2001
    - Timestamp: 2026-01-15 14:30:00

17. Backend returns success response
18. Frontend shows success message: "Goal approved successfully!"
19. Modal closes automatically
20. Goal table refreshes - approved goal now shows:
    - Status: "InProgress" (green badge)
    - Actions column now shows: [View Progress] instead of [View Details]

### Step 4: Manager Requests Changes on Second Goal
1. Priya clicks "View Details" on second goal (React Certification)
2. Goal details modal opens
3. Priya reads through the goal
4. Notices the goal description is vague - doesn't specify which React certification
5. Clicks "📝 Request Changes" button
6. Comment text area expands and becomes required
7. Priya types in comment box:
   "Please specify which React certification you plan to complete. There are multiple options on Udemy - React Advanced or React Complete Guide? Also, please add more detail about the final project you'll be building."

8. Priya clicks "Submit Request" button
9. Confirmation dialog: "This will send the goal back to Rahul for revision. Continue?"
10. Priya confirms

11. PUT request sent to `/api/goals/2002/request-changes`:
    ```json
    {
      "action": "request_changes",
      "requestedBy": 502,
      "comments": "Please specify which React certification...",
      "requestedDate": "2026-01-15T14:35:00"
    }
    ```

12. Backend updates Goal table:
    - Status remains: "Pending"
    - RequestChanges: true
    - LastReviewedBy: 502
    - LastReviewedDate: 2026-01-15 14:35:00

13. Backend inserts feedback record into Feedback table:
    - FeedbackID: 4001
    - GoalID: 2002
    - GivenByUserID: 502
    - Comments: "Please specify which React certification..."
    - FeedbackType: "ChangeRequest"
    - Date: 2026-01-15 14:35:00

14. Backend creates notification for Rahul:
    - NotificationID: 1006
    - UserID: 501
    - Type: "GoalChangeRequested"
    - Message: "Priya Patel has requested changes to your goal 'Complete React Advanced Certification'. Please review the feedback and update your goal."
    - RelatedEntityType: "Goal"
    - RelatedEntityID: 2002
    - Status: "Unread"
    - Priority: "Medium"

15. Backend creates audit log entry for change request
16. Success message shown: "Change request sent to Rahul Sharma"
17. Goal remains in Pending list but now shows indicator: "⚠️ Changes Requested"

### Step 5: Manager Approves Third Goal
1. Priya clicks "View Details" on third goal (Mentoring)
2. Reviews goal details
3. Sees mentoring goal is well-defined
4. Clicks "✅ Approve Goal"
5. Confirms approval
6. System follows same approval workflow as Goal 1
7. Goal status changes to "InProgress"
8. Notification sent to Rahul

9. Priya's Team Goals dashboard now shows:
   - 1 goal pending (with changes requested)
   - 2 goals in progress
   - Notification badge reduces from 3 to 0

---

## **PHASE 4: Employee Updates Goal and Works on Goals**

### Step 1: Employee Receives Notifications
1. Rahul logs in next morning (January 16, 2026)
2. Backend creates audit log for login
3. Sees notification badge showing "3" notifications
4. Clicks notification bell
5. Sees three notifications:
   - "Your goal 'Reduce API response time by 30%' has been approved..." (High priority)
   - "Priya Patel has requested changes to your goal 'Complete React Advanced Certification'..." (Medium priority)
   - "Your goal 'Mentor 2 junior developers' has been approved..." (Medium priority)
6. Clicks on the change request notification first
7. System marks as read and redirects to goal details

### Step 2: Employee Revises Goal Based on Feedback
1. Goal details page loads for Goal 2002
2. Page shows:
   - Goal title and current description
   - Status: "Pending - Changes Requested"
   - Manager Feedback section (highlighted):
     - From: Priya Patel
     - Date: Jan 15, 2026
     - Feedback: "Please specify which React certification you plan to complete..."
   - [Edit Goal] button (enabled because changes were requested)

3. Rahul clicks "[Edit Goal]" button
4. Goal edit form loads with existing data pre-filled
5. Rahul updates:
   - Title: "Complete React - The Complete Guide Certification (Udemy)"
   - Description: "Complete Maximilian Schwarzmüller's 'React - The Complete Guide' course on Udemy (65 hours). This includes all coding exercises, 4 section projects, and a final e-commerce application project using React, Redux, and Firebase. The final project will demonstrate proficiency in React hooks, Context API, Redux state management, authentication, and database integration."
6. Rahul clicks "Save Changes"
7. Confirmation: "Your goal has been updated and resubmitted for approval. Continue?"
8. Rahul confirms

9. PUT request sent to `/api/goals/2002`:
    ```json
    {
      "title": "Complete React - The Complete Guide Certification (Udemy)",
      "description": "Complete Maximilian Schwarzmüller's...",
      "resubmitted": true,
      "resubmittedDate": "2026-01-16T09:15:00"
    }
    ```

10. Backend updates Goal table:
    - Title, Description updated
    - RequestChanges: false
    - Status: "Pending" (back to normal pending, not "changes requested")
    - ResubmittedDate: 2026-01-16 09:15:00
    - LastModifiedDate: 2026-01-16 09:15:00

11. Backend creates notification for Priya:
    - Type: "GoalResubmitted"
    - Message: "Rahul Sharma has updated and resubmitted the goal 'Complete React - The Complete Guide Certification' based on your feedback"
    - RelatedEntityID: 2002

12. Backend creates audit log entry for goal update
13. Success message: "Goal updated and resubmitted successfully!"
14. Goal moves back to Priya's pending approval queue

### Step 3: Employee Begins Working on Approved Goals
1. Rahul navigates to "My Goals" → "In Progress" tab
2. Sees two approved goals ready to work on:
   - Goal 2001: Reduce API response time by 30%
   - Goal 2003: Mentor 2 junior developers
3. Clicks on Goal 2001 to view details
4. Goal progress tracking page loads:

   **Goal Progress Tracking Interface:**
   
   **Goal Header:**
   - Reduce API response time by 30%
   - Status: In Progress 🟢
   - Timeline: Jan 15 - Mar 15, 2026
   - Days Remaining: 58 days
   - Overall Progress: Working on it
   
   **Progress Updates:**
   - [+ Add Progress Update] button
   - List of previous updates (currently empty)
   
   **Evidence Collection Area:** (Currently shown as information only)
   - 📝 Note: Evidence link will be required when you mark this goal as complete
   - You will need to provide a single link as proof of completion
   
   **Actions:**
   - [Update Progress]
   - [Mark as Complete] (grayed out - available only when ready to submit for completion approval)

5. Rahul works on the goal over the next several weeks
6. Periodically clicks [+ Add Progress Update] to log progress:
   - February 1: "Completed analysis of all 47 API endpoints. Identified 12 slow queries requiring optimization."
   - February 15: "Implemented Redis caching. Seeing 15% improvement so far."
   - March 1: "Optimized all remaining queries. Total improvement now at 35%."

7. Progress updates are saved to database for tracking and visibility

---

## **PHASE 5: Employee Marks Goal Complete with Evidence Link (NEW WORKFLOW)**

### Step 1: Employee Completes Goal Work
1. March 10, 2026 - Rahul has completed all work on API optimization goal
2. Logs into system
3. Navigates to "My Goals" → "In Progress" tab
4. Sees Goal 2001 with all progress updates showing completion
5. Clicks on goal to open details
6. Notices the [Mark as Complete] button is now active (green)
7. Clicks "[Mark as Complete]" button

### Step 2: Evidence Link Submission Form (NEW - Simplified)
1. "Submit Goal for Completion Approval" modal appears
2. Modal displays evidence requirement and submission form:

   **Submit Goal Completion Modal:**
   
   **Goal:** Reduce API response time by 30%
   
   **Completion Summary:**
   - Timeline: Jan 15 - Mar 15, 2026
   - Actual Completion: March 10, 2026 (5 days early)
   - Duration: 54 days
   
   **Evidence Requirement:**
   You must provide an evidence link before this goal can be submitted for completion approval.
   
   **Evidence Link Submission:**
   
   - **Evidence Link (Required):** *
     [Text input with URL validation]
     Example: https://drive.google.com/file/d/abc123/view
     
   - **Link Description:** *
     [Text area]
     "Describe what this link contains and how it proves goal completion"
   
   - **Access Instructions:** (Optional)
     [Text area]
     "Provide any login credentials or special instructions needed to access this link"
   
   **Completion Notes:** (Optional)
   [Rich text area]
   "Provide any additional context about goal completion, challenges faced, or learnings gained"
   
   **Actions:**
   [Cancel] [Submit for Approval]

3. Rahul fills in the evidence link information:
   
   - **Evidence Link:**
     "https://drive.google.com/drive/folders/1a2b3c4d5e6f7g8h9i0j?usp=sharing"
   
   - **Link Description:**
     "Google Drive folder containing:
     1. Grafana dashboard screenshots showing API response time improvements (305ms → 198ms, 35% improvement)
     2. All 5 GitHub pull request links for the optimization work
     3. Confluence documentation page link with complete technical details
     4. Team presentation recording demonstrating the optimization techniques
     
     This folder provides comprehensive proof of the 35% API performance improvement, exceeding the 30% target."
   
   - **Access Instructions:**
     "Link has view access for anyone at @company.com domain. No special login required for internal employees."

4. Rahul adds completion notes:
   "Successfully exceeded the goal target by achieving 35% improvement instead of 30%. Key learnings: (1) Redis caching provided the biggest impact (15% improvement alone), (2) Query optimization required careful analysis but yielded consistent gains, (3) Connection pooling resolved intermittent latency spikes. Challenges: Initial Redis configuration required several iterations to optimize cache hit rates. The optimization techniques are now documented and can be applied to other services. Estimated impact: Improved user experience for 50,000+ daily API users, reduced server costs by ~$800/month."

5. Frontend validates:
   - Evidence link is provided and is a valid URL format
   - Link description is provided (minimum 20 characters)
   - All required fields are filled

6. Rahul clicks "Submit for Approval" button

7. Confirmation dialog appears:
   "Submit Goal for Completion Approval?"
   
   "This will:"
   - Mark your goal as completed pending manager verification
   - Send the evidence link to your manager (Priya Patel) for review
   - Lock the goal from further edits until approval/rejection
   
   "Your manager will review the evidence and either approve completion or request additional information."
   
   [Go Back] [Confirm Submission]

8. Rahul clicks "Confirm Submission"

### Step 3: Backend Processes Completion Submission
1. POST request sent to `/api/goals/2001/submit-completion`:
   ```json
   {
     "completionDate": "2026-03-10",
     "completionNotes": "Successfully exceeded the goal target...",
     "evidenceLink": "https://drive.google.com/drive/folders/1a2b3c4d5e6f7g8h9i0j?usp=sharing",
     "evidenceLinkDescription": "Google Drive folder containing: 1. Grafana dashboard...",
     "evidenceAccessInstructions": "Link has view access for anyone at @company.com domain...",
     "submittedBy": 501,
     "submittedDate": "2026-03-10T15:30:00"
   }
   ```

2. Backend validates request:
   - User is authenticated and owns this goal
   - Goal status is "InProgress"
   - Evidence link is provided and valid URL format
   - Link description is provided

3. Backend updates Goal table:
   - Status: "InProgress" → "PendingCompletionApproval" (NEW STATUS)
   - CompletionSubmittedDate: 2026-03-10 15:30:00
   - CompletionNotes: "Successfully exceeded the goal target..."
   - EvidenceLink: "https://drive.google.com/drive/folders/1a2b3c4d5e6f7g8h9i0j?usp=sharing" (NEW FIELD)
   - EvidenceLinkDescription: "Google Drive folder containing..." (NEW FIELD)
   - EvidenceAccessInstructions: "Link has view access..." (NEW FIELD)
   - CompletionApprovalStatus: "Pending" (NEW FIELD)
   - LastModifiedDate: 2026-03-10 15:30:00

4. Backend creates notification for manager (Priya):
   - NotificationID: 1010
   - UserID: 502
   - Type: "GoalCompletionSubmitted"
   - Message: "Rahul Sharma has marked goal 'Reduce API response time by 30%' as complete and submitted evidence link for your review. Action required."
   - RelatedEntityType: "Goal"
   - RelatedEntityID: 2001
   - Status: "Unread"
   - Priority: "High"
   - ActionRequired: true
   - CreatedDate: 2026-03-10 15:30:00

5. Backend creates audit log entry:
   - AuditID: 9050
   - UserID: 501
   - Action: "GoalCompletionSubmitted"
   - Details: "Submitted goal 'Reduce API response time by 30%' (ID: 2001) for completion approval with evidence link"
   - RelatedEntityType: "Goal"
   - RelatedEntityID: 2001
   - Timestamp: 2026-03-10 15:30:00

6. Backend returns success response

7. Frontend displays success message:
   "Goal submitted for completion approval!"
   
   "Your goal has been submitted to Priya Patel for verification. You will receive a notification once your manager reviews the evidence and approves or provides feedback."

8. Goal automatically moves from "In Progress" tab to new "Pending Completion Approval" tab
9. Goal card shows:
   - Status: "Pending Completion Approval" (orange badge)
   - Submitted: March 10, 2026
   - Evidence: Link submitted ✓
   - Awaiting: Manager review

10. Rahul's "My Goals" dashboard updates:
    - In Progress: 1 goal (Mentor 2 junior developers)
    - Pending Completion Approval: 1 goal (API optimization)
    - All tabs showing updated counts

---

## **PHASE 6: Manager Reviews Goal Completion and Evidence Link (NEW WORKFLOW)**

### Step 1: Manager Receives Completion Notification
1. March 11, 2026 - Priya logs into system
2. Backend creates audit log for login
3. Dashboard loads showing notifications badge: "4"
4. Priya also sees new section highlighted: "Goal Completion Approvals" with red badge showing "1"
5. Clicks on notification bell
6. Sees notification: "Rahul Sharma has marked goal 'Reduce API response time by 30%' as complete and submitted evidence link for your review. Action required." (High priority)
7. Clicks notification
8. System marks as read
9. Redirected to "Goal Completion Approvals" section

### Step 2: Goal Completion Approvals Dashboard
1. Goal Completion Approvals page loads with pending completions list:

   **Goal Completion Approvals Dashboard:**
   
   **Filters:**
   - Employee: [dropdown]
   - Submission Date: [date range]
   - Priority: [dropdown]
   
   **Pending Approvals Table:**
   | Employee | Goal Title | Category | Submitted Date | Days Since Submission | Evidence | Priority | Actions |
   |----------|-----------|----------|----------------|---------------------|----------|----------|---------|
   | Rahul Sharma | Reduce API response time by 30% | Technical | Mar 10, 2026 | 1 day ago | Link | High 🔴 | [Review Evidence] |
   
   **Quick Stats:**
   - Pending Reviews: 1
   - Avg Review Time: N/A
   - This Week: 1 submission

2. Priya clicks "[Review Evidence]" button for Rahul's goal

### Step 3: Evidence Review Interface (NEW - Simplified for Link Only)
1. Comprehensive goal completion review page loads:

   **Goal Completion Review Interface:**
   
   **Header:**
   - Reduce API response time by 30%
   - Status: Pending Completion Approval
   - Employee: Rahul Sharma | Department: Engineering
   - Submitted: March 10, 2026 at 3:30 PM (1 day ago)
   
   **Tab Navigation:**
   - [Goal Details]
   - [Evidence Review] ← Currently active
   - [Completion History]
   
   ---
   
   **Goal Overview Section:**
   - Original Goal: "Optimize database queries and implement caching to improve API performance from average 300ms to 210ms"
   - Target: 30% improvement
   - Timeline: Jan 15 - Mar 15, 2026 (completed 5 days early ✓)
   
   **Employee's Completion Notes:**
   [Displays Rahul's completion notes]
   "Successfully exceeded the goal target by achieving 35% improvement instead of 30%. Key learnings: (1) Redis caching provided the biggest impact (15% improvement alone)..."
   
   ---
   
   **Evidence Review Section:**
   
   **Evidence Link:**
   - **URL:** https://drive.google.com/drive/folders/1a2b3c4d5e6f7g8h9i0j?usp=sharing
     [Open Link in New Tab] button
   
   - **Link Description:**
     "Google Drive folder containing:
     1. Grafana dashboard screenshots showing API response time improvements (305ms → 198ms, 35% improvement)
     2. All 5 GitHub pull request links for the optimization work
     3. Confluence documentation page link with complete technical details
     4. Team presentation recording demonstrating the optimization techniques
     
     This folder provides comprehensive proof of the 35% API performance improvement, exceeding the 30% target."
   
   - **Access Instructions:**
     "Link has view access for anyone at @company.com domain. No special login required for internal employees."
   
   - **Submitted:** March 10, 2026 at 3:30 PM
   
   **Manager Verification:**
   - Verification Status: [Dropdown: Not Yet Verified / Verified / Rejected / Needs Additional Link]
   - Verification Notes: [Text area for manager comments]
   - [Save Verification Status]
   
   ---
   
   **Overall Completion Assessment:**
   
   **Manager's Final Comments:**
   [Rich text area for overall feedback]
   
   **Completion Decision:**
   - ✅ Approve Completion (green button)
   - ⚠️ Request Additional Evidence (orange button)
   - ❌ Reject Completion (red button)

### Step 4: Manager Reviews Evidence Link
1. Priya clicks "[Open Link in New Tab]" button
2. New browser tab opens with Google Drive folder
3. Priya reviews the contents:
   - Sees folder with 4 items as described
   - Opens Grafana dashboard screenshots - verifies performance data showing 305ms → 198ms (35% improvement)
   - Checks GitHub PR links - confirms 5 PRs related to optimization, all merged
   - Reviews Confluence documentation - comprehensive 15-page technical doc
   - Watches first few minutes of presentation recording - Rahul explaining techniques to team
4. All evidence confirms goal completion and exceeds target
5. Priya closes the Google Drive tab and returns to review interface

6. In Evidence verification section, Priya:
   - Selects "Verified" from dropdown
   - Adds verification notes: "Evidence link confirmed. Google Drive folder contains comprehensive proof of goal completion: Grafana dashboards show clear 35% improvement (exceeding 30% target), all GitHub PRs verified and merged, excellent technical documentation, and bonus presentation recording. All materials are high quality and demonstrate thorough work."
   - Clicks [Save Verification Status]

7. PUT request sent to `/api/goals/2001/evidence/verify`:
   ```json
   {
     "verificationStatus": "Verified",
     "verificationNotes": "Evidence link confirmed. Google Drive folder contains...",
     "verifiedBy": 502,
     "verifiedDate": "2026-03-11T10:15:00"
   }
   ```

8. Backend updates Goal table:
   - EvidenceLinkVerificationStatus: "Verified"
   - EvidenceLinkVerificationNotes: "Evidence link confirmed..."
   - EvidenceLinkVerifiedBy: 502
   - EvidenceLinkVerifiedDate: 2026-03-11 10:15:00

9. Success message: "Evidence verification saved"
10. Evidence section now shows green checkmark: ✅ Verified

### Step 5: Manager Approves Goal Completion
1. Priya scrolls to "Overall Completion Assessment" section
2. Evidence verified, ready to make final decision
3. Priya types in "Manager's Final Comments":
   "Exceptional work, Rahul! You not only met but exceeded the goal target with 35% improvement vs. 30% target. The evidence provided through the Google Drive folder is comprehensive and demonstrates thorough work:
   
   ✓ Performance dashboards show clear, measurable improvement with data over 2-month period
   ✓ Code changes are substantial, well-reviewed, and properly merged (5 PRs)
   ✓ Documentation is excellent and will serve as valuable team resource
   ✓ Bonus presentation shows initiative in knowledge sharing
   
   Key highlights:
   - Completed 5 days ahead of schedule
   - Exceeded target by 5 percentage points
   - Estimated cost savings of $800/month
   - Created reusable documentation for future optimization projects
   - Shared knowledge with team through presentation
   
   This is exactly the kind of thorough, high-impact work we value. Well done!"

4. Priya clicks "✅ Approve Completion" button
5. Final confirmation dialog appears:
   "Approve Goal Completion?"
   
   "This will:"
   - Mark the goal as officially completed
   - Record the goal as successfully achieved in Rahul's performance record
   - Close the goal and prevent further modifications
   - Send completion confirmation to Rahul
   - Update goal completion metrics for reporting
   
   "This action cannot be undone."
   
   [Go Back] [Confirm Approval]

6. Priya clicks "Confirm Approval"

### Step 6: Backend Processes Completion Approval
1. POST request sent to `/api/goals/2001/approve-completion`:
   ```json
   {
     "approvalDecision": "Approved",
     "managerComments": "Exceptional work, Rahul! You not only met but exceeded...",
     "approvedBy": 502,
     "approvalDate": "2026-03-11T10:45:00",
     "evidenceVerificationStatus": "Verified"
   }
   ```

2. Backend validates:
   - Manager has authority to approve this goal
   - Evidence link is verified
   - Goal is in "PendingCompletionApproval" status

3. Backend updates Goal table:
   - Status: "PendingCompletionApproval" → "Completed" (FINAL STATUS)
   - CompletionApprovalStatus: "Approved"
   - CompletionApprovedBy: 502
   - CompletionApprovedDate: 2026-03-11 10:45:00
   - FinalCompletionDate: 2026-03-11 10:45:00
   - ManagerCompletionComments: "Exceptional work, Rahul!..."
   - LastModifiedDate: 2026-03-11 10:45:00

4. Backend inserts completion approval record into GoalCompletionApproval table:
   - ApprovalID: 6001
   - GoalID: 2001
   - ApprovalDecision: "Approved"
   - ApprovedBy: 502
   - ApprovalDate: 2026-03-11 10:45:00
   - ManagerComments: "Exceptional work, Rahul!..."
   - EvidenceLinkVerified: true
   - DecisionRationale: "Evidence verified and goal exceeded target"

5. Backend creates notification for Rahul:
   - NotificationID: 1015
   - UserID: 501
   - Type: "GoalCompletionApproved"
   - Message: "🎉 Congratulations! Your manager has approved the completion of your goal 'Reduce API response time by 30%'. Great work!"
   - RelatedEntityType: "Goal"
   - RelatedEntityID: 2001
   - Status: "Unread"
   - Priority: "High"
   - CreatedDate: 2026-03-11 10:45:00

6. Backend creates audit log entry:
   - AuditID: 9055
   - UserID: 502
   - Action: "GoalCompletionApproved"
   - Details: "Approved completion of goal 'Reduce API response time by 30%' (ID: 2001) for Rahul Sharma. Evidence link verified. Goal exceeded target (35% vs 30%)."
   - RelatedEntityType: "Goal"
   - RelatedEntityID: 2001
   - Timestamp: 2026-03-11 10:45:00

7. Backend updates performance metrics:
   - Increments Rahul's completed goals count
   - Updates team goal completion rate
   - Records goal completion for reporting analytics

8. Backend returns success response

9. Frontend displays success message:
   "Goal completion approved!"
   
   "Rahul Sharma has been notified that the goal 'Reduce API response time by 30%' is officially completed. The goal has been added to his performance record."

10. Goal disappears from "Pending Completion Approvals" list
11. Priya's dashboard updates:
    - Goal Completion Approvals badge: 1 → 0
    - Team completed goals count increases

### Step 7: Manager Reviews Goal Requiring Additional Evidence (Alternative Flow)
1. Let's say there's another employee, Sneha, who submitted a goal completion with insufficient evidence
2. Priya reviews Sneha's goal: "Implement automated testing framework"
3. Evidence submitted:
   - Evidence Link: https://github.com/company/project/tree/testing
   - Link Description: "GitHub repository branch with testing framework code"
4. Priya clicks to open the GitHub link
5. Reviews the repository:
   - Code is there but limited test coverage visible
   - No documentation in README
   - No test coverage metrics visible
   - Link only shows code, not proof of test coverage or documentation
6. Priya returns to review interface
7. In verification section, selects "Needs Additional Link"
8. Adds verification notes: "Repository link works but I cannot see test coverage reports or documentation. Please provide an additional link that shows test coverage metrics or a link to documentation explaining the framework."
9. Saves verification status
10. In "Overall Completion Assessment", Priya clicks "⚠️ Request Additional Evidence"
11. Additional evidence request form appears:
    - What additional evidence link is needed: [text area]
    - Deadline for resubmission: [date picker]

12. Priya fills in:
    - "Please provide an additional link showing:
      1. Test coverage report with percentage coverage achieved, OR
      2. Link to documentation (README in GitHub or Confluence) explaining how to use the testing framework
      
      The current GitHub link shows the code but doesn't demonstrate the coverage achieved or provide usage documentation."
    - Deadline: March 15, 2026

13. Clicks "Submit Request"
14. POST request to `/api/goals/[goalId]/request-additional-evidence`
15. Backend updates goal:
    - CompletionApprovalStatus: "AdditionalEvidenceRequired"
    - Status remains: "PendingCompletionApproval"
16. Notification sent to Sneha:
    - Type: "AdditionalEvidenceRequired"
    - Message: "Priya Patel has reviewed your goal completion but requires an additional evidence link. Please review the feedback and resubmit."
17. Backend creates audit log entry
18. Sneha receives notification and can see specific feedback
19. Sneha can update the evidence link (add or replace)
20. Goal returns to manager's queue once resubmitted

### Step 8: Manager Rejects Goal Completion (Alternative Flow - Rarely Used)
1. In extreme cases where goal clearly wasn't completed as stated
2. Manager clicks "❌ Reject Completion"
3. Rejection reason form appears (required):
   - Primary reason: [dropdown: Evidence Insufficient / Goal Not Actually Completed / Evidence Link Broken / Other]
   - Detailed explanation: [text area - required, minimum 100 characters]
   - Next steps for employee: [text area]
4. Manager fills form and confirms rejection
5. POST request to `/api/goals/[goalId]/reject-completion`
6. Backend updates goal:
   - Status: "PendingCompletionApproval" → back to "InProgress"
   - CompletionApprovalStatus: "Rejected"
   - RejectionReason: [detailed explanation]
7. Notification sent to employee with detailed feedback
8. Goal goes back to employee's "In Progress" tab
9. Employee can continue working and resubmit when ready
10. Audit log records rejection with full details

---

## **PHASE 7: Employee Views Completion Approval (Simplified)**

### Step 1: Employee Receives Approval Notification
1. Rahul logs in on March 11, 2026 afternoon
2. Backend creates audit log for login
3. Sees notification badge: "1"
4. Clicks notification bell
5. Sees celebration notification: "🎉 Congratulations! Your manager has approved the completion of your goal 'Reduce API response time by 30%'. Great work!"
6. Clicks notification
7. System marks as read

### Step 2: View Completed Goal with Manager Feedback
1. Redirected to goal details page
2. Page displays completed goal with feedback:

   **Completed Goal View:**
   
   **Header:**
   - Reduce API response time by 30% ✅
   - Status: COMPLETED (green badge with checkmark)
   - Completion Date: March 11, 2026
   - Duration: 55 days (Jan 15 - Mar 11)
   - Completed: 4 days early
   
   **Achievement Summary:**
   - Target: 30% improvement
   - Achieved: 35% improvement (based on evidence)
   - Performance: EXCEEDED TARGET 🎯
   
   **Evidence Submitted:**
   - Evidence Link: [Link to Google Drive folder]
   - Link Description: [Shows description]
   - Manager Verification: ✅ Verified by Priya Patel on Mar 11, 2026
   
   **Manager's Completion Comments:**
   [Displays Priya's detailed feedback]
   "Exceptional work, Rahul! You not only met but exceeded the goal target with 35% improvement vs. 30% target. The evidence provided through the Google Drive folder is comprehensive and demonstrates thorough work..."
   
   **Completion Impact:**
   - Estimated Cost Savings: $800/month
   - User Impact: 50,000+ daily API users
   - Knowledge Sharing: Team presentation completed
   - Documentation: Reusable for future projects
   
   **Actions:**
   - [View Evidence Link]
   - [Add to Performance Portfolio]

3. Rahul reads manager's feedback
4. Feels accomplished seeing evidence verified
5. Clicks "Add to Performance Portfolio"
6. Confirmation: "This goal has been added to your performance portfolio and will be included in your performance review."

### Step 3: Employee's Goals Dashboard Updates
1. Rahul navigates to "My Goals" main dashboard
2. Updated tabs show:
   - All Goals (3)
   - Pending Approval (1) - React certification still pending Priya's approval
   - In Progress (1) - Mentoring goal
   - Pending Completion Approval (0)
   - Completed (1) - API optimization ✅
3. Dashboard statistics update:
   - Goals Created: 3
   - Goals Approved: 3
   - Goals In Progress: 1
   - Goals Completed: 1
   - Completion Rate: 33%
   - Goals Pending: 1
4. Achievement indicators appear showing goal exceeded target

---

## **PHASE 8: Review Cycle - Self Assessment (Simplified - No Evidence in Self-Assessment)**

### Step 1: Review Period Begins
1. March 20, 2026 - Review cycle end approaching (11 days remaining)
2. System runs scheduled job (cron job) daily at 9:00 AM
3. Backend queries ReviewCycle table for cycles ending within 10 days
4. Finds "Q1 2026 Performance Review" (CycleID: 101, ends March 31)
5. System logic checks:
   - Cycle status: "Active" ✓
   - End date within 10 days: ✓ (11 days remaining)
   - Self-assessment reminder not sent in last 5 days: ✓
6. Backend queries User table for all active employees (Status = "Active")
7. Retrieves list of 150 active employees
8. For each employee, system creates notification:

   **Notification Creation Logic:**
   - Check if employee already completed self-assessment
   - If not completed, create reminder notification
   - If completed, skip notification

9. Backend creates notifications in batch:
   - NotificationID: 1020-1169 (150 notifications)
   - Type: "ReviewReminder"
   - Message: "Q1 2026 Performance Review deadline is March 31 (11 days remaining). Please complete your self-assessment."
   - Status: "Unread"
   - Priority: "High"
   - ActionRequired: true
   - CreatedDate: 2026-03-20 09:00:00

10. Backend creates audit log entries for notification batch
11. System sends in-app notification to all 150 employees

### Step 2: Employee Receives Reminder
1. Rahul logs in on March 20, 2026
2. Backend creates audit log for login
3. Dashboard loads
4. Prominent banner appears at top:
   "⚠️ Action Required: Q1 2026 Performance Review"
   "Deadline: March 31, 2026 (11 days remaining)"
   [Start Self-Assessment] button
5. Notification badge shows "2"
6. Clicks notification bell
7. Sees notifications:
   - "Q1 2026 Performance Review deadline is March 31..." (High priority, action required)
   - Another system notification
8. Clicks review reminder notification
9. System marks as read
10. Redirected to "My Reviews" section

### Step 3: My Reviews Dashboard
1. "My Reviews" page loads with review cycle information:

   **My Reviews Dashboard:**
   
   **Active Review Cycle:**
   - Title: Q1 2026 Performance Review
   - Period: January 1 - March 31, 2026
   - Status: Self-Assessment Pending ⚠️
   - Deadline: March 31, 2026 (11 days remaining)
   - Progress: 0% Complete
   
   **Your Status:**
   - Self-Assessment: Not Started ❌
   - Manager Review: Waiting for your self-assessment
   - Final Status: Pending
   
   **Your Goals for This Cycle:**
   - ✅ Reduce API response time by 30% - Completed (Exceeded target)
   - 🔄 Complete React Certification - In Progress (85% complete)
   - 🔄 Mentor 2 junior developers - In Progress (Ongoing)
   
   **Actions:**
   [Start Self-Assessment] (large, prominent button)
   [View Review Guidelines]
   [See Past Reviews]

2. Rahul clicks "Start Self-Assessment" button

### Step 4: Self-Assessment Form (Simplified - No Evidence Section)
1. Self-assessment form loads with simplified sections:

   **Q1 2026 Self-Assessment Form:**
   
   **Instructions:**
   "Please complete all sections honestly and thoroughly. This self-assessment will be shared with your manager (Priya Patel) and will be part of your official performance record."
   
   ---
   
   **SECTION 1: GOAL ACHIEVEMENT REVIEW**
   
   **Goal 1: Reduce API response time by 30%**
   - Original Target: 30% improvement
   - Status: ✅ Completed
   - Completion Date: March 11, 2026
   - Manager Approved: Yes
   
   **Your Achievement Summary:**
   [Text area, required]
   [Rahul types:]
   "Achieved 35% improvement in API response time, exceeding the 30% target. Implemented Redis caching, optimized 15 database queries, and improved connection pooling. Completed 4 days ahead of schedule. Created comprehensive documentation that can be used by other teams for similar optimizations."
   
   **Impact & Learnings:**
   [Text area, required]
   [Rahul types:]
   "This project had significant impact on system performance and user experience. Key impacts:
   - Improved response times for 50,000+ daily API users
   - Reduced server costs by ~$800/month
   - Enhanced system scalability
   
   Key learnings:
   - Redis caching provided 15% improvement alone - caching strategy is crucial
   - Query optimization requires careful analysis but yields consistent gains
   - Documentation and knowledge sharing multiply the impact
   
   Challenges overcome:
   - Initial Redis configuration required multiple iterations
   - Balancing cache hit rates with memory usage
   
   Skills developed: Redis architecture, query optimization, performance benchmarking"
   
   ---
   
   **Goal 2: Complete React Advanced Certification**
   - Original Target: Complete course and certification
   - Status: 🔄 In Progress (85% complete)
   - Expected Completion: March 28, 2026
   
   **Progress Update:**
   [Text area, required]
   [Rahul types:]
   "Made excellent progress on React certification. Currently 85% complete with 7 days remaining before deadline.
   
   Completed:
   - 55 of 65 course hours finished
   - 3 of 4 section projects completed
   - Advanced hooks, Context API, and Redux modules completed
   
   Remaining:
   - Final 10 hours of content
   - Final e-commerce project (60% done)
   - Certificate exam
   
   On track to complete by March 28, 3 days before review deadline."
   
   **Why not completed?** (shown for in-progress goals)
   [Text area]
   "Course is comprehensive (65 hours total) and I prioritized the API optimization goal which had higher business impact. Maintained steady progress averaging 8 hours/week on the certification."
   
   ---
   
   **Goal 3: Mentor 2 junior developers**
   - Original Target: Provide mentoring to 2 junior developers
   - Status: 🔄 In Progress (Ongoing throughout quarter)
   - Expected Completion: March 31, 2026
   
   **Progress Update:**
   [Text area, required]
   [Rahul types:]
   "Successfully mentoring Amit Patel and Sneha Kumar throughout Q1 2026.
   
   Mentoring activities:
   - Weekly 1-hour mentoring sessions (12 sessions completed)
   - Code reviews: Reviewed 25+ pull requests with detailed feedback
   - Pair programming: 8 sessions on complex features
   - Career guidance: 3 sessions discussing career paths
   
   Mentee progress:
   - Amit: Improved code quality significantly, PR revision cycles reduced from 3 to 1.5
   - Sneha: Strong progress in backend development, code coverage improved from 60% to 85%
   
   Impact: Both mentees have grown significantly and received positive feedback from team lead."
   
   **Will this be completed by deadline?**
   Yes ✓ / No / Uncertain
   [Selected: Yes]
   
   ---
   
   **SECTION 2: ADDITIONAL ACHIEVEMENTS** (Optional but recommended)
   
   "List any significant achievements, contributions, or accomplishments not covered in your goals:"
   
   [Text area]
   [Rahul types:]
   "Beyond my primary goals, I contributed to several additional areas:
   
   1. Emergency Production Issue Resolution:
      - Diagnosed and fixed critical database deadlock issue in February
      - Worked extra hours over weekend to resolve
   
   2. Team Collaboration:
      - Led technical design discussion for new microservices architecture
      - Presented API optimization learnings to entire engineering team
   
   3. Process Improvements:
      - Created reusable performance testing framework now used by team
      - Documented best practices for database optimization
   
   4. Continuous Learning:
      - Completed LinkedIn Learning course on System Design
      - Attended 2 technical conferences (virtual)"
   
   ---
   
   **SECTION 3: CHALLENGES FACED** (Optional but recommended)
   
   "What challenges or obstacles did you face this quarter? How did you address them?"
   
   [Text area]
   [Rahul types:]
   "Key challenges and how I addressed them:
   
   1. Balancing Multiple Goals:
      - Challenge: Managing API optimization, certification, and mentoring simultaneously
      - Solution: Created structured schedule with dedicated time blocks
      - Outcome: Made progress on all goals without compromising quality
   
   2. Technical Complexity:
      - Challenge: Redis implementation was more complex than expected
      - Solution: Researched best practices, consulted with senior engineers
      - Outcome: Achieved 85% hit rate
   
   3. Knowledge Gaps:
      - Challenge: Limited prior experience with Redis at scale
      - Solution: Self-study through documentation and online courses
      - Outcome: Became team resource for caching strategies"
   
   ---
   
   **SECTION 4: AREAS FOR DEVELOPMENT** (Required)
   
   "What areas would you like to develop or improve? What support do you need?"
   
   [Text area, required]
   [Rahul types:]
   "Areas I want to develop in next quarter:
   
   1. System Design & Architecture:
      - Want to improve skills in designing scalable systems
      - Would benefit from: Mentorship from senior architect, system design training
   
   2. Leadership & Communication:
      - Want to develop presentation skills and technical leadership
      - Would benefit from: Leadership training, opportunities to lead larger projects
   
   3. Cloud Technologies:
      - Want to expand knowledge of cloud-native architectures
      - Would benefit from: Cloud certification support, hands-on cloud projects"
   
   ---
   
   **SECTION 5: SELF-RATING** (Required)
   
   "Rate your overall performance this quarter (1-5 scale):"
   
   [Rating scale with detailed descriptions]
   
   1 - Needs Significant Improvement
   2 - Needs Improvement
   3 - Meets Expectations
   4 - Exceeds Expectations
   5 - Outstanding
   
   **Your Rating:** [Dropdown selection]
   [Rahul selects: 4 - Exceeds Expectations]
   
   **Rationale for your rating:** (Required)
   [Text area]
   [Rahul types:]
   "I rate myself as 'Exceeds Expectations' (4) for the following reasons:
   
   Goal Achievement:
   - Completed 1 of 3 goals, exceeding target by 5%
   - On track to complete remaining 2 goals by deadline
   
   Quality of Work:
   - API optimization had measurable business impact ($800/month savings)
   - Work completed ahead of schedule
   
   Initiative & Leadership:
   - Went beyond requirements with documentation and knowledge sharing
   - Took initiative on additional contributions
   - Actively mentored junior team members
   
   Why not 5: One goal not yet completed, room to grow in architecture skills"
   
   ---
   
   **SECTION 6: GOALS FOR NEXT QUARTER** (Optional)
   
   "What goals would you like to set for Q2 2026?"
   
   [Text area]
   [Rahul types:]
   "Proposed goals for Q2 2026:
   
   1. Lead Microservices Migration Project
   2. Obtain AWS Solutions Architect Certification
   3. Continue Mentoring + Expand to Team Workshops
   4. Contribute to 2 High-Priority Features"
   
   ---
   
   **SECTION 7: ADDITIONAL COMMENTS** (Optional)
   
   [Text area]
   [Rahul types:]
   "Thank you to Priya for providing clear direction and timely feedback. Grateful for the team's collaboration on the API optimization project. Looking forward to Q2!"
   
   ---
   
   **CERTIFICATION**
   
   ☑️ I certify that the information provided in this self-assessment is accurate and complete to the best of my knowledge.
   
   [Cancel] [Save as Draft] [Submit Self-Assessment]

2. Rahul has spent 30 minutes thoughtfully completing the self-assessment
3. Reviews all sections one more time
4. Satisfied with the content
5. Clicks "Submit Self-Assessment" button

6. Final confirmation dialog appears:
   "Submit Self-Assessment?"
   
   "Once submitted:"
   - You cannot edit your self-assessment
   - It will be shared with your manager (Priya Patel)
   - It becomes part of your official performance record
   
   [Go Back to Edit] [Confirm Submission]

7. Rahul clicks "Confirm Submission"

### Step 5: Backend Processes Self-Assessment Submission
1. POST request sent to `/api/performance-reviews`:
   ```json
   {
     "cycleId": 101,
     "userId": 501,
     "selfAssessment": {
       "goal1Achievement": "Achieved 35% improvement...",
       "goal1Impact": "This project had significant impact...",
       "goal2Progress": "Made excellent progress...",
       "goal2Reason": "Course is comprehensive...",
       "goal3Progress": "Successfully mentoring...",
       "additionalAchievements": "Beyond my primary goals...",
       "challengesFaced": "Key challenges and how I addressed...",
       "developmentAreas": "Areas I want to develop...",
       "selfRating": 4,
       "ratingRationale": "I rate myself as 'Exceeds Expectations'...",
       "nextQuarterGoals": "Proposed goals for Q2 2026...",
       "additionalComments": "Thank you to Priya..."
     },
     "rating": null,
     "reviewDate": "2026-03-20T16:30:00",
     "submittedDate": "2026-03-20T16:30:00"
   }
   ```

2. Backend validates:
   - User is authenticated (UserID: 501)
   - Review cycle is active (CycleID: 101)
   - All required sections are completed
   - Self-rating is within valid range (1-5)
   - User hasn't already submitted self-assessment for this cycle

3. Backend creates PerformanceReview record:
   - ReviewID: 3001 (auto-generated)
   - CycleID: 101
   - UserID: 501
   - SelfAssessment: [Full JSON object with all sections]
   - EmployeeSelfRating: 4
   - ManagerFeedback: null (not filled yet)
   - ManagerRating: null (manager hasn't rated yet)
   - ReviewDate: 2026-03-20 16:30:00
   - Status: "SelfAssessmentCompleted"
   - SubmittedDate: 2026-03-20 16:30:00
   - LastModifiedDate: 2026-03-20 16:30:00

4. Backend links completed goals to performance review:
   - Queries Goal table for UserID 501 with Status "Completed"
   - Creates entries in PerformanceReviewGoals linking table:
     - ReviewID: 3001, GoalID: 2001

5. Backend creates notification for manager (Priya):
   - NotificationID: 1025
   - UserID: 502
   - Type: "SelfAssessmentSubmitted"
   - Message: "Rahul Sharma has submitted self-assessment for Q1 2026 Performance Review. Please review and provide your feedback by March 31, 2026."
   - RelatedEntityType: "PerformanceReview"
   - RelatedEntityID: 3001
   - Status: "Unread"
   - Priority: "High"
   - ActionRequired: true
   - CreatedDate: 2026-03-20 16:30:00

6. Backend creates audit log entry:
   - AuditID: 9060
   - UserID: 501
   - Action: "SelfAssessmentSubmitted"
   - Details: "Submitted self-assessment for Q1 2026 Performance Review (ReviewID: 3001). Self-rating: 4 (Exceeds Expectations). Completed goals: 1, In-progress goals: 2."
   - RelatedEntityType: "PerformanceReview"
   - RelatedEntityID: 3001
   - Timestamp: 2026-03-20 16:30:00

7. Backend updates user's review status tracking:
   - UserID: 501
   - CycleID: 101
   - SelfAssessmentStatus: "Completed"
   - SelfAssessmentSubmittedDate: 2026-03-20 16:30:00

8. Backend returns success response

9. Frontend displays success screen:
   "Self-Assessment Submitted Successfully! ✓"
   
   "Thank you, Rahul!"
   
   "Your self-assessment for Q1 2026 Performance Review has been submitted and shared with your manager, Priya Patel."
   
   **What happens next:**
   1. Your manager will review your self-assessment
   2. Your manager will evaluate your performance and goals
   3. Your manager will provide feedback and ratings
   4. You'll receive notification when review is complete
   
   **Timeline:**
   - Self-assessment submitted: March 20, 2026 ✓
   - Manager review deadline: March 31, 2026
   
   [Return to Dashboard] [View My Goals]

10. Rahul clicks "Return to Dashboard"

---

## **PHASE 9: Manager Reviews Performance**

### Step 1: Manager Gets Notification
1. March 21, 2026 - Priya logs in
2. Backend creates audit log for login
3. Sees notification about Rahul's self-assessment
4. Clicks notification
5. Redirected to Performance Reviews page

### Step 2: Performance Reviews Dashboard
1. Performance Reviews dashboard loads showing pending reviews
2. Table displays:
   | Employee | Self-Assessment Status | Self-Rating | Goals Status | Manager Review Status |
   |----------|----------------------|-------------|--------------|---------------------|
   | Rahul Sharma | Completed ✓ | 4 | 1 Complete, 2 In Progress | Pending |
   | [Other employees...] | ... | ... | ... | ... |

3. Priya clicks "Review Now" for Rahul

### Step 3: Manager Review Interface
1. Detailed performance review page loads with tabs:
   - Overview
   - Goals Performance
   - Self-Assessment
   - Manager Feedback
   - Final Review

2. **Overview Tab** shows:
   - Employee info: Rahul Sharma, Engineering, Senior Software Engineer
   - Review period: Q1 2026
   - Self-rating: 4 - Exceeds Expectations
   - Goals summary: 1 completed, 2 in progress
   - Manager review: Pending

3. Priya clicks through each tab to review

### Step 4: Goals Performance Tab
1. Shows all three goals:
   
   **Goal 1: Reduce API response time by 30%**
   - Status: ✅ Completed (Approved Mar 11)
   - Target: 30% → Achieved: 35%
   - Evidence: Link verified
   - Priya's previous approval comments visible
   
   **Goal 2: Complete React Certification**
   - Status: In Progress (85% complete)
   - Expected completion: Mar 28
   
   **Goal 3: Mentor 2 junior developers**
   - Status: In Progress
   - Ongoing throughout quarter

### Step 5: Self-Assessment Review Tab
1. Displays all sections Rahul submitted:
   - Goal achievement reviews
   - Additional achievements
   - Challenges faced
   - Development areas
   - Self-rating: 4 with rationale
   - Next quarter goals

2. Priya reads through carefully
3. Can add manager notes in text areas for each section

### Step 6: Manager Feedback Tab
1. Priya provides comprehensive feedback in form:

   **SECTION 1: PERFORMANCE SUMMARY**
   [Text area]
   Priya types: "Rahul demonstrated exceptional performance during Q1 2026, consistently exceeding expectations across technical execution, collaboration, and professional development. API optimization project delivered measurable business value (35% improvement, $800/month savings). Strong mentoring impact on junior developers."

   **SECTION 2: STRENGTHS**
   Priya lists 5 key strengths with examples:
   1. Technical Excellence & Problem-Solving
   2. Initiative & Ownership
   3. Collaboration & Mentoring
   4. Continuous Learning
   5. Results Orientation

   **SECTION 3: AREAS FOR IMPROVEMENT**
   Priya identifies development areas:
   1. Strategic Communication
   2. System Architecture & Design
   3. Delegation & Leadership
   4. Work-Life Balance

   **SECTION 4: GOAL-SPECIFIC FEEDBACK**
   - Goal 1: Outstanding execution, exceeded target
   - Goal 2: On track, good prioritization
   - Goal 3: Exceeds expectations, measurable impact

   **SECTION 5: RECOMMENDATIONS & NEXT STEPS**
   - Tech lead role on Q2 microservices project
   - Architecture mentorship
   - Leadership development opportunities

2. Priya completes all sections

### Step 7: Final Review & Rating Tab
1. Final rating form loads:

   **Select Overall Performance Rating:**
   ( ) 1 - Unsatisfactory
   ( ) 2 - Needs Improvement
   ( ) 3 - Meets Expectations
   (●) 4 - Exceeds Expectations
   ( ) 5 - Outstanding

2. Priya selects: 4 - Exceeds Expectations

3. **Rating Justification:** [Required text area]
   Priya types: "Rating of 4 based on: Exceeded primary goal target (35% vs 30%), measurable business impact ($800/month savings), strong mentoring results, additional contributions beyond goals, completed ahead of schedule. Aligns with employee self-rating showing strong self-awareness."

4. **Compensation Recommendations:**
   - ☑️ Merit increase: 5-7%
   - ☑️ Bonus: $2,500
   - ☐ Promotion (track for next cycle)

5. **Next Period Goals:**
   Priya types recommended Q2 goals

6. **Manager Certification:**
   ☑️ All required checkboxes completed

7. Priya clicks "Submit Final Review"

### Step 8: Confirmation and Submission
1. Confirmation dialog appears
2. Priya confirms submission
3. PUT request sent to `/api/performance-reviews/3001`
4. Backend updates PerformanceReview table:
   - ManagerFeedback: [Full feedback JSON]
   - ManagerRating: 4
   - ReviewedBy: 502
   - ReviewCompletedDate: 2026-03-21 16:45:00
   - Status: "Completed"

5. Backend creates notification for Rahul:
   - Type: "PerformanceReviewCompleted"
   - Message: "Your Q1 2026 Performance Review has been completed. Rating: 4 - Exceeds Expectations"

6. Backend creates notification for HR about compensation recommendations

7. Backend creates audit log entry:
   - Action: "PerformanceReviewCompleted"
   - Details: "Completed review for Rahul Sharma. Rating: 4"

8. Success message shown to Priya
9. Review marked as completed in dashboard

---

## **PHASE 10: Employee Views Final Performance Review**

### Step 1: Employee Receives Notification
1. March 21, 2026 evening - Rahul logs in
2. Backend creates audit log
3. Sees notification: "Your Q1 2026 Performance Review has been completed. Rating: 4 - Exceeds Expectations"
4. Clicks notification
5. Redirected to Performance Review Results page

### Step 2: Performance Review Results Page
1. Results page loads with overall rating prominently displayed:

   **OVERALL RATING: 4 - EXCEEDS EXPECTATIONS ⭐⭐⭐⭐**
   
   **Rating Comparison:**
   - Your Self-Rating: 4
   - Manager's Rating: 4
   - ✓ Ratings aligned

2. **Summary Tab** shows:
   - Goals achievement summary
   - Key strengths (5 listed)
   - Development areas (4 listed)
   - Business impact highlights
   - Compensation recommendations: Merit increase + Bonus
   - Next quarter focus areas

3. **Manager Feedback Tab** displays all detailed feedback sections

4. **Goals Performance Tab** shows rating for each goal

5. **Self-Assessment Tab** shows Rahul's original submission with manager's response notes

6. **Next Steps Tab** shows:
   - Immediate actions
   - Career development plan
   - Q2 proposed goals (approved by manager)
   - Support committed by manager

### Step 3: Review Acknowledgment
1. Rahul clicks "Acknowledge Review" button
2. Acknowledgment dialog appears:
   
   "By acknowledging, you confirm you have read and understood the review."
   
   **Optional: Add Your Response**
   [Text area]
   
3. Rahul types: "Thank you for the detailed feedback. I appreciate the recognition and guidance. Looking forward to the Q2 opportunities, especially the tech lead role."

4. Clicks "Acknowledge Review"

5. POST request to `/api/performance-reviews/3001/acknowledge`

6. Backend updates:
   - AcknowledgedBy: 501
   - AcknowledgedDate: 2026-03-21 18:30:00
   - EmployeeResponse: "Thank you for the detailed feedback..."
   - Status: "CompletedAndAcknowledged"

7. Notification sent to Priya about acknowledgment

8. Audit log entry created

9. Success message: "Performance Review Acknowledged ✓"

### Step 4: Dashboard Updates
1. Rahul returns to dashboard
2. "My Reviews" section shows:
   - Q1 2026 Review: COMPLETED ✓
   - Rating: 4 - Exceeds Expectations
   - Acknowledged: March 21, 2026

3. Performance summary widget shows:
   - Latest Rating: 4 ⭐⭐⭐⭐
   - Career Path: On track for Tech Lead
   - Next Review: Q2 2026

4. Goals dashboard shows updated completion status



## **PHASE 11: Admin Views Reports and Analytics (Enhanced)**

### Step 1: Admin Accesses Analytics Dashboard
1. April 1, 2026 - Admin logs into system
2. Backend creates audit log for login
3. Navigates to "Analytics & Reporting"
4. Enhanced analytics dashboard loads with comprehensive data:

   **PERFORMANCE ANALYTICS DASHBOARD**
   
   **Review Cycle Selection:**
   [Dropdown: Q1 2026 Performance Review] (selected)
   [Date Range: Jan 1 - Mar 31, 2026]
   
   **Key Metrics Overview:**
   
   [Card Layout with visual indicators]
   
   **Completion Metrics:**
   - Total Employees: 150
   - Self-Assessments Submitted: 150 (100%)
   - Manager Reviews Completed: 148 (99%)
   - Reviews Acknowledged: 146 (97%)
   - Pending: 2 (1%)
   
   **Performance Distribution:**
   - Outstanding (5): 12 employees (8%)
   - Exceeds Expectations (4): 58 employees (39%)
   - Meets Expectations (3): 72 employees (48%)
   - Needs Improvement (2): 7 employees (5%)
   - Unsatisfactory (1): 1 employee (1%)
   - Average Rating: 3.6
   
   **Goal Metrics:**
   - Total Goals Set: 450
   - Goals Completed: 187 (42%)
   - Goals In Progress: 253 (56%)
   - Goals Pending Approval: 10 (2%)
   - **Goals Pending Completion Approval: 45 (10%)** ← NEW METRIC
   - Goal Completion Rate (including pending): 52%
   - Average Goals per Employee: 3.0
   
   **Evidence Metrics** (NEW SECTION - SIMPLIFIED):
   - Goals Requiring Evidence: 450 (100% - per review cycle setting)
   - Evidence Links Submitted: 187 (100% of completed goals)
   - Evidence Links Verified: 187 (100%)
   - Average Verification Time: 1.8 days
   
   **Timeline Metrics:**
   - Average Self-Assessment Time: 30 minutes
   - Average Manager Review Time: 75 minutes
   - Average Time to Complete Review: 8 days
   - Average Goal Completion Approval Time: 4.1 days
   - Timeliest Reviews: Engineering (5 days avg)
   
   **Compensation Recommendations:**
   - Merit Increases Recommended: 128 employees (85%)
   - Bonuses Recommended: 42 employees (28%)
   - Promotions Recommended: 8 employees (5%)

2. Multiple visualization sections with interactive charts

### Step 2: Department Performance Comparison
1. Admin scrolls to department breakdown section:

   **DEPARTMENT PERFORMANCE BREAKDOWN**
   
   [Interactive table with sortable columns]
   
   | Department | Employees | Avg Rating | Completion Rate | Goal Achievement | Evidence Quality | Top Performer |
   |-----------|-----------|-----------|----------------|-----------------|----------------|--------------|
   | Engineering | 45 | 4.1 | 100% | 92% | Excellent (100% verified) | Rahul Sharma (4) |
   | Marketing | 30 | 3.6 | 100% | 85% | Good (98% verified) | Sarah Lee (5) |
   | Sales | 35 | 3.4 | 97% | 78% | Good (95% verified) | John Chen (4) |
   | Product | 20 | 3.8 | 100% | 88% | Excellent (100% verified) | Emma Davis (5) |
   | Operations | 20 | 3.5 | 100% | 82% | Good (97% verified) | Mike Wilson (4) |

### Step 3: Goal Completion Analysis (Enhanced with Evidence Tracking)
1. Admin clicks on "Goal Analytics" tab
2. Detailed goal analysis page loads:

   **GOAL ANALYTICS**
   
   **Goal Status Distribution:**
   [Pie chart]
   - Completed: 187 (42%)
   - In Progress: 253 (56%)
   - **Pending Completion Approval: 45 (10%)** ← NEW
   - Pending Manager Approval: 10 (2%)
   
   **Goal Completion Workflow Metrics** (NEW):
   
   **Completion Approval Funnel:**
   1. Goals Marked Complete by Employees: 232
   2. Evidence Links Submitted: 232 (100%)
   3. Awaiting Manager Review: 45 (19%)
   4. Manager Approved: 187 (81%)
   5. Manager Requested Additional Evidence: 12 (5%)
   6. Manager Rejected: 0 (0%)
   
   **Average Time in Each Stage:**
   - Employee marks complete → Evidence link submission: < 1 hour (same action)
   - Evidence submission → Manager review start: 2.3 days
   - Manager review start → Approval decision: 1.8 days
   - **Total average approval time: 4.1 days**
   
   **Evidence Link Quality Metrics** (NEW - SIMPLIFIED):
   
   **Evidence Link Verification:**
   - Evidence links verified: 187 (100%)
   - Evidence links rejected: 0 (0%)
   - Additional evidence requested: 12 (6%)
   - Average verification time: 1.8 days
   
   **Top Evidence Link Domains:**
   1. drive.google.com (52%)
   2. github.com (18%)
   3. confluence.company.com (15%)
   4. grafana.company.com (8%)
   5. Other (7%)

### Step 4: Audit Log Analysis (NEW FEATURE)
1. Admin clicks "Audit Logs" from analytics menu
2. Comprehensive audit log analysis page loads:

   **AUDIT LOG ANALYTICS**
   
   **System Activity Overview (Q1 2026):**
   
   **Total Events Logged:** 12,847
   
   **Activity by Type:**
   [Bar chart showing event counts]
   - Logins: 4,523 (35%)
   - Goal Actions: 2,890 (22%)
   - Review Actions: 1,678 (13%)
   - **Goal Completion Approvals: 421 (3%)** ← NEW
   - Notification Actions: 2,156 (17%)
   - Evidence Link Submissions: 232 (2%)
   - Admin Actions: 234 (2%)
   - Other: 713 (6%)
   
   **Goal-Related Audit Events** (NEW DETAILED VIEW):
   
   | Event Type | Count | Notable Patterns |
   |-----------|-------|------------------|
   | GoalCreated | 450 | Spike in mid-January |
   | GoalApproved | 440 | Most within 2 days of creation |
   | GoalCompletionSubmitted | 232 | Peak in late March |
   | GoalCompletionApproved | 187 | 81% approval rate |
   | GoalCompletionRejected | 0 | No rejections |
   | AdditionalEvidenceRequested | 12 | 5% of submissions |
   | EvidenceLinkSubmitted | 232 | One link per completed goal |
   | EvidenceLinkVerified | 187 | 100% verification rate |
   
   **Approval Workflow Patterns:**
   - Average time: Goal creation → Manager approval: 1.2 days
   - Average time: Completion submission → Manager approval: 4.1 days
   - Fastest approver: Priya Patel (Engineering) - 0.8 days avg
   - Slowest approver: Tom Anderson (Sales) - 8.5 days avg
   
   **Security & Access Events:**
   - Total Logins: 4,523
   - Failed Login Attempts: 23 (0.5%)
   - Password Changes: 156
   - Unusual Activity Detected: 0
   
   **Data Integrity:**
   - All transactions recorded: ✓
   - No data loss incidents: ✓
   - Backup completion rate: 100%
   - System uptime: 99.95%

### Step 5: Export Comprehensive Report
1. Admin clicks "Generate Comprehensive Report" button
2. Report configuration dialog appears:
   - Report Type: [Executive Summary / Detailed Analytics / Department Comparison / Goal Analysis / Audit Trail]
   - Scope: [Company-wide / Department / Individual]
   - Period: Q1 2026
   - Format: [PDF / Excel / CSV]
   - Include:
     - ☑️ Performance ratings distribution
     - ☑️ Goal completion statistics
     - ☑️ Evidence link verification data
     - ☑️ Timeline metrics
     - ☑️ Compensation recommendations
     - ☑️ Audit log summary
3. Admin selects "Executive Summary", "Company-wide", "PDF"
4. Clicks "Generate Report"
5. Backend processes:
   - Queries all relevant tables
   - Aggregates data
   - Calculates metrics
   - Generates visualizations
   - Creates PDF report with company branding
6. PDF downloads with comprehensive analytics for leadership review
7. Report saved to database with ReportID for future reference
8. Audit log entry created for report generation

---

This completes the revised, comprehensive workflow walkthrough with all the requested changes:

1. Admin sets password during user creation
2. Removed external credential delivery mentions
3. Removed temporary password change requirement
4. Evidence is only links (no documents)
5. Evidence only required at goal completion, not creation
6. Removed goal completion certificates
7. No evidence in self-assessment form
8. Removed milestones - simplified to single goal with single evidence link

The workflow now shows a streamlined process focusing on goal creation, manager approval, completion with single evidence link, and comprehensive auditing throughout.
