# PerformanceTrack System - Complete API List with Filtering Details

## **1. AUTHENTICATION APIs**
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `PUT /api/users/{userId}/change-password` - Change password

---

## **2. USER MANAGEMENT APIs**
- `GET /api/users?role={role}&department={department}&status={status}&search={name/email}&page={page}&limit={limit}` - Get all users with filters
- `GET /api/users/{userId}` - Get user by ID
- `POST /api/users` - Create new user (Admin only)
- `PUT /api/users/{userId}` - Update user details
- `GET /api/users/{managerId}/team?status={status}` - Get manager's team members
- `GET /api/users/managers?department={department}` - Get all managers (for dropdown)
- `GET /api/users/departments` - Get all departments list (for filters)

---

## **3. REVIEW CYCLE APIs**
- `GET /api/review-cycles?status={status}&startDate={date}&endDate={date}&page={page}&limit={limit}` - Get all review cycles with filters
- `GET /api/review-cycles/{cycleId}` - Get review cycle by ID
- `POST /api/review-cycles` - Create new review cycle (Admin only)
- `PUT /api/review-cycles/{cycleId}` - Update review cycle
- `GET /api/review-cycles/active` - Get active review cycle

---

## **4. GOAL MANAGEMENT APIs**

### 4.1 Goal CRUD & Filtering
- `GET /api/goals?userId={userId}&managerId={managerId}&status={status}&category={category}&priority={priority}&startDate={date}&endDate={date}&search={keyword}&page={page}&limit={limit}&sort={field}&order={asc/desc}` - Get goals with comprehensive filters
- `GET /api/goals/{goalId}` - Get goal by ID
- `POST /api/goals` - Create new goal (Employee)
- `PUT /api/goals/{goalId}` - Update goal (Employee - only when changes requested)
- `DELETE /api/goals/{goalId}` - Delete goal (soft delete)

### 4.2 Goal Filtering Helpers
- `GET /api/goals/categories` - Get all goal categories (for filter dropdown)
- `GET /api/goals/statuses` - Get all goal statuses (for filter dropdown)
- `GET /api/goals/priorities` - Get all priorities (for filter dropdown)

### 4.3 Goal Approval (Manager)
- `PUT /api/goals/{goalId}/approve` - Approve goal (Manager)
- `PUT /api/goals/{goalId}/request-changes` - Request changes to goal (Manager)

### 4.4 Goal Completion & Evidence
- `POST /api/goals/{goalId}/submit-completion` - Submit goal completion with evidence link (Employee)
- `PUT /api/goals/{goalId}/evidence/verify` - Verify evidence link (Manager)
- `POST /api/goals/{goalId}/approve-completion` - Approve goal completion (Manager)
- `POST /api/goals/{goalId}/request-additional-evidence` - Request additional evidence (Manager)
- `POST /api/goals/{goalId}/reject-completion` - Reject goal completion (Manager)

### 4.5 Goal Completion Approvals (Manager Dashboard)
- `GET /api/goals/pending-completion-approval?employeeId={userId}&submittedDateFrom={date}&submittedDateTo={date}&priority={priority}&category={category}&page={page}&limit={limit}` - Get goals pending completion approval with filters

### 4.6 Goal Progress
- `POST /api/goals/{goalId}/progress` - Add progress update (Employee)
- `GET /api/goals/{goalId}/progress` - Get all progress updates

---

## **5. PERFORMANCE REVIEW APIs**

### 5.1 Self-Assessment & Filtering
- `GET /api/performance-reviews?cycleId={cycleId}&userId={userId}&status={status}&selfRating={rating}&managerRating={rating}&department={department}&acknowledgedStatus={yes/no}&page={page}&limit={limit}&sort={field}&order={asc/desc}` - Get performance reviews with filters
- `GET /api/performance-reviews/{reviewId}` - Get review by ID
- `POST /api/performance-reviews` - Submit self-assessment (Employee)
- `PUT /api/performance-reviews/{reviewId}` - Update self-assessment (Employee - draft only)

### 5.2 Manager Review Dashboard
- `GET /api/performance-reviews/pending-manager-review?cycleId={cycleId}&employeeId={userId}&department={department}&selfRating={rating}&submittedDateFrom={date}&submittedDateTo={date}&page={page}&limit={limit}` - Get reviews pending manager action with filters

### 5.3 Manager Review Submission
- `PUT /api/performance-reviews/{reviewId}` - Submit manager review and rating (Manager)

### 5.4 Review Acknowledgment
- `POST /api/performance-reviews/{reviewId}/acknowledge` - Acknowledge review (Employee)

### 5.5 Review Filtering Helpers
- `GET /api/performance-reviews/statuses` - Get all review statuses (for filter dropdown)
- `GET /api/performance-reviews/ratings` - Get rating scale (1-5) (for filter dropdown)

---

## **6. FEEDBACK APIs**
- `GET /api/feedback?goalId={goalId}&reviewId={reviewId}&userId={userId}&feedbackType={type}&dateFrom={date}&dateTo={date}&page={page}&limit={limit}` - Get feedback with filters
- `POST /api/feedback` - Create feedback (used internally by goal/review processes)
- `GET /api/feedback/types` - Get all feedback types (for filter dropdown)

---

## **7. NOTIFICATION APIs**
- `GET /api/notifications?userId={userId}&status={unread/read}&type={type}&priority={priority}&dateFrom={date}&dateTo={date}&actionRequired={true/false}&page={page}&limit={limit}&sort={createdDate}&order={desc}` - Get notifications with filters
- `PUT /api/notifications/{notificationId}` - Mark notification as read
- `PUT /api/notifications/mark-all-read?userId={userId}` - Mark all notifications as read for user
- `GET /api/notifications/types` - Get all notification types (for filter dropdown)
- `GET /api/notifications/count?userId={userId}&status={unread}` - Get unread notification count

---

## **8. AUDIT LOG APIs**
- `GET /api/audit-logs?userId={userId}&action={action}&status={status}&startDate={date}&endDate={date}&relatedEntityType={type}&relatedEntityId={id}&ipAddress={ip}&page={page}&limit={limit}&sort={timestamp}&order={desc}` - Get audit logs with comprehensive filters (Admin only)
- `POST /api/audit-logs/export?format={pdf/csv/excel}&userId={userId}&action={action}&startDate={date}&endDate={date}` - Export audit logs with filters (Admin only)
- `GET /api/audit-logs/actions` - Get all action types (for filter dropdown)
- `GET /api/audit-logs/entity-types` - Get all entity types (for filter dropdown)

---

## **9. ANALYTICS & REPORTING APIs**

### 9.1 Dashboard Metrics (with filters)
- `GET /api/reports/dashboard?cycleId={cycleId}&department={department}&role={role}` - Get dashboard summary metrics (role-based with filters)
- `GET /api/reports/performance-summary?cycleId={cycleId}&department={department}&rating={rating}&status={status}` - Get performance review summary with filters
- `GET /api/reports/goal-analytics?cycleId={cycleId}&department={department}&category={category}&status={status}&priority={priority}` - Get goal completion analytics with filters
- `GET /api/reports/department-performance?cycleId={cycleId}&department={department}` - Get department-wise performance with filters

### 9.2 Advanced Analytics
- `GET /api/reports/goal-completion-funnel?cycleId={cycleId}&department={department}` - Get goal completion approval funnel data
- `GET /api/reports/evidence-analytics?cycleId={cycleId}&department={department}` - Get evidence link verification analytics
- `GET /api/reports/timeline-metrics?cycleId={cycleId}&department={department}` - Get timeline and efficiency metrics

### 9.3 Report Generation
- `POST /api/reports/generate` - Generate custom report (Admin/Manager) with filters in request body
  ```json
  {
    "reportType": "executive_summary",
    "scope": "department",
    "department": "Engineering",
    "cycleId": 101,
    "format": "pdf",
    "filters": {
      "rating": [4, 5],
      "status": "completed"
    }
  }
  ```
- `GET /api/reports/{reportId}` - Download generated report
- `GET /api/reports?generatedBy={userId}&dateFrom={date}&dateTo={date}&format={format}&page={page}&limit={limit}` - Get list of generated reports with filters

---

## **10. SYSTEM CONFIGURATION APIs** (for dropdowns and filters)

### 10.1 Dropdown Data APIs
- `GET /api/config/roles` - Get all user roles
- `GET /api/config/departments` - Get all departments
- `GET /api/config/goal-categories` - Get all goal categories
- `GET /api/config/goal-priorities` - Get all goal priorities
- `GET /api/config/goal-statuses` - Get all goal statuses
- `GET /api/config/review-statuses` - Get all review statuses
- `GET /api/config/ratings` - Get rating scale options
- `GET /api/config/notification-types` - Get all notification types
- `GET /api/config/feedback-types` - Get all feedback types
- `GET /api/config/audit-action-types` - Get all audit action types

---

## **TOTAL: 67 APIs**

### **Breakdown by Module:**
- Authentication: 3 APIs
- User Management: 7 APIs
- Review Cycles: 5 APIs
- Goals: 20 APIs (includes filtering helpers)
- Performance Reviews: 10 APIs (includes filtering helpers)
- Feedback: 3 APIs
- Notifications: 5 APIs
- Audit Logs: 4 APIs
- Analytics & Reporting: 10 APIs
- System Configuration: 10 APIs (dropdown/filter data)

---

## **Common Query Parameters Pattern:**

### **Filtering:**
- `{field}={value}` - Exact match
- `{field}From={value}` & `{field}To={value}` - Range filter
- `search={keyword}` - Text search
- `status={status}` - Status filter
- `department={dept}` - Department filter
- `userId={id}` - User filter
- `managerId={id}` - Manager filter
- `cycleId={id}` - Review cycle filter

### **Pagination:**
- `page={number}` - Page number (default: 1)
- `limit={number}` - Items per page (default: 20)

### **Sorting:**
- `sort={field}` - Sort by field (default: depends on endpoint)
- `order={asc|desc}` - Sort order (default: desc)

### **Response Format:**
All list APIs return standardized response:
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 150,
    "totalPages": 8
  },
  "filters": {
    "applied": {...},
    "available": {...}
  }
}
```

---

## **Filter Implementation Examples:**

### **Example 1: Manager viewing team goals**
```
GET /api/goals?managerId=502&status=Pending,InProgress&priority=High&sort=endDate&order=asc&page=1&limit=10
```

### **Example 2: Admin viewing audit logs**
```
GET /api/audit-logs?action=GoalCompletionApproved&startDate=2026-01-01&endDate=2026-03-31&department=Engineering&page=1&limit=50&sort=timestamp&order=desc
```

### **Example 3: Employee viewing notifications**
```
GET /api/notifications?userId=501&status=unread&type=GoalApproved,GoalCompletionApproved&actionRequired=true&sort=priority&order=desc
```

### **Example 4: Admin generating analytics**
```
GET /api/reports/goal-analytics?cycleId=101&department=Engineering&category=Technical&status=Completed&priority=High
```
