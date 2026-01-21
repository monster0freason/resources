# PerformanceTrack System - Complete API List (Minimum Required)

## **1. AUTHENTICATION APIs**
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `PUT /api/users/{userId}/change-password` - Change password

---

## **2. USER MANAGEMENT APIs**
- `GET /api/users` - Get all users (with filters: role, department, status)
- `GET /api/users/{userId}` - Get user by ID
- `POST /api/users` - Create new user (Admin only)
- `PUT /api/users/{userId}` - Update user details
- `GET /api/users/{userId}/team` - Get manager's team members

---

## **3. REVIEW CYCLE APIs**
- `GET /api/review-cycles` - Get all review cycles
- `GET /api/review-cycles/{cycleId}` - Get review cycle by ID
- `POST /api/review-cycles` - Create new review cycle (Admin only)
- `PUT /api/review-cycles/{cycleId}` - Update review cycle
- `GET /api/review-cycles/active` - Get active review cycle

---

## **4. GOAL MANAGEMENT APIs**

### 4.1 Goal CRUD
- `GET /api/goals` - Get goals (with filters: userId, managerId, status, category, priority)
- `GET /api/goals/{goalId}` - Get goal by ID
- `POST /api/goals` - Create new goal (Employee)
- `PUT /api/goals/{goalId}` - Update goal (Employee - only when changes requested)
- `DELETE /api/goals/{goalId}` - Delete goal (soft delete)

### 4.2 Goal Approval (Manager)
- `PUT /api/goals/{goalId}/approve` - Approve goal (Manager)
- `PUT /api/goals/{goalId}/request-changes` - Request changes to goal (Manager)

### 4.3 Goal Completion & Evidence
- `POST /api/goals/{goalId}/submit-completion` - Submit goal completion with evidence link (Employee)
- `PUT /api/goals/{goalId}/evidence/verify` - Verify evidence link (Manager)
- `POST /api/goals/{goalId}/approve-completion` - Approve goal completion (Manager)
- `POST /api/goals/{goalId}/request-additional-evidence` - Request additional evidence (Manager)
- `POST /api/goals/{goalId}/reject-completion` - Reject goal completion (Manager)

### 4.4 Goal Progress
- `POST /api/goals/{goalId}/progress` - Add progress update (Employee)
- `GET /api/goals/{goalId}/progress` - Get all progress updates

---

## **5. PERFORMANCE REVIEW APIs**

### 5.1 Self-Assessment
- `GET /api/performance-reviews` - Get performance reviews (with filters: cycleId, userId, status)
- `GET /api/performance-reviews/{reviewId}` - Get review by ID
- `POST /api/performance-reviews` - Submit self-assessment (Employee)
- `PUT /api/performance-reviews/{reviewId}` - Update self-assessment (Employee - draft only)

### 5.2 Manager Review
- `PUT /api/performance-reviews/{reviewId}` - Submit manager review and rating (Manager)

### 5.3 Review Acknowledgment
- `POST /api/performance-reviews/{reviewId}/acknowledge` - Acknowledge review (Employee)

---

## **6. FEEDBACK APIs**
- `GET /api/feedback` - Get feedback (with filters: goalId, reviewId, userId)
- `POST /api/feedback` - Create feedback (used internally by goal/review processes)

---

## **7. NOTIFICATION APIs**
- `GET /api/notifications` - Get notifications (with filters: userId, status, type, priority)
- `PUT /api/notifications/{notificationId}` - Mark notification as read
- `PUT /api/notifications/mark-all-read` - Mark all notifications as read for user

---

## **8. AUDIT LOG APIs**
- `GET /api/audit-logs` - Get audit logs (with filters: userId, action, dateRange, status) (Admin only)
- `POST /api/audit-logs/export` - Export audit logs (Admin only)

---

## **9. ANALYTICS & REPORTING APIs**

### 9.1 Dashboard Metrics
- `GET /api/reports/dashboard` - Get dashboard summary metrics (role-based)
- `GET /api/reports/performance-summary` - Get performance review summary (with filters: cycleId, department)
- `GET /api/reports/goal-analytics` - Get goal completion analytics
- `GET /api/reports/department-performance` - Get department-wise performance

### 9.2 Report Generation
- `POST /api/reports/generate` - Generate custom report (Admin/Manager)
- `GET /api/reports/{reportId}` - Download generated report

---

## **TOTAL: 46 APIs**

### **Breakdown by Module:**
- Authentication: 3 APIs
- User Management: 5 APIs
- Review Cycles: 5 APIs
- Goals: 13 APIs
- Performance Reviews: 6 APIs
- Feedback: 2 APIs
- Notifications: 3 APIs
- Audit Logs: 2 APIs
- Analytics & Reporting: 7 APIs

---

## **Key Query Parameters Used Across APIs:**

### Common Filters:
- `userId` - Filter by user
- `managerId` - Filter by manager
- `cycleId` - Filter by review cycle
- `status` - Filter by status
- `startDate` / `endDate` - Date range filter
- `department` - Filter by department
- `role` - Filter by role
- `priority` - Filter by priority
- `category` - Filter by category

### Pagination:
- `page` - Page number
- `limit` - Items per page
- `sort` - Sort field
- `order` - Sort order (asc/desc)
