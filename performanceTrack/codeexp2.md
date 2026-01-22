# 🚀 Continued Extreme Detail Explanation

---

## 🎮 3. REMAINING CONTROLLERS

### **ReviewCycleController.java** - Review Cycle Management

```java
@RestController
@RequestMapping("/api/v1/review-cycles")
public class ReviewCycleController {
```
- **Simple terms**: "Handles all review cycle endpoints at /api/v1/review-cycles"

```java
@Autowired
private ReviewCycleService cycleSvc;
```
- **cycleSvc**: Shortened variable name for ReviewCycleService
- **Simple terms**: "Inject the review cycle service"

```java
@GetMapping
public ApiResponse<List<ReviewCycle>> getAllCycles() {
    List<ReviewCycle> cycles = cycleSvc.getAllCycles();
    return ApiResponse.success("Review cycles retrieved", cycles);
}
```
- **List<ReviewCycle>**: A list (array) of ReviewCycle objects
- **Simple terms**: "Get all review cycles (quarterly, annual reviews, etc.)"

```java
@GetMapping("/{cycleId}")
public ApiResponse<ReviewCycle> getCycleById(@PathVariable Integer cycleId) {
```
- **Example URL**: /api/v1/review-cycles/5
- **Simple terms**: "Get one specific review cycle by ID"

```java
@GetMapping("/active")
public ApiResponse<ReviewCycle> getActiveCycle() {
```
- **"/active"**: Special endpoint
- **Full URL**: /api/v1/review-cycles/active
- **Simple terms**: "Get the currently active review cycle"

```java
ReviewCycle cycle = cycleSvc.getActiveCycle();
return ApiResponse.success("Active cycle retrieved", cycle);
```
- **getActiveCycle()**: Service method finds cycle with status = ACTIVE
- **Simple terms**: "Ask service to find the active review period"

```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<ReviewCycle> createCycle(@Valid @RequestBody CreateReviewCycleRequest req,
                                            HttpServletRequest httpReq) {
```
- **Simple terms**: "Admin creates a new review cycle"

```java
Integer adminId = (Integer) httpReq.getAttribute("userId");
ReviewCycle cycle = cycleSvc.createCycle(req, adminId);
return ApiResponse.success("Review cycle created", cycle);
```

```java
@PutMapping("/{cycleId}")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<ReviewCycle> updateCycle(@PathVariable Integer cycleId,
                                            @Valid @RequestBody CreateReviewCycleRequest req,
                                            HttpServletRequest httpReq) {
```
- **Simple terms**: "Admin updates an existing review cycle"

```java
Integer adminId = (Integer) httpReq.getAttribute("userId");
ReviewCycle cycle = cycleSvc.updateCycle(cycleId, req, adminId);
return ApiResponse.success("Review cycle updated", cycle);
```

---

### **PerformanceReviewController.java** - Performance Reviews

```java
@RestController
@RequestMapping("/api/v1/performance-reviews")
public class PerformanceReviewController {
    
    @Autowired
    private PerformanceReviewService reviewSvc;
```

```java
@GetMapping
public ApiResponse<List<PerformanceReview>> getReviews(HttpServletRequest httpReq,
                                                        @RequestParam(required = false) Integer userId,
                                                        @RequestParam(required = false) Integer cycleId) {
```
- **Two optional query parameters**: userId and cycleId
- **Example URLs**:
  - /api/v1/performance-reviews (all for current user)
  - /api/v1/performance-reviews?userId=5 (for user 5)
  - /api/v1/performance-reviews?cycleId=2 (for cycle 2)
- **Simple terms**: "Get reviews with optional filters"

```java
String role = (String) httpReq.getAttribute("userRole");
Integer currentUserId = (Integer) httpReq.getAttribute("userId");
```
- **Simple terms**: "Extract current user's role and ID from request"

```java
List<PerformanceReview> reviews;
if (cycleId != null) {
    reviews = reviewSvc.getReviewsByCycle(cycleId);
```
- **if (cycleId != null)**: Check if cycleId was provided
- **!= null**: "is not null" (was provided)
- **Simple terms**: "If cycleId given, get reviews for that cycle"

```java
} else if (userId != null && role.equals("ADMIN")) {
    reviews = reviewSvc.getReviewsByUser(userId);
```
- **&&**: AND operator - both conditions must be true
- **Simple terms**: "If userId given AND user is admin, get that user's reviews"

```java
} else {
    reviews = reviewSvc.getReviewsByUser(currentUserId);
}
```
- **else**: Default case
- **Simple terms**: "Otherwise, get current user's own reviews"

```java
return ApiResponse.success("Reviews retrieved", reviews);
```

```java
@GetMapping("/{reviewId}")
public ApiResponse<PerformanceReview> getReviewById(@PathVariable Integer reviewId) {
```
- **Example**: /api/v1/performance-reviews/42
- **Simple terms**: "Get one specific review by ID"

```java
PerformanceReview review = reviewSvc.getReviewById(reviewId);
return ApiResponse.success("Review retrieved", review);
```

```java
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<PerformanceReview> submitSelfAssessment(@Valid @RequestBody SelfAssessmentRequest req,
                                                            HttpServletRequest httpReq) {
```
- **Simple terms**: "Employee submits self-assessment for a review cycle"

```java
Integer empId = (Integer) httpReq.getAttribute("userId");
PerformanceReview review = reviewSvc.submitSelfAssessment(req, empId);
return ApiResponse.success("Self-assessment submitted", review);
```

```java
@PutMapping("/{reviewId}/draft")
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<PerformanceReview> updateDraft(@PathVariable Integer reviewId,
                                                   @Valid @RequestBody SelfAssessmentRequest req,
                                                   HttpServletRequest httpReq) {
```
- **"/draft"**: Endpoint suffix
- **Full URL**: /api/v1/performance-reviews/42/draft
- **Simple terms**: "Employee updates their self-assessment draft before final submission"

```java
Integer empId = (Integer) httpReq.getAttribute("userId");
PerformanceReview review = reviewSvc.updateSelfAssessmentDraft(reviewId, req, empId);
return ApiResponse.success("Draft updated", review);
```

```java
@PutMapping("/{reviewId}")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<PerformanceReview> submitManagerReview(@PathVariable Integer reviewId,
                                                           @Valid @RequestBody ManagerReviewRequest req,
                                                           HttpServletRequest httpReq) {
```
- **Simple terms**: "Manager submits their review of employee's performance"

```java
Integer mgrId = (Integer) httpReq.getAttribute("userId");
PerformanceReview review = reviewSvc.submitManagerReview(reviewId, req, mgrId);
return ApiResponse.success("Manager review submitted", review);
```

```java
@PostMapping("/{reviewId}/acknowledge")
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<PerformanceReview> acknowledgeReview(@PathVariable Integer reviewId,
                                                         @RequestBody Map<String, String> body,
                                                         HttpServletRequest httpReq) {
```
- **"/acknowledge"**: Employee confirms they've read the review
- **Simple terms**: "Employee acknowledges they've seen their performance review"

```java
Integer empId = (Integer) httpReq.getAttribute("userId");
String response = body.get("response");
PerformanceReview review = reviewSvc.acknowledgeReview(reviewId, empId, response);
return ApiResponse.success("Review acknowledged", review);
```
- **body.get("response")**: Optional employee response/comment
- **Simple terms**: "Employee can add optional comments when acknowledging"

---

### **NotificationController.java** - Notifications

```java
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notifRepo;
```
- **notifRepo**: Directly using repository (not service)
- **Why?**: Notification operations are simple (read/update), don't need complex business logic
- **Simple terms**: "Inject notification repository for database access"

```java
@GetMapping
public ApiResponse<List<Notification>> getNotifications(HttpServletRequest httpReq,
                                                         @RequestParam(required = false) String status) {
```
- **status**: Optional filter (UNREAD or READ)
- **Example URLs**:
  - /api/v1/notifications (all)
  - /api/v1/notifications?status=UNREAD (only unread)
- **Simple terms**: "Get current user's notifications, optionally filtered by read status"

```java
Integer userId = (Integer) httpReq.getAttribute("userId");
```
- **Simple terms**: "Get logged-in user's ID"

```java
List<Notification> notifs;
if (status != null) {
    NotificationStatus notifStatus = NotificationStatus.valueOf(status.toUpperCase());
    notifs = notifRepo.findByUser_UserIdAndStatusOrderByCreatedDateDesc(userId, notifStatus);
```
- **NotificationStatus.valueOf()**: Convert string to enum
- **status.toUpperCase()**: Convert "unread" to "UNREAD"
- **valueOf()**: Finds enum constant matching the string
- **Example**: "UNREAD" → NotificationStatus.UNREAD
- **findByUser_UserIdAndStatusOrderByCreatedDateDesc()**: Spring Data JPA query method
- **Breakdown**:
  - **findBy**: Start of query
  - **User_UserId**: Navigate to User entity, filter by userId
  - **And**: AND condition
  - **Status**: Filter by status field
  - **OrderBy**: Sort results
  - **CreatedDate**: Sort by this field
  - **Desc**: Descending (newest first)
- **Simple terms**: "Get notifications for this user with this status, newest first"

```java
} else {
    notifs = notifRepo.findByUser_UserIdOrderByCreatedDateDesc(userId);
}
```
- **Simple terms**: "Get all notifications for this user, newest first"

```java
return ApiResponse.success("Notifications retrieved", notifs);
```

```java
@PutMapping("/{notifId}")
public ApiResponse<Notification> markAsRead(@PathVariable Integer notifId) {
```
- **Simple terms**: "Mark one notification as read"

```java
Notification notif = notifRepo.findById(notifId)
        .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
```
- **findById()**: Find entity by primary key (returns Optional)
- **Optional**: Container that may or may not contain a value
- **.orElseThrow()**: If not found, throw exception
- **() ->**: Lambda expression (anonymous function)
- **new ResourceNotFoundException()**: Create exception object
- **Simple terms**: "Find notification or throw error if doesn't exist"

**Why Optional?**
```java
// OLD way (without Optional):
Notification notif = notifRepo.findById(notifId);
if (notif == null) {
    throw new ResourceNotFoundException("Not found");
}

// NEW way (with Optional):
Notification notif = notifRepo.findById(notifId)
    .orElseThrow(() -> new ResourceNotFoundException("Not found"));
```
- **Benefit**: Cleaner, safer, prevents NullPointerException

```java
notif.setStatus(NotificationStatus.READ);
notif.setReadDate(LocalDateTime.now());
```
- **setStatus()**: Update status to READ
- **LocalDateTime.now()**: Current date and time
- **Simple terms**: "Mark as READ and record when it was read"

```java
Notification updated = notifRepo.save(notif);
```
- **save()**: Insert or update in database
- **Why save() for update?**: JPA save() does "upsert" (insert if new, update if exists)
- **Simple terms**: "Save changes to database"

```java
return ApiResponse.success("Notification marked as read", updated);
```

```java
@PutMapping("/mark-all-read")
public ApiResponse<Void> markAllAsRead(HttpServletRequest httpReq) {
```
- **Simple terms**: "Mark ALL unread notifications as read for current user"

```java
Integer userId = (Integer) httpReq.getAttribute("userId");

List<Notification> notifs = notifRepo
        .findByUser_UserIdAndStatusOrderByCreatedDateDesc(userId, NotificationStatus.UNREAD);
```
- **Simple terms**: "Get all unread notifications for this user"

```java
notifs.forEach(n -> {
    n.setStatus(NotificationStatus.READ);
    n.setReadDate(LocalDateTime.now());
});
```
- **forEach()**: Loop through each item in list
- **n ->**: Lambda parameter (each notification)
- **{}**: Code to execute for each item
- **Simple terms**: "For each notification, mark as READ with current timestamp"

**Equivalent traditional loop:**
```java
for (Notification n : notifs) {
    n.setStatus(NotificationStatus.READ);
    n.setReadDate(LocalDateTime.now());
}
```

```java
notifRepo.saveAll(notifs);
```
- **saveAll()**: Save multiple entities at once
- **Why bulk save?**: More efficient than saving one by one
- **Simple terms**: "Update all notifications in database in one operation"

```java
return ApiResponse.success("All notifications marked as read");
```

---

### **FeedbackController.java** - Feedback

```java
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {
    
    @Autowired
    private FeedbackRepository fbRepo;
    
    @Autowired
    private GoalRepository goalRepo;
    
    @Autowired
    private PerformanceReviewRepository reviewRepo;
    
    @Autowired
    private UserRepository userRepo;
```
- **Multiple repositories**: Need to access goals, reviews, and users
- **Simple terms**: "Inject all repositories we need for feedback operations"

```java
@GetMapping
public ApiResponse<List<Feedback>> getFeedback(@RequestParam(required = false) Integer goalId,
                                                @RequestParam(required = false) Integer reviewId) {
```
- **Two optional filters**: goalId or reviewId
- **Simple terms**: "Get feedback filtered by goal OR review"

```java
List<Feedback> feedback;
if (goalId != null) {
    feedback = fbRepo.findByGoal_GoalId(goalId);
} else if (reviewId != null) {
    feedback = fbRepo.findByReview_ReviewId(reviewId);
} else {
    feedback = fbRepo.findAll();
}
return ApiResponse.success("Feedback retrieved", feedback);
```
- **Simple terms**: "Get feedback for specific goal/review, or all feedback"

```java
@PostMapping
public ApiResponse<Feedback> createFeedback(@RequestBody Map<String, Object> body,
                                            HttpServletRequest httpReq) {
```
- **Map<String, Object>**: Key-value pairs where values can be any type
- **Why Object?**: goalId is Integer, comments is String
- **Simple terms**: "Create new feedback"

```java
Integer userId = (Integer) httpReq.getAttribute("userId");
User user = userRepo.findById(userId).orElse(null);
```
- **.orElse(null)**: If not found, return null instead of throwing error
- **Simple terms**: "Find user who's giving feedback"

```java
Feedback fb = new Feedback();
fb.setGivenByUser(user);
fb.setComments((String) body.get("comments"));
fb.setFeedbackType((String) body.get("feedbackType"));
fb.setDate(LocalDateTime.now());
```
- **new Feedback()**: Create new feedback object
- **(String) body.get()**: Cast Object to String
- **Simple terms**: "Create feedback with user, comments, type, and timestamp"

```java
if (body.get("goalId") != null) {
    Integer goalId = (Integer) body.get("goalId");
    Goal goal = goalRepo.findById(goalId).orElse(null);
    fb.setGoal(goal);
}
```
- **if (body.get("goalId") != null)**: Was goalId provided?
- **Simple terms**: "If goalId provided, link feedback to that goal"

```java
if (body.get("reviewId") != null) {
    Integer reviewId = (Integer) body.get("reviewId");
    PerformanceReview review = reviewRepo.findById(reviewId).orElse(null);
    fb.setReview(review);
}
```
- **Simple terms**: "If reviewId provided, link feedback to that review"

```java
Feedback saved = fbRepo.save(fb);
return ApiResponse.success("Feedback created", saved);
```

---

### **ReportController.java** - Reports & Analytics

```java
@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {
```
- **@PreAuthorize on class level**: Applies to ALL methods in this class
- **Simple terms**: "All report endpoints require ADMIN or MANAGER role"

```java
@Autowired
private ReportService reportSvc;
```

```java
@GetMapping
public ApiResponse<List<Report>> getAllReports() {
    List<Report> reports = reportSvc.getAllReports();
    return ApiResponse.success("Reports retrieved", reports);
}
```
- **Simple terms**: "Get all generated reports"

```java
@GetMapping("/{reportId}")
public ApiResponse<Report> getReportById(@PathVariable Integer reportId) {
    Report report = reportSvc.getReportById(reportId);
    return ApiResponse.success("Report retrieved", report);
}
```
- **Simple terms**: "Get one specific report"

```java
@PostMapping("/generate")
public ApiResponse<Report> generateReport(@RequestBody Map<String, String> body,
                                           HttpServletRequest httpReq) {
```
- **"/generate"**: Special endpoint for creating new reports
- **Simple terms**: "Generate a new report with specified parameters"

```java
Integer userId = (Integer) httpReq.getAttribute("userId");
String scope = body.get("scope");
String metrics = body.get("metrics");
String format = body.getOrDefault("format", "PDF");
```
- **.getOrDefault()**: Get value OR use default if key doesn't exist
- **"format", "PDF"**: If format not provided, use "PDF"
- **Simple terms**: "Extract report parameters; use PDF if format not specified"

```java
Report report = reportSvc.generateReport(scope, metrics, format, userId);
return ApiResponse.success("Report generated", report);
```

```java
@GetMapping("/dashboard")
public ApiResponse<Map<String, Object>> getDashboard(HttpServletRequest httpReq) {
```
- **Map<String, Object>**: Dictionary/map with string keys and any value types
- **Why Object?**: Values can be Integer, Double, String, etc.
- **Example response**:
  ```json
  {
    "totalGoals": 42,
    "completionRate": 75.5,
    "department": "Engineering"
  }
  ```
- **Simple terms**: "Get dashboard metrics (stats) for current user"

```java
String role = (String) httpReq.getAttribute("userRole");
Integer userId = (Integer) httpReq.getAttribute("userId");
Map<String, Object> metrics = reportSvc.getDashboardMetrics(userId, role);
return ApiResponse.success("Dashboard metrics retrieved", metrics);
```
- **Simple terms**: "Get role-specific dashboard data (employee/manager/admin see different stats)"

```java
@GetMapping("/performance-summary")
public ApiResponse<Map<String, Object>> getPerformanceSummary(
        @RequestParam(required = false) Integer cycleId,
        @RequestParam(required = false) String dept) {
```
- **Two optional filters**: cycleId and dept
- **Simple terms**: "Get performance summary stats, optionally filtered"

```java
Map<String, Object> summary = reportSvc.getPerformanceSummary(cycleId, dept);
return ApiResponse.success("Performance summary retrieved", summary);
```

```java
@GetMapping("/goal-analytics")
public ApiResponse<Map<String, Object>> getGoalAnalytics() {
```
- **Simple terms**: "Get analytics about goals (completion rates, status breakdown, etc.)"

```java
Map<String, Object> analytics = reportSvc.getGoalAnalytics();
return ApiResponse.success("Goal analytics retrieved", analytics);
```

```java
@GetMapping("/department-performance")
public ApiResponse<List<Map<String, Object>>> getDeptPerformance() {
```
- **List<Map<String, Object>>**: List of dictionaries
- **Example response**:
  ```json
  [
    {"department": "Engineering", "completionRate": 85.5},
    {"department": "Sales", "completionRate": 72.3}
  ]
  ```
- **Simple terms**: "Get performance metrics for each department"

```java
List<Map<String, Object>> performance = reportSvc.getDepartmentPerformance();
return ApiResponse.success("Department performance retrieved", performance);
```

---

### **AuditLogController.java** - Audit Logs

```java
@RestController
@RequestMapping("/api/v1/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
```
- **Admin only**: Audit logs are security-sensitive
- **Simple terms**: "Only admins can view audit logs (security tracking)"

```java
@Autowired
private AuditLogRepository auditRepo;
```

```java
@GetMapping
public ApiResponse<List<AuditLog>> getAuditLogs(
        @RequestParam(required = false) Integer userId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDt,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDt) {
```
- **@DateTimeFormat**: Tell Spring how to parse date string
- **iso = DateTimeFormat.ISO.DATE_TIME**: Use ISO 8601 format
- **ISO 8601 example**: "2025-01-22T14:30:00"
- **Four optional filters**: userId, action, date range
- **Simple terms**: "Get audit logs with various filter options"

```java
List<AuditLog> logs;

if (userId != null) {
    logs = auditRepo.findByUser_UserIdOrderByTimestampDesc(userId);
```
- **Simple terms**: "If userId provided, get logs for that user"

```java
} else if (action != null) {
    logs = auditRepo.findByActionOrderByTimestampDesc(action);
```
- **Simple terms**: "If action provided, get logs for that action type"

```java
} else if (startDt != null && endDt != null) {
    logs = auditRepo.findByTimestampBetweenOrderByTimestampDesc(startDt, endDt);
```
- **startDt != null && endDt != null**: BOTH dates must be provided
- **Simple terms**: "If date range provided, get logs in that time period"

```java
} else {
    logs = auditRepo.findAll();
}
```
- **Simple terms**: "No filters? Get all audit logs"

```java
return ApiResponse.success("Audit logs retrieved", logs);
```

```java
@PostMapping("/export")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<String> exportLogs(@RequestBody Map<String, String> body) {
```
- **Simple terms**: "Export audit logs to file (CSV, etc.)"

```java
String format = body.getOrDefault("format", "CSV");
String filePath = "/exports/audit_logs_" + System.currentTimeMillis() + "." + format.toLowerCase();
```
- **System.currentTimeMillis()**: Current time in milliseconds since 1970
- **Why?**: Creates unique filename
- **Example**: /exports/audit_logs_1706023840000.csv
- **.toLowerCase()**: Convert "CSV" to "csv"
- **Simple terms**: "Create unique filename with timestamp"

```java
// In real implementation, this would generate actual file
return ApiResponse.success("Audit logs export initiated", filePath);
```
- **Note**: This is a stub; real implementation would generate file
- **Simple terms**: "Return file path (actual file generation would happen in production)"

---

## 📦 4. DTOs (Data Transfer Objects)

### **ApiResponse.java** - Standard API Response Wrapper

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
```
- **<T>**: Generic type parameter
- **Why generics?**: Can hold any type of data
- **Example**: ApiResponse<User>, ApiResponse<Goal>, ApiResponse<String>
- **Simple terms**: "A container that can hold any type of response data"

```java
private String status;  // "success" or "error"
private String msg;     // Message
private T data;         // Response data
```
- **T data**: The actual data (type T, specified when creating ApiResponse)
- **Example**:
  ```java
  ApiResponse<User> response;
  response.data → User object
  
  ApiResponse<List<Goal>> response;
  response.data → List of Goals
  ```

```java
public static <T> ApiResponse<T> success(String msg, T data) {
    return new ApiResponse<>("success", msg, data);
}
```
- **public static**: Can call without creating ApiResponse object
- **<T>**: Method is also generic
- **ApiResponse<T>**: Returns ApiResponse with type T
- **Usage**: `ApiResponse.success("Done", user)`
- **Simple terms**: "Helper method to create success response"

```java
public static <T> ApiResponse<T> success(String msg) {
    return new ApiResponse<>("success", msg, null);
}
```
- **Overloaded method**: Same name, different parameters
- **null**: No data to return
- **Usage**: `ApiResponse.success("Deleted successfully")`
- **Simple terms**: "Success response without data"

```java
public static <T> ApiResponse<T> error(String msg) {
    return new ApiResponse<>("error", msg, null);
}
```
- **Simple terms**: "Helper method to create error response"

**Example JSON responses:**
```json
// Success with data
{
  "status": "success",
  "msg": "User created",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}

// Success without data
{
  "status": "success",
  "msg": "User deleted",
  "data": null
}

// Error
{
  "status": "error",
  "msg": "User not found",
  "data": null
}
```

---

### **LoginRequest.java** - Login Data

```java
@Data
public class LoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
```
- **@NotBlank**: Field cannot be null, empty, or whitespace-only
- **message = "..."**: Error message if validation fails
- **@Email**: Must be valid email format
- **Validation examples**:
  - ✅ "john@example.com"
  - ❌ null (NotBlank fails)
  - ❌ "" (NotBlank fails)
  - ❌ "   " (NotBlank fails)
  - ❌ "not-an-email" (Email fails)
- **Simple terms**: "Email is required and must be valid format"

```java
@NotBlank(message = "Password is required")
private String password;
```
- **Simple terms**: "Password is required (cannot be blank)"

**Request JSON example:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

---

### **LoginResponse.java** - Login Response Data

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;       // JWT token
    private Integer userId;
    private String name;
    private String email;
    private UserRole role;
    private String department;
}
```
- **Purpose**: What to send back after successful login
- **token**: JWT token for authentication
- **Simple terms**: "All the info the frontend needs after login"

**Response JSON example:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 42,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "EMPLOYEE",
  "department": "Engineering"
}
```

---

### **CreateGoalRequest.java** - Goal Creation Data

```java
@Data
public class CreateGoalRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
```
- **Simple terms**: "Goal title cannot be blank"

```java
@NotBlank(message = "Description is required")
private String desc;
```
- **desc**: Shortened from "description"
- **Simple terms**: "Description cannot be blank"

```java
@NotNull(message = "Category is required")
private GoalCategory cat;
```
- **@NotNull**: Cannot be null (but can be empty if it was a string)
- **GoalCategory**: Enum type
- **Simple terms**: "Must provide a category (LEARNING, PERFORMANCE, etc.)"

```java
@NotNull(message = "Priority is required")
private GoalPriority pri;
```
- **Simple terms**: "Must specify priority (HIGH, MEDIUM, LOW)"

```java
@NotNull(message = "Start date is required")
private LocalDate startDt;

@NotNull(message = "End date is required")
private LocalDate endDt;
```
- **LocalDate**: Date without time (just year-month-day)
- **Simple terms**: "Must provide start and end dates"

```java
@NotNull(message = "Manager ID is required")
private Integer mgrId;
```
- **Simple terms**: "Must specify which manager will oversee this goal"

**Request JSON example:**
```json
{
  "title": "Complete Spring Boot Training",
  "desc": "Learn Spring Boot by building 3 projects",
  "cat": "LEARNING",
  "pri": "HIGH",
  "startDt": "2025-02-01",
  "endDt": "2025-05-01",
  "mgrId": 5
}
```

---

### **SubmitCompletionRequest.java** - Goal Completion Submission

```java
@Data
public class SubmitCompletionRequest {
    
    @NotBlank(message = "Evidence link is required")
    private String evLink;  // evidenceLink
```
- **evLink**: Evidence link (GitHub repo, Google Drive, etc.)
- **Simple terms**: "Must provide proof of goal completion"

```java
@NotBlank(message = "Link description is required")
private String linkDesc;  // evidenceLinkDescription
```
- **Simple terms**: "Must describe what's in the evidence link"

```java
private String accessInstr;  // evidenceAccessInstructions
```
- **No validation**: Optional field
- **Simple terms**: "How to access the evidence (password, instructions, etc.)"

```java
private String compNotes;  // completionNotes
```
- **Simple terms**: "Optional notes about completing the goal"

**Request JSON example:**
```json
{
  "evLink": "https://github.com/john/spring-boot-project",
  "linkDesc": "GitHub repository with all 3 completed projects",
  "accessInstr": "Public repository, no login needed",
  "compNotes": "Completed all requirements and added unit tests"
}
```

---

### **ApproveCompletionRequest.java** - Manager Approval

```java
@Data
public class ApproveCompletionRequest {
    
    private String mgrComments;  // Manager comments on completion
}
```
- **No validation**: Comments are optional
- **Simple terms**: "Manager's feedback when approving goal completion"

**Request JSON example:**
```json
{
  "mgrComments": "Excellent work! Projects demonstrate strong understanding."
}
```

---

### **SelfAssessmentRequest.java** - Employee Self-Assessment

```java
@Data
public class SelfAssessmentRequest {
    
    @NotNull(message = "Cycle ID is required")
    private Integer cycleId;
```
- **Simple terms**: "Which review cycle is this for?"

```java
@NotNull(message = "Self-assessment data is required")
private String selfAssmt;  // JSON string with all sections
```
- **JSON string**: Contains structured assessment data
- **Example content**:
  ```json
  {
    "achievements": "Completed 10 goals",
    "challenges": "Time management",
    "areas_for_improvement": "Technical skills"
  }
  ```
- **Simple terms**: "Employee's self-evaluation as JSON string"

```java
@NotNull(message = "Self-rating is required")
private Integer selfRating;
```
- **Typical range**: 1-5
- **Simple terms**: "How would you rate yourself?"

**Request JSON example:**
```json
{
  "cycleId": 2,
  "selfAssmt": "{\"achievements\":\"...\",\"challenges\":\"...\"}",
  "selfRating": 4
}
```

---

### **ManagerReviewRequest.java** - Manager's Review

```java
@Data
public class ManagerReviewRequest {
    
    @NotNull(message = "Manager feedback is required")
    private String mgrFb;  // JSON string with manager feedback
```
- **Simple terms**: "Manager's evaluation of employee"

```java
@NotNull(message = "Manager rating is required")
private Integer mgrRating;
```
- **Simple terms**: "Manager's rating (1-5)"

```java
private String ratingJust;  // Rating justification
```
- **Optional**: Why this rating?
- **Simple terms**: "Explanation for the rating"

```java
private String compRec;     // Compensation recommendations (JSON)
```
- **Optional**: Salary increase, bonus recommendations
- **JSON format**: Structured recommendations
- **Simple terms**: "Recommendations for raise/bonus"

```java
private String nextGoals;   // Next period goals
```
- **Optional**: Goals for next review cycle
- **Simple terms**: "What should employee work on next?"

---

### **CreateUserRequest.java** - User Creation/Update

```java
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
```

```java
@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
private String email;
```
- **Two validations**: Not blank AND valid email format

```java
@NotBlank(message = "Password is required")
private String password;
```

```java
@NotNull(message = "Role is required")
private UserRole role;
```
- **Simple terms**: "ADMIN, MANAGER, or EMPLOYEE"

```java
private String dept;
```
- **Optional**: Department name

```java
private Integer mgrId;  // Manager ID
```
- **Optional**: Who's their manager?

```java
private UserStatus status = UserStatus.ACTIVE;
```
- **Default value**: New users are ACTIVE by default
- **= UserStatus.ACTIVE**: Field initializer
- **Simple terms**: "Users start as ACTIVE unless specified otherwise"

---

### **CreateReviewCycleRequest.java** - Review Cycle Creation

```java
@Data
public class CreateReviewCycleRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
```
- **Example**: "Q1 2025 Review", "Annual Review 2025"

```java
@NotNull(message = "Start date is required")
private LocalDate startDt;

@NotNull(message = "End date is required")
private LocalDate endDt;
```

```java
@NotNull(message = "Status is required")
private ReviewCycleStatus status;
```
- **ACTIVE or CLOSED**

```java
private Boolean reqCompAppr = true;  // Requires completion approval
```
- **Boolean**: true or false
- **= true**: Default value
- **Simple terms**: "By default, goal completions need manager approval"

```java
private Boolean evReq = true;        // Evidence required
```
- **Simple terms**: "By default, goals need evidence"

---

## 🏪 5. REPOSITORIES (Database Access Layer)

Repositories use **Spring Data JPA** which auto-generates database queries!

### **UserRepository.java**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
```
- **interface**: Contract (doesn't have implementation)
- **extends JpaRepository<User, Integer>**: Inherit common database operations
- **<User, Integer>**: Entity type and ID type
- **@Repository**: Marks this as a data access component
- **Simple terms**: "Interface for User table database operations"

**What JpaRepository provides automatically:**
```java
save(user)         // Insert or update
findById(id)       // Find by primary key
findAll()          // Get all records
deleteById(id)     // Delete by ID
count()            // Count total records
existsById(id)     // Check if exists
```

```java
Optional<User> findByEmail(String email);
```
- **Method naming convention**: Spring generates SQL automatically!
- **findBy**: Start of query
- **Email**: Field name (must match entity field exactly!)
- **Generated SQL**: `SELECT * FROM users WHERE email = ?`
- **Optional<User>**: May or may not find a user
- **Simple terms**: "Find user by email address"

```java
List<User> findByRole(UserRole role);
```
- **Generated SQL**: `SELECT * FROM users WHERE role = ?`
- **Returns**: List (can be empty)
- **Simple terms**: "Find all users with specific role"

```java
List<User> findByDepartment(String department);
```
- **Generated SQL**: `SELECT * FROM users WHERE department = ?`

```java
List<User> findByStatus(UserStatus status);
```
- **Generated SQL**: `SELECT * FROM users WHERE status = ?`

```java
List<User> findByManager_UserId(Integer managerId);
```
- **Manager_UserId**: Navigate through relationship
- **Manager**: Entity reference in User
- **_**: Separator
- **UserId**: Field in Manager (which is also a User)
- **Generated SQL**: `SELECT * FROM users WHERE manager_id = ?`
- **Simple terms**: "Find all users who report to this manager"

---

### **GoalRepository.java**

```java
@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    
    List<Goal> findByAssignedToUser_UserId(Integer userId);
```
- **AssignedToUser_UserId**: Navigate relationship
- **Generated SQL**: `SELECT * FROM goals WHERE assigned_to_user_id = ?`
- **Simple terms**: "Find all goals assigned to this user"

```java
List<Goal> findByAssignedManager_UserId(Integer managerId);
```
- **Simple terms**: "Find all goals managed by this manager"

```java
List<Goal> findByStatus(GoalStatus status);
```
- **Simple terms**: "Find goals with specific status (PENDING, COMPLETED, etc.)"

```java
List<Goal> findByAssignedToUser_UserIdAndStatus(Integer userId, GoalStatus status);
```
- **And**: Combines two conditions
- **Generated SQL**: `SELECT * FROM goals WHERE assigned_to_user_id = ? AND status = ?`
- **Simple terms**: "Find user's goals with specific status"

---

### **PerformanceReviewRepository.java**

```java
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Integer> {
    
    List<PerformanceReview> findByUser_UserId(Integer userId);
```
- **Simple terms**: "Find all reviews for this user"

```java
List<PerformanceReview> findByCycle_CycleId(Integer cycleId);
```
- **Simple terms**: "Find all reviews in this cycle"

```java
List<PerformanceReview> findByStatus(PerformanceReviewStatus status);
```

```java
Optional<PerformanceReview> findByCycle_CycleIdAndUser_UserId(Integer cycleId, Integer userId);
```
- **Optional**: May not exist (user might not have review for this cycle yet)
- **Simple terms**: "Find user's review for specific cycle"

---

### **ReviewCycleRepository.java**

```java
@Repository
public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, Integer> {
    
    List<ReviewCycle> findByStatus(ReviewCycleStatus status);
```

```java
Optional<ReviewCycle> findFirstByStatusOrderByStartDateDesc(ReviewCycleStatus status);
```
- **findFirst**: Get only one result
- **By**: Filter condition
- **Status**: By this status
- **OrderBy**: Sort results
- **StartDateDesc**: By start date, descending (newest first)
- **Generated SQL**: `SELECT * FROM review_cycles WHERE status = ? ORDER BY start_date DESC LIMIT 1`
- **Simple terms**: "Find the most recent active review cycle"

---

### **NotificationRepository.java**

```java
List<Notification> findByUser_UserIdOrderByCreatedDateDesc(Integer userId);
```
- **OrderBy**: Sort results
- **CreatedDateDesc**: By created date, descending
- **Simple terms**: "Find user's notifications, newest first"

```java
List<Notification> findByUser_UserIdAndStatusOrderByCreatedDateDesc(Integer userId, NotificationStatus status);
```
- **And**: Multiple conditions
- **Simple terms**: "Find user's notifications with specific status, newest first"

---

### **PerformanceReviewGoalsRepository.java**

```java
List<PerformanceReviewGoals> findByReview_ReviewId(Integer reviewId);
```
- **Simple terms**: "Find all goal links for this review"

```java
List<PerformanceReviewGoals> findByGoal_GoalId(Integer goalId);
```
- **Simple terms**: "Find all reviews that reference this goal"

---

### **FeedbackRepository.java**

```java
List<Feedback> findByGoal_GoalId(Integer goalId);
List<Feedback> findByReview_ReviewId(Integer reviewId);
```

---

### **ReportRepository.java**

```java
List<Report> findByScope(String scope);
List<Report> findByGeneratedBy_UserIdOrderByGeneratedDateDesc(Integer userId);
```

---

### **AuditLogRepository.java**

```java
List<AuditLog> findByUser_UserIdOrderByTimestampDesc(Integer userId);
List<AuditLog> findByActionOrderByTimestampDesc(String action);
```

```java
List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
```
- **Between**: Range query
- **Generated SQL**: `SELECT * FROM audit_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC`
- **Simple terms**: "Find logs within date range, newest first"

---

I've explained Controllers, DTOs, and Repositories in extreme detail. Would you like me to continue with:

1. **Services** (Business Logic) - AuthService, GoalService, UserService, etc.
2. **Security** (JwtAuthFilter, JwtUtil)
3. **Exception Handling** (GlobalExceptionHandler)
4. **Main Application Class**

Which section should I explain next?