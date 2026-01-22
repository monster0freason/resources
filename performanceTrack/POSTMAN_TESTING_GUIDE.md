# Postman Testing Guide - All 47 APIs with Complete Details

## Setup Instructions

### 1. Import Collection
1. Open Postman
2. Click "Import" → Upload `PerformanceTrack_Postman.json`
3. Collection appears in left sidebar

### 2. Configure Variables
1. Right-click collection → Edit → Variables
2. Set `baseUrl` = `http://localhost:8080/api/v1`
3. Leave token variables empty (will fill after login)

---

## PHASE 1: AUTHENTICATION (3 APIs)

### API 1: Login - Admin
**Method:** POST  
**URL:** `{{baseUrl}}/auth/login`  
**Headers:**
```
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "email": "admin@company.com",
  "password": "admin123"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {...}
  }
}
```
**Action After:** Copy token → Set `adminToken` variable

---

### API 2: Login - Manager
**Method:** POST  
**URL:** `{{baseUrl}}/auth/login`  
**Headers:**
```
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "email": "priya@company.com",
  "password": "manager123"
}
```
**Expected Response:** Same as above  
**Action After:** Copy token → Set `managerToken` variable

---

### API 3: Login - Employee
**Method:** POST  
**URL:** `{{baseUrl}}/auth/login`  
**Headers:**
```
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "email": "rahul@company.com",
  "password": "employee123"
}
```
**Expected Response:** Same as above  
**Action After:** Copy token → Set `employeeToken` variable

---

## PHASE 2: ADMIN SETUP (5 APIs)

### API 4: Create Review Cycle
**Method:** POST  
**URL:** `{{baseUrl}}/review-cycles`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "title": "Q2 2026 Performance Review",
  "startDt": "2026-04-01",
  "endDt": "2026-06-30",
  "status": "ACTIVE",
  "reqCompAppr": true,
  "evReq": true
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Review cycle created",
  "data": {
    "cycleId": 2,
    "title": "Q2 2026 Performance Review",
    ...
  }
}
```
**Note:** Save `cycleId` = 2 (or whatever returned)

---

### API 5: Get All Review Cycles
**Method:** GET  
**URL:** `{{baseUrl}}/review-cycles`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:** List of all cycles

---

### API 6: Get Active Review Cycle
**Method:** GET  
**URL:** `{{baseUrl}}/review-cycles/active`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** Currently active cycle

---

### API 7: Create User
**Method:** POST  
**URL:** `{{baseUrl}}/users`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "name": "Amit Sharma",
  "email": "amit@company.com",
  "password": "amit123",
  "role": "EMPLOYEE",
  "dept": "Engineering",
  "mgrId": 2,
  "status": "ACTIVE"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "User created",
  "data": {
    "userId": 4,
    "name": "Amit Sharma",
    ...
  }
}
```
**Note:** Save `userId` = 4

---

### API 8: Get All Users
**Method:** GET  
**URL:** `{{baseUrl}}/users`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:** List of all users

---

## PHASE 3: EMPLOYEE CREATES GOAL (2 APIs)

### API 9: Create Goal
**Method:** POST  
**URL:** `{{baseUrl}}/goals`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "title": "Reduce API response time by 30%",
  "desc": "Optimize database queries and implement Redis caching",
  "cat": "TECHNICAL",
  "pri": "HIGH",
  "startDt": "2026-01-15",
  "endDt": "2026-03-15",
  "mgrId": 2
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal created",
  "data": {
    "goalId": 1,
    "status": "PENDING",
    ...
  }
}
```
**Note:** Save `goalId` = 1, status = "PENDING"

---

### API 10: Get All Goals
**Method:** GET  
**URL:** `{{baseUrl}}/goals`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** List with the goal just created

---

## PHASE 4: MANAGER APPROVES GOAL (3 APIs)

### API 11: Get Goal by ID
**Method:** GET  
**URL:** `{{baseUrl}}/goals/1`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
```
**Body:** None  
**Expected Response:** Goal details with status "PENDING"

---

### API 12: Approve Goal
**Method:** PUT  
**URL:** `{{baseUrl}}/goals/1/approve`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal approved",
  "data": {
    "goalId": 1,
    "status": "IN_PROGRESS",
    ...
  }
}
```
**Note:** Status changed to "IN_PROGRESS"

---

### API 13: Get Team Members
**Method:** GET  
**URL:** `{{baseUrl}}/users/2/team`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
```
**Body:** None  
**Expected Response:** List of manager's team members

---

## PHASE 5: EMPLOYEE TRACKS PROGRESS (2 APIs)

### API 14: Add Progress Update
**Method:** POST  
**URL:** `{{baseUrl}}/goals/1/progress`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "note": "Completed Redis caching setup. Seeing 15% improvement so far."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Progress added"
}
```

---

### API 15: Get Progress Updates
**Method:** GET  
**URL:** `{{baseUrl}}/goals/1/progress`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Progress retrieved",
  "data": "2026-01-22T10:30:00: Completed Redis caching setup..."
}
```

---

## PHASE 6: EMPLOYEE SUBMITS COMPLETION (1 API)

### API 16: Submit Goal Completion
**Method:** POST  
**URL:** `{{baseUrl}}/goals/1/submit-completion`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "evLink": "https://drive.google.com/folder/abc123",
  "linkDesc": "Performance dashboards showing 35% improvement, GitHub PRs, and documentation",
  "accessInstr": "Accessible to all @company.com emails",
  "compNotes": "Achieved 35% improvement, exceeding the 30% target. All metrics documented."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal completion submitted",
  "data": {
    "goalId": 1,
    "status": "PENDING_COMPLETION_APPROVAL",
    ...
  }
}
```
**Note:** Status = "PENDING_COMPLETION_APPROVAL"

---

## PHASE 7: MANAGER VERIFIES & APPROVES (2 APIs)

### API 17: Verify Evidence
**Method:** PUT  
**URL:** `{{baseUrl}}/goals/1/evidence/verify`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "status": "VERIFIED",
  "notes": "Evidence reviewed and confirmed. Documentation is complete and metrics show 35% improvement."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Evidence verified",
  "data": {
    "evidenceLinkVerificationStatus": "VERIFIED",
    ...
  }
}
```

---

### API 18: Approve Goal Completion
**Method:** POST  
**URL:** `{{baseUrl}}/goals/1/approve-completion`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "mgrComments": "Excellent work! You exceeded the target significantly."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal completion approved",
  "data": {
    "goalId": 1,
    "status": "COMPLETED",
    ...
  }
}
```
**Note:** Goal now COMPLETED!

---

## PHASE 8: PERFORMANCE REVIEW (4 APIs)

### API 19: Submit Self-Assessment
**Method:** POST  
**URL:** `{{baseUrl}}/performance-reviews`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "cycleId": 1,
  "selfAssmt": "{\"achievements\":\"Completed API optimization goal with 35% improvement\",\"challenges\":\"Redis configuration complexity\",\"learnings\":\"Caching strategies\"}",
  "selfRating": 4
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Self-assessment submitted",
  "data": {
    "reviewId": 1,
    "status": "SELF_ASSESSMENT_COMPLETED",
    ...
  }
}
```
**Note:** Save `reviewId` = 1

---

### API 20: Get All Reviews
**Method:** GET  
**URL:** `{{baseUrl}}/performance-reviews`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** List with review just created

---

### API 21: Get Review by ID
**Method:** GET  
**URL:** `{{baseUrl}}/performance-reviews/1`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** Review details

---

### API 22: Submit Manager Review
**Method:** PUT  
**URL:** `{{baseUrl}}/performance-reviews/1`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "mgrFb": "{\"strengths\":\"Technical excellence, exceeds targets\",\"improvements\":\"Can improve cross-team communication\"}",
  "mgrRating": 4,
  "ratingJust": "Exceeded expectations with 35% improvement vs 30% target",
  "compRec": "{\"meritIncrease\":\"5-7%\",\"bonus\":2500}",
  "nextGoals": "Lead microservices migration project in Q2"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Performance review submitted",
  "data": {
    "reviewId": 1,
    "status": "COMPLETED",
    ...
  }
}
```

---

### API 23: Acknowledge Review
**Method:** POST  
**URL:** `{{baseUrl}}/performance-reviews/1/acknowledge`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "response": "Thank you for the feedback. I appreciate the recognition and will work on improving communication. Looking forward to the microservices project!"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Review acknowledged",
  "data": {
    "status": "COMPLETED_AND_ACKNOWLEDGED",
    ...
  }
}
```

---

## PHASE 9: NOTIFICATIONS (3 APIs)

### API 24: Get All Notifications
**Method:** GET  
**URL:** `{{baseUrl}}/notifications`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** List of all notifications from previous actions

---

### API 25: Mark Notification as Read
**Method:** PUT  
**URL:** `{{baseUrl}}/notifications/1`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Notification marked as read"
}
```

---

### API 26: Mark All as Read
**Method:** PUT  
**URL:** `{{baseUrl}}/notifications/mark-all-read`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "All notifications marked as read"
}
```

---

## PHASE 10: FEEDBACK (2 APIs)

### API 27: Create Feedback
**Method:** POST  
**URL:** `{{baseUrl}}/feedback`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "goalId": 1,
  "comments": "Great work on the caching implementation! The 35% improvement is impressive.",
  "feedbackType": "POSITIVE"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Feedback created",
  "data": {
    "feedbackId": 1,
    ...
  }
}
```

---

### API 28: Get Feedback
**Method:** GET  
**URL:** `{{baseUrl}}/feedback?goalId=1`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** List of feedback for goal

---

## PHASE 11: ANALYTICS & REPORTS (7 APIs)

### API 29: Get Dashboard Metrics
**Method:** GET  
**URL:** `{{baseUrl}}/reports/dashboard`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Dashboard metrics retrieved",
  "data": {
    "totalGoals": 1,
    "completedGoals": 1,
    "inProgressGoals": 0,
    "completionRate": 100
  }
}
```
**Note:** Different data for Employee/Manager/Admin

---

### API 30: Get Goal Analytics
**Method:** GET  
**URL:** `{{baseUrl}}/reports/goal-analytics`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal analytics retrieved",
  "data": {
    "totalGoals": 1,
    "completed": 1,
    "inProgress": 0,
    "pending": 0,
    "completionRate": 100
  }
}
```

---

### API 31: Get Performance Summary
**Method:** GET  
**URL:** `{{baseUrl}}/reports/performance-summary?cycleId=1`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Performance summary retrieved",
  "data": {
    "totalReviews": 1,
    "avgSelfRating": 4.0,
    "avgManagerRating": 4.0,
    "cycleId": 1
  }
}
```

---

### API 32: Get Department Performance
**Method:** GET  
**URL:** `{{baseUrl}}/reports/department-performance`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Department performance retrieved",
  "data": [
    {
      "department": "Engineering",
      "employeeCount": 2,
      "totalGoals": 1,
      "completedGoals": 1,
      "completionRate": 100
    }
  ]
}
```

---

### API 33: Generate Report
**Method:** POST  
**URL:** `{{baseUrl}}/reports/generate`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "scope": "Department",
  "metrics": "{\"goalCompletion\":100,\"avgRating\":4.0}",
  "format": "PDF"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Report generated",
  "data": {
    "reportId": 1,
    "filePath": "/reports/1737542400000.pdf"
  }
}
```

---

### API 34: Get All Reports
**Method:** GET  
**URL:** `{{baseUrl}}/reports`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:** List of all generated reports

---

### API 35: Get Report by ID
**Method:** GET  
**URL:** `{{baseUrl}}/reports/1`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:** Report details

---

## PHASE 12: AUDIT LOGS (2 APIs)

### API 36: Get Audit Logs
**Method:** GET  
**URL:** `{{baseUrl}}/audit-logs`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Audit logs retrieved",
  "data": [
    {
      "auditId": 1,
      "action": "LOGIN",
      "user": {...},
      "timestamp": "2026-01-22T10:00:00"
    },
    ...
  ]
}
```

---

### API 37: Export Audit Logs
**Method:** POST  
**URL:** `{{baseUrl}}/audit-logs/export`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "format": "CSV"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Audit logs export initiated",
  "data": "/exports/audit_logs_1737542400000.csv"
}
```

---

## PHASE 13: REMAINING ADMIN OPERATIONS (4 APIs)

### API 38: Get User by ID
**Method:** GET  
**URL:** `{{baseUrl}}/users/3`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
```
**Body:** None  
**Expected Response:** User details (Rahul - Employee)

---

### API 39: Update User
**Method:** PUT  
**URL:** `{{baseUrl}}/users/4`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "name": "Amit Sharma - Senior Engineer",
  "role": "EMPLOYEE",
  "dept": "Engineering",
  "mgrId": 2,
  "status": "ACTIVE"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "User updated",
  "data": {
    "userId": 4,
    "name": "Amit Sharma - Senior Engineer",
    ...
  }
}
```

---

### API 40: Get Review Cycle by ID
**Method:** GET  
**URL:** `{{baseUrl}}/review-cycles/1`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:** Cycle details

---

### API 41: Update Review Cycle
**Method:** PUT  
**URL:** `{{baseUrl}}/review-cycles/2`  
**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "title": "Q2 2026 Performance Review - Final",
  "startDt": "2026-04-01",
  "endDt": "2026-06-30",
  "status": "CLOSED",
  "reqCompAppr": true,
  "evReq": true
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Review cycle updated",
  "data": {
    "cycleId": 2,
    "status": "CLOSED",
    ...
  }
}
```

---

## PHASE 14: OPTIONAL WORKFLOW VARIATIONS (6 APIs)

### API 42: Update Goal (After Manager Requests Changes)
**Method:** PUT  
**URL:** `{{baseUrl}}/goals/1`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "title": "Reduce API response time by 35%",
  "desc": "Optimize database queries, implement Redis caching, and improve connection pooling",
  "cat": "TECHNICAL",
  "pri": "HIGH",
  "startDt": "2026-01-15",
  "endDt": "2026-03-15",
  "mgrId": 2
}
```
**Expected Response:** Updated goal
**Note:** Only works if goal has `requestChanges` = true

---

### API 43: Request Changes to Goal
**Method:** PUT  
**URL:** `{{baseUrl}}/goals/1/request-changes`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "comments": "Please add more details about the caching strategy and expected timeline for each phase."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Changes requested",
  "data": {
    "requestChanges": true,
    ...
  }
}
```
**Note:** Use this instead of "Approve Goal" to request modifications

---

### API 44: Request Additional Evidence
**Method:** POST  
**URL:** `{{baseUrl}}/goals/1/request-additional-evidence`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "reason": "Please provide the test coverage report and load testing results to verify the 35% improvement."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Additional evidence requested",
  "data": {
    "evidenceVerificationStatus": "ADDITIONAL_EVIDENCE_REQUIRED",
    ...
  }
}
```

---

### API 45: Reject Goal Completion
**Method:** POST  
**URL:** `{{baseUrl}}/goals/1/reject-completion`  
**Headers:**
```
Authorization: Bearer {{managerToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "reason": "The evidence provided does not clearly demonstrate the 30% improvement target. Please provide more detailed metrics."
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal completion rejected",
  "data": {
    "status": "IN_PROGRESS",
    "completionApprovalStatus": "REJECTED",
    ...
  }
}
```
**Note:** Use this instead of "Approve Completion" to reject

---

### API 46: Update Self-Assessment Draft
**Method:** PUT  
**URL:** `{{baseUrl}}/performance-reviews/1/draft`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "cycleId": 1,
  "selfAssmt": "{\"achievements\":\"Completed API optimization goal with 35% improvement, mentored 2 junior developers\",\"challenges\":\"Redis configuration\",\"learnings\":\"Advanced caching\"}",
  "selfRating": 4
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Draft updated",
  "data": {
    "reviewId": 1,
    ...
  }
}
```
**Note:** Can update before manager reviews

---

### API 47: Delete Goal
**Method:** DELETE  
**URL:** `{{baseUrl}}/goals/1`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Goal deleted"
}
```
**Note:** Soft delete - marks as REJECTED

---

## PHASE 15: FINAL - CHANGE PASSWORD & LOGOUT (2 APIs)

### API 48: Change Password
**Method:** PUT  
**URL:** `{{baseUrl}}/auth/change-password`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
Content-Type: application/json
```
**Body (raw JSON):**
```json
{
  "oldPassword": "employee123",
  "newPassword": "newpassword456"
}
```
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Password changed successfully"
}
```

---

### API 49: Logout
**Method:** POST  
**URL:** `{{baseUrl}}/auth/logout`  
**Headers:**
```
Authorization: Bearer {{employeeToken}}
```
**Body:** None  
**Expected Response:**
```json
{
  "status": "success",
  "msg": "Logout successful"
}
```

---

## Quick Reference: All 47 APIs by Phase

**Phase 1: Auth (3)** - Login x3  
**Phase 2: Setup (5)** - Create cycle, Get cycles x3, Create user, Get users  
**Phase 3: Goal Creation (2)** - Create goal, Get goals  
**Phase 4: Approval (3)** - Get goal, Approve, Get team  
**Phase 5: Progress (2)** - Add progress, Get progress  
**Phase 6: Completion (1)** - Submit completion  
**Phase 7: Verification (2)** - Verify evidence, Approve completion  
**Phase 8: Review (4)** - Submit self, Get reviews x2, Submit manager, Acknowledge  
**Phase 9: Notifications (3)** - Get all, Mark read, Mark all read  
**Phase 10: Feedback (2)** - Create, Get  
**Phase 11: Analytics (7)** - Dashboard, Analytics, Summary, Dept perf, Generate, Get reports x2  
**Phase 12: Audit (2)** - Get logs, Export  
**Phase 13: Admin Ops (4)** - Get user, Update user, Get cycle, Update cycle  
**Phase 14: Variations (6)** - Update goal, Request changes, Request evidence, Reject completion, Update draft, Delete  
**Phase 15: Final (2)** - Change password, Logout  

---

## Testing Checklist

### Must Test (Core Flow) ✓
- [ ] Phase 1-9 (Login through Notifications)
- [ ] Phase 11 (Analytics - at least 2 APIs)
- [ ] Phase 12 (Audit logs)

### Should Test (Admin Functions) ✓
- [ ] Phase 2 (Review cycle management)
- [ ] Phase 13 (User/Cycle updates)

### Optional Test (Variations) ✓
- [ ] Phase 14 (Alternative workflows)

### Final Test ✓
- [ ] Phase 15 (Password & Logout)

---

## Common Response Codes

- **200 OK** - Success
- **201 Created** - Resource created
- **400 Bad Request** - Invalid data or workflow violation
- **401 Unauthorized** - Missing/invalid token
- **403 Forbidden** - Wrong role
- **404 Not Found** - Resource not found
- **500 Internal Error** - Server error

---

## Pro Tips

1. **Save IDs:** Note goalId, reviewId, userId, cycleId from responses
2. **Test Sequentially:** Follow phases 1-15 in order
3. **Check Notifications:** After each action, check notifications
4. **Use Correct Tokens:** Admin for admin ops, Manager for approvals, Employee for goals
5. **Verify Status:** Check object status after each state change

---

**All 47 APIs covered! Follow phases 1-15 for complete testing. 🚀**

