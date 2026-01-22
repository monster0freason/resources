# 🚀 Continued - Remaining Services, Security, Exceptions & Main App

---

## 🔧 7. REMAINING SERVICES

### **PerformanceReviewService.java** - Performance Review Logic

```java
@Service
public class PerformanceReviewService {
    
    @Autowired
    private PerformanceReviewRepository reviewRepo;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private ReviewCycleRepository cycleRepo;
    
    @Autowired
    private NotificationRepository notifRepo;
    
    @Autowired
    private AuditLogRepository auditRepo;
    
    @Autowired
    private PerformanceReviewGoalsRepository reviewGoalsRepo;
    
    @Autowired
    private GoalRepository goalRepo;
```
- **Multiple repositories**: Reviews interact with cycles, goals, users, notifications
- **Simple terms**: "Inject all dependencies needed for review operations"

---

#### **Retrieval Methods**

```java
public List<PerformanceReview> getReviewsByUser(Integer userId) {
    return reviewRepo.findByUser_UserId(userId);
}

public List<PerformanceReview> getReviewsByCycle(Integer cycleId) {
    return reviewRepo.findByCycle_CycleId(cycleId);
}

public PerformanceReview getReviewById(Integer reviewId) {
    return reviewRepo.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
}
```
- **Simple methods**: Direct repository calls
- **Simple terms**: "Get reviews by different criteria"

---

#### **submitSelfAssessment() Method**

```java
public PerformanceReview submitSelfAssessment(SelfAssessmentRequest req, Integer empId) {
```
- **Simple terms**: "Employee submits their self-assessment for a review cycle"

```java
User emp = userRepo.findById(empId)
        .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
```
- **Simple terms**: "Find the employee"

```java
ReviewCycle cycle = cycleRepo.findById(req.getCycleId())
        .orElseThrow(() -> new ResourceNotFoundException("Review cycle not found"));
```
- **Simple terms**: "Find the review cycle"

```java
PerformanceReview review = reviewRepo
        .findByCycle_CycleIdAndUser_UserId(req.getCycleId(), empId)
        .orElse(null);
```
- **Complex query**: Find review for this cycle AND this user
- **.orElse(null)**: Return null if doesn't exist (not throwing error)
- **Why might not exist?**: First time employee submits for this cycle
- **Simple terms**: "Check if review already exists for this employee and cycle"

```java
if (review != null && review.getStatus() != PerformanceReviewStatus.PENDING) {
    throw new BadRequestException("Self-assessment already submitted");
}
```
- **&&**: AND operator
- **!=**: Not equal
- **Logic**: If review exists AND status is NOT PENDING, it's already submitted
- **Simple terms**: "Prevent resubmitting already completed self-assessment"

```java
if (review == null) {
    review = new PerformanceReview();
    review.setCycle(cycle);
    review.setUser(emp);
}
```
- **First submission**: Create new review
- **Simple terms**: "If no review exists, create new one"

```java
review.setSelfAssessment(req.getSelfAssmt());
review.setEmployeeSelfRating(req.getSelfRating());
review.setStatus(PerformanceReviewStatus.SELF_ASSESSMENT_COMPLETED);
review.setSubmittedDate(LocalDateTime.now());
```
- **Status**: PENDING → SELF_ASSESSMENT_COMPLETED
- **Simple terms**: "Fill in self-assessment data and mark as completed"

```java
PerformanceReview saved = reviewRepo.save(review);
```
- **save()**: Insert if new (reviewId is null), update if exists
- **Simple terms**: "Save review to database"

```java
List<Goal> completedGoals = goalRepo.findByAssignedToUser_UserIdAndStatus(empId, GoalStatus.COMPLETED);
for (Goal goal : completedGoals) {
    PerformanceReviewGoals link = new PerformanceReviewGoals();
    link.setReview(saved);
    link.setGoal(goal);
    link.setLinkedDate(LocalDateTime.now());
    reviewGoalsRepo.save(link);
}
```
- **Purpose**: Link all completed goals to this review
- **for loop**: Iterate through each completed goal
- **PerformanceReviewGoals**: Junction table for many-to-many relationship
- **Why link?**: Manager can see what goals employee completed during review
- **Simple terms**: "Link all employee's completed goals to this review"

**For loop breakdown:**
```java
List<Goal> completedGoals;  // List of goals
for (Goal goal : completedGoals) {
    // goal is each item in the list, one at a time
    // Code here runs for EACH goal
}
```

```java
if (emp.getManager() != null) {
    Notification notif = new Notification();
    notif.setUser(emp.getManager());
    notif.setType(NotificationType.SELF_ASSESSMENT_SUBMITTED);
    notif.setMessage(emp.getName() + " submitted self-assessment");
    notif.setRelatedEntityType("PerformanceReview");
    notif.setRelatedEntityId(saved.getReviewId());
    notif.setStatus(NotificationStatus.UNREAD);
    notif.setPriority("HIGH");
    notif.setActionRequired(true);
    notifRepo.save(notif);
}
```
- **if (emp.getManager() != null)**: Check employee has a manager
- **Why check?**: Some users (like CEOs) might not have managers
- **Simple terms**: "Notify manager that employee submitted self-assessment"

```java
AuditLog log = new AuditLog();
log.setUser(emp);
log.setAction("SELF_ASSESSMENT_SUBMITTED");
log.setDetails("Submitted self-assessment for " + cycle.getTitle());
log.setRelatedEntityType("PerformanceReview");
log.setRelatedEntityId(saved.getReviewId());
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);
```
- **Simple terms**: "Record submission in audit log"

```java
return saved;
```

---

#### **updateSelfAssessmentDraft() Method**

```java
public PerformanceReview updateSelfAssessmentDraft(Integer reviewId, SelfAssessmentRequest req, Integer empId) {
```
- **Simple terms**: "Employee updates their self-assessment draft (before final submission)"

```java
PerformanceReview review = getReviewById(reviewId);

if (!review.getUser().getUserId().equals(empId)) {
    throw new UnauthorizedException("Not authorized");
}
```
- **Simple terms**: "Find review and verify employee owns it"

```java
if (review.getStatus() != PerformanceReviewStatus.PENDING && 
    review.getStatus() != PerformanceReviewStatus.SELF_ASSESSMENT_COMPLETED) {
    throw new BadRequestException("Cannot update - review already completed");
}
```
- **!=**: Not equal
- **Logic**: Can only update if PENDING or SELF_ASSESSMENT_COMPLETED
- **Why?**: Once manager reviews, employee can't change self-assessment
- **Simple terms**: "Check review is still editable"

```java
review.setSelfAssessment(req.getSelfAssmt());
review.setEmployeeSelfRating(req.getSelfRating());

PerformanceReview updated = reviewRepo.save(review);
```
- **Note**: NOT changing status here
- **Simple terms**: "Update self-assessment without changing review status"

```java
User emp = userRepo.findById(empId).orElse(null);
AuditLog log = new AuditLog();
log.setUser(emp);
log.setAction("SELF_ASSESSMENT_DRAFT_UPDATED");
log.setDetails("Updated self-assessment draft");
log.setRelatedEntityType("PerformanceReview");
log.setRelatedEntityId(reviewId);
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);
```

```java
return updated;
```

---

#### **submitManagerReview() Method**

```java
public PerformanceReview submitManagerReview(Integer reviewId, ManagerReviewRequest req, Integer mgrId) {
```
- **Simple terms**: "Manager submits their review of employee's performance"

```java
PerformanceReview review = getReviewById(reviewId);

if (!review.getUser().getManager().getUserId().equals(mgrId)) {
    throw new UnauthorizedException("Not authorized");
}
```
- **review.getUser().getManager()**: Navigate: Review → User → Manager
- **Simple terms**: "Verify this manager oversees this employee"

```java
if (review.getStatus() != PerformanceReviewStatus.SELF_ASSESSMENT_COMPLETED) {
    throw new BadRequestException("Self-assessment not completed");
}
```
- **Business rule**: Employee must complete self-assessment first
- **Simple terms**: "Check employee has submitted their part"

```java
User mgr = userRepo.findById(mgrId).orElse(null);
review.setManagerFeedback(req.getMgrFb());
review.setManagerRating(req.getMgrRating());
review.setRatingJustification(req.getRatingJust());
review.setCompensationRecommendations(req.getCompRec());
review.setNextPeriodGoals(req.getNextGoals());
review.setReviewedBy(mgr);
review.setReviewCompletedDate(LocalDateTime.now());
review.setStatus(PerformanceReviewStatus.COMPLETED);
```
- **Status**: SELF_ASSESSMENT_COMPLETED → COMPLETED
- **All manager fields**: Feedback, rating, justification, compensation, next goals
- **Simple terms**: "Fill in all manager review data and mark as completed"

```java
PerformanceReview saved = reviewRepo.save(review);
```

```java
Notification notif = new Notification();
notif.setUser(review.getUser());
notif.setType(NotificationType.PERFORMANCE_REVIEW_COMPLETED);
notif.setMessage("Your performance review has been completed");
notif.setRelatedEntityType("PerformanceReview");
notif.setRelatedEntityId(reviewId);
notif.setStatus(NotificationStatus.UNREAD);
notif.setPriority("HIGH");
notifRepo.save(notif);
```
- **Simple terms**: "Notify employee their review is ready"

```java
AuditLog log = new AuditLog();
log.setUser(mgr);
log.setAction("MANAGER_REVIEW_COMPLETED");
log.setDetails("Completed review for " + review.getUser().getName());
log.setRelatedEntityType("PerformanceReview");
log.setRelatedEntityId(reviewId);
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);
```

```java
return saved;
```

---

#### **acknowledgeReview() Method**

```java
public PerformanceReview acknowledgeReview(Integer reviewId, Integer empId, String response) {
```
- **Simple terms**: "Employee acknowledges they've read their review"

```java
PerformanceReview review = getReviewById(reviewId);

if (!review.getUser().getUserId().equals(empId)) {
    throw new UnauthorizedException("Not authorized");
}

if (review.getStatus() != PerformanceReviewStatus.COMPLETED) {
    throw new BadRequestException("Review not completed");
}
```
- **Business rule**: Can only acknowledge COMPLETED reviews
- **Simple terms**: "Verify employee owns review and it's completed"

```java
User emp = userRepo.findById(empId).orElse(null);
review.setAcknowledgedBy(emp);
review.setAcknowledgedDate(LocalDateTime.now());
review.setEmployeeResponse(response);
review.setStatus(PerformanceReviewStatus.COMPLETED_AND_ACKNOWLEDGED);
```
- **Status**: COMPLETED → COMPLETED_AND_ACKNOWLEDGED
- **employeeResponse**: Optional comments from employee
- **Simple terms**: "Record acknowledgment with optional employee response"

```java
PerformanceReview saved = reviewRepo.save(review);
```

```java
if (review.getUser().getManager() != null) {
    Notification notif = new Notification();
    notif.setUser(review.getUser().getManager());
    notif.setType(NotificationType.REVIEW_ACKNOWLEDGED);
    notif.setMessage(review.getUser().getName() + " acknowledged their review");
    notif.setRelatedEntityType("PerformanceReview");
    notif.setRelatedEntityId(reviewId);
    notif.setStatus(NotificationStatus.UNREAD);
    notifRepo.save(notif);
}
```
- **Simple terms**: "Notify manager that employee acknowledged review"

```java
AuditLog log = new AuditLog();
log.setUser(emp);
log.setAction("REVIEW_ACKNOWLEDGED");
log.setDetails("Acknowledged performance review");
log.setRelatedEntityType("PerformanceReview");
log.setRelatedEntityId(reviewId);
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);
```

```java
return saved;
```

---

### **ReportService.java** - Analytics & Reporting Logic

```java
@Service
public class ReportService {
    
    @Autowired
    private ReportRepository reportRepo;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private AuditLogRepository auditRepo;
    
    @Autowired
    private GoalRepository goalRepo;
    
    @Autowired
    private PerformanceReviewRepository reviewRepo;
```
- **Simple terms**: "Service for generating reports and analytics"

---

#### **Basic Retrieval Methods**

```java
public List<Report> getAllReports() {
    return reportRepo.findAll();
}

public Report getReportById(Integer reportId) {
    return reportRepo.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
}

public List<Report> getReportsByUser(Integer userId) {
    return reportRepo.findByGeneratedBy_UserIdOrderByGeneratedDateDesc(userId);
}
```

---

#### **generateReport() Method**

```java
public Report generateReport(String scope, String metrics, String format, Integer userId) {
```
- **Parameters**: What kind of report, what data, what format (PDF/Excel/CSV)
- **Simple terms**: "Generate a report with specified parameters"

```java
User user = userRepo.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
```

```java
Report report = new Report();
report.setScope(scope);
report.setMetrics(metrics);
report.setFormat(format);
report.setGeneratedBy(user);
report.setGeneratedDate(LocalDateTime.now());
report.setFilePath("/reports/" + System.currentTimeMillis() + "." + format.toLowerCase());
```
- **System.currentTimeMillis()**: Milliseconds since January 1, 1970
- **Example**: 1706023840000
- **Purpose**: Unique filename
- **.toLowerCase()**: "PDF" → "pdf"
- **Example filePath**: "/reports/1706023840000.pdf"
- **Simple terms**: "Create report record with unique filename"

```java
Report saved = reportRepo.save(report);
```

```java
AuditLog log = new AuditLog();
log.setUser(user);
log.setAction("REPORT_GENERATED");
log.setDetails("Generated " + scope + " report in " + format + " format");
log.setRelatedEntityType("Report");
log.setRelatedEntityId(saved.getReportId());
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);
```

```java
return saved;
```

**Note**: This is a stub implementation. In production, you'd actually generate the file content.

---

#### **getDashboardMetrics() Method**

```java
public Map<String, Object> getDashboardMetrics(Integer userId, String role) {
```
- **Map<String, Object>**: Dictionary with string keys and any values
- **Simple terms**: "Get role-specific dashboard statistics"

```java
Map<String, Object> metrics = new HashMap<>();
```
- **HashMap**: Implementation of Map interface
- **new HashMap<>()**: Diamond operator (type inference)
- **Simple terms**: "Create empty dictionary to store metrics"

```java
if (role.equals("EMPLOYEE")) {
```
- **Employee dashboard**: Shows their own stats

```java
List<Goal> myGoals = goalRepo.findByAssignedToUser_UserId(userId);
```
- **Simple terms**: "Get all employee's goals"

```java
long completedGoals = myGoals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
```
- **.stream()**: Convert List to Stream (for functional operations)
- **.filter()**: Keep only items matching condition
- **g ->**: Lambda parameter (each goal)
- **g.getStatus() == GoalStatus.COMPLETED**: Condition (is completed?)
- **.count()**: Count how many pass the filter
- **long**: Primitive number type (64-bit integer)

**Stream API explanation:**
```java
// Traditional way:
long completedGoals = 0;
for (Goal g : myGoals) {
    if (g.getStatus() == GoalStatus.COMPLETED) {
        completedGoals++;
    }
}

// Stream way (more concise):
long completedGoals = myGoals.stream()
    .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
    .count();
```

```java
long inProgressGoals = myGoals.stream().filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS).count();
long pendingGoals = myGoals.stream().filter(g -> g.getStatus() == GoalStatus.PENDING).count();
```
- **Simple terms**: "Count goals in different statuses"

```java
metrics.put("totalGoals", myGoals.size());
metrics.put("completedGoals", completedGoals);
metrics.put("inProgressGoals", inProgressGoals);
metrics.put("pendingGoals", pendingGoals);
metrics.put("completionRate", myGoals.size() > 0 ? (completedGoals * 100.0 / myGoals.size()) : 0);
```
- **.put()**: Add key-value pair to map
- **myGoals.size()**: List size (number of elements)
- **completedGoals * 100.0**: Convert to percentage
- **/ myGoals.size()**: Divide by total
- **100.0** (not 100): Forces floating-point division
- **Ternary**: Avoid division by zero
- **Simple terms**: "Add statistics to metrics map"

**Why 100.0 not 100?**
```java
int a = 5, b = 10;
int result1 = a * 100 / b;   // = 50 (integer division)
double result2 = a * 100.0 / b; // = 50.0 (float division)
```

```java
} else if (role.equals("MANAGER")) {
```
- **Manager dashboard**: Shows team stats

```java
List<Goal> teamGoals = goalRepo.findByAssignedManager_UserId(userId);
List<User> teamMembers = userRepo.findByManager_UserId(userId);

metrics.put("teamSize", teamMembers.size());
metrics.put("totalTeamGoals", teamGoals.size());
metrics.put("pendingApprovals", teamGoals.stream().filter(g -> g.getStatus() == GoalStatus.PENDING).count());
metrics.put("pendingCompletions", teamGoals.stream().filter(g -> g.getStatus() == GoalStatus.PENDING_COMPLETION_APPROVAL).count());
```
- **Simple terms**: "Manager sees team size and goals needing attention"

```java
} else {
```
- **Admin dashboard**: Shows org-wide stats

```java
List<User> allUsers = userRepo.findAll();
List<Goal> allGoals = goalRepo.findAll();
List<PerformanceReview> allReviews = reviewRepo.findAll();

metrics.put("totalUsers", allUsers.size());
metrics.put("totalGoals", allGoals.size());
metrics.put("totalReviews", allReviews.size());
metrics.put("completedGoals", allGoals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count());
```
- **Simple terms**: "Admin sees organization-wide statistics"

```java
}

return metrics;
```

---

#### **getPerformanceSummary() Method**

```java
public Map<String, Object> getPerformanceSummary(Integer cycleId, String dept) {
```
- **Optional filters**: By cycle and/or department
- **Simple terms**: "Get performance summary with optional filters"

```java
Map<String, Object> summary = new HashMap<>();

List<PerformanceReview> reviews;
if (cycleId != null) {
    reviews = reviewRepo.findByCycle_CycleId(cycleId);
} else {
    reviews = reviewRepo.findAll();
}
```
- **Simple terms**: "Get reviews for specific cycle or all reviews"

```java
if (dept != null && !dept.isEmpty()) {
    reviews = reviews.stream()
            .filter(r -> dept.equals(r.getUser().getDepartment()))
            .toList();
}
```
- **dept != null && !dept.isEmpty()**: Check department provided and not empty
- **.stream()**: Convert to stream
- **.filter()**: Keep only matching department
- **r.getUser().getDepartment()**: Navigate: Review → User → Department
- **.toList()**: Convert stream back to list
- **Simple terms**: "Filter reviews by department if specified"

```java
long totalReviews = reviews.size();
double avgSelfRating = reviews.stream()
        .filter(r -> r.getEmployeeSelfRating() != null)
        .mapToInt(PerformanceReview::getEmployeeSelfRating)
        .average()
        .orElse(0.0);
```
- **.filter()**: Keep only reviews with self-rating
- **.mapToInt()**: Extract integer values
- **PerformanceReview::getEmployeeSelfRating**: Method reference (shorthand for r -> r.getEmployeeSelfRating())
- **.average()**: Calculate average
- **.orElse(0.0)**: Default value if no data
- **Simple terms**: "Calculate average self-rating"

**Method reference explanation:**
```java
// Lambda way:
.mapToInt(r -> r.getEmployeeSelfRating())

// Method reference way (cleaner):
.mapToInt(PerformanceReview::getEmployeeSelfRating)
```

```java
double avgManagerRating = reviews.stream()
        .filter(r -> r.getManagerRating() != null)
        .mapToInt(PerformanceReview::getManagerRating)
        .average()
        .orElse(0.0);
```

```java
summary.put("totalReviews", totalReviews);
summary.put("avgSelfRating", avgSelfRating);
summary.put("avgManagerRating", avgManagerRating);
summary.put("cycleId", cycleId);
summary.put("department", dept);

return summary;
```

---

#### **getGoalAnalytics() Method**

```java
public Map<String, Object> getGoalAnalytics() {
```
- **Simple terms**: "Get analytics about all goals"

```java
Map<String, Object> analytics = new HashMap<>();

List<Goal> allGoals = goalRepo.findAll();
```

```java
long pending = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.PENDING).count();
long inProgress = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS).count();
long pendingCompletion = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.PENDING_COMPLETION_APPROVAL).count();
long completed = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
long rejected = allGoals.stream().filter(g -> g.getStatus() == GoalStatus.REJECTED).count();
```
- **Simple terms**: "Count goals in each status"

```java
analytics.put("totalGoals", allGoals.size());
analytics.put("pending", pending);
analytics.put("inProgress", inProgress);
analytics.put("pendingCompletion", pendingCompletion);
analytics.put("completed", completed);
analytics.put("rejected", rejected);
analytics.put("completionRate", allGoals.size() > 0 ? (completed * 100.0 / allGoals.size()) : 0);

return analytics;
```

---

#### **getDepartmentPerformance() Method**

```java
public List<Map<String, Object>> getDepartmentPerformance() {
```
- **Return type**: List of maps (one map per department)
- **Simple terms**: "Get performance metrics for each department"

```java
List<Map<String, Object>> performance = new ArrayList<>();
```
- **ArrayList**: Implementation of List interface
- **Simple terms**: "Create empty list to store department metrics"

```java
List<User> allUsers = userRepo.findAll();
List<String> departments = allUsers.stream()
        .map(User::getDepartment)
        .filter(dept -> dept != null && !dept.isEmpty())
        .distinct()
        .toList();
```
- **.map()**: Transform each item
- **User::getDepartment**: Extract department from each user
- **.filter()**: Keep only non-null, non-empty departments
- **.distinct()**: Remove duplicates
- **.toList()**: Convert to list
- **Simple terms**: "Get list of unique departments"

```java
for (String dept : departments) {
```
- **for-each loop**: Iterate through each department

```java
Map<String, Object> deptMetrics = new HashMap<>();
```
- **Simple terms**: "Create map for this department's metrics"

```java
List<User> deptUsers = userRepo.findByDepartment(dept);
List<Goal> deptGoals = new ArrayList<>();
for (User user : deptUsers) {
    deptGoals.addAll(goalRepo.findByAssignedToUser_UserId(user.getUserId()));
}
```
- **Nested loops**: For each department user, get their goals
- **.addAll()**: Add all items from another list
- **Simple terms**: "Collect all goals for users in this department"

```java
long completedGoals = deptGoals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
```

```java
deptMetrics.put("department", dept);
deptMetrics.put("employeeCount", deptUsers.size());
deptMetrics.put("totalGoals", deptGoals.size());
deptMetrics.put("completedGoals", completedGoals);
deptMetrics.put("completionRate", deptGoals.size() > 0 ? (completedGoals * 100.0 / deptGoals.size()) : 0);

performance.add(deptMetrics);
```
- **.add()**: Add map to list
- **Simple terms**: "Store this department's metrics and add to results"

```java
}

return performance;
```

---

### **ReviewCycleService.java** - Review Cycle Management

```java
@Service
public class ReviewCycleService {
    
    @Autowired
    private ReviewCycleRepository cycleRepo;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private AuditLogRepository auditRepo;
```

---

#### **Retrieval Methods**

```java
public List<ReviewCycle> getAllCycles() {
    return cycleRepo.findAll();
}

public ReviewCycle getCycleById(Integer cycleId) {
    return cycleRepo.findById(cycleId)
            .orElseThrow(() -> new ResourceNotFoundException("Review cycle not found"));
}

public ReviewCycle getActiveCycle() {
    return cycleRepo.findFirstByStatusOrderByStartDateDesc(ReviewCycleStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("No active review cycle found"));
}
```
- **getActiveCycle()**: Finds most recent active cycle
- **Simple terms**: "Get the currently active review period"

---

#### **createCycle() Method**

```java
public ReviewCycle createCycle(CreateReviewCycleRequest req, Integer adminId) {
```
- **Simple terms**: "Admin creates a new review cycle"

```java
ReviewCycle cycle = new ReviewCycle();
cycle.setTitle(req.getTitle());
cycle.setStartDate(req.getStartDt());
cycle.setEndDate(req.getEndDt());
cycle.setStatus(req.getStatus());
cycle.setRequiresCompletionApproval(req.getReqCompAppr());
cycle.setEvidenceRequired(req.getEvReq());
```
- **Simple terms**: "Create cycle with all specified settings"

```java
ReviewCycle saved = cycleRepo.save(cycle);
```

```java
User admin = userRepo.findById(adminId).orElse(null);
AuditLog log = new AuditLog();
log.setUser(admin);
log.setAction("REVIEW_CYCLE_CREATED");
log.setDetails("Created review cycle: " + cycle.getTitle());
log.setRelatedEntityType("ReviewCycle");
log.setRelatedEntityId(saved.getCycleId());
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);

return saved;
```

---

#### **updateCycle() Method**

```java
public ReviewCycle updateCycle(Integer cycleId, CreateReviewCycleRequest req, Integer adminId) {
```
- **Simple terms**: "Admin updates existing review cycle"

```java
ReviewCycle cycle = getCycleById(cycleId);

cycle.setTitle(req.getTitle());
cycle.setStartDate(req.getStartDt());
cycle.setEndDate(req.getEndDt());
cycle.setStatus(req.getStatus());
cycle.setRequiresCompletionApproval(req.getReqCompAppr());
cycle.setEvidenceRequired(req.getEvReq());

ReviewCycle updated = cycleRepo.save(cycle);
```

```java
User admin = userRepo.findById(adminId).orElse(null);
AuditLog log = new AuditLog();
log.setUser(admin);
log.setAction("REVIEW_CYCLE_UPDATED");
log.setDetails("Updated review cycle: " + cycle.getTitle());
log.setRelatedEntityType("ReviewCycle");
log.setRelatedEntityId(cycleId);
log.setStatus("SUCCESS");
log.setTimestamp(LocalDateTime.now());
auditRepo.save(log);

return updated;
```

---

## 🔐 8. SECURITY LAYER

### **JwtAuthFilter.java** - JWT Authentication Filter

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
```
- **@Component**: Spring-managed component
- **extends OncePerRequestFilter**: Runs once per HTTP request
- **Why?**: Without "Once", filter might run multiple times
- **Simple terms**: "Custom filter that validates JWT tokens on every request"

```java
@Autowired
private JwtUtil jwtUtil;
```
- **Simple terms**: "Inject JWT utility for token validation"

---

#### **doFilterInternal() Method**

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
```
- **@Override**: Overriding method from parent class
- **HttpServletRequest request**: Incoming HTTP request
- **HttpServletResponse response**: HTTP response to send back
- **FilterChain filterChain**: Chain of filters to execute
- **throws**: This method can throw exceptions
- **Simple terms**: "Process each HTTP request for authentication"

```java
String authHeader = request.getHeader("Authorization");
```
- **getHeader()**: Get HTTP header value
- **"Authorization"**: Header name
- **Example**: "Authorization: Bearer eyJhbGciOi..."
- **Simple terms**: "Get Authorization header from request"

```java
String token = null;
String email = null;
```
- **Declare variables**: Initialize to null
- **Simple terms**: "Prepare variables for token and email"

```java
if (authHeader != null && authHeader.startsWith("Bearer ")) {
```
- **authHeader != null**: Check header exists
- **&&**: AND operator
- **authHeader.startsWith("Bearer ")**: Check format is correct
- **Why "Bearer "?**: Standard format for JWT tokens
- **Simple terms**: "Check if Authorization header exists and has correct format"

```java
token = authHeader.substring(7);
```
- **.substring(7)**: Extract string starting from position 7
- **Why 7?**: "Bearer " is 7 characters long
- **Example**: "Bearer eyJhbGci..." → "eyJhbGci..."
- **Simple terms**: "Remove 'Bearer ' prefix to get actual token"

**String positions:**
```
"Bearer eyJhbGci..."
 0123456789...
        ^
        Position 7 (start of token)
```

```java
try {
    email = jwtUtil.extractEmail(token);
```
- **try-catch**: Handle potential errors
- **jwtUtil.extractEmail()**: Decode token and extract email
- **Simple terms**: "Try to extract email from token"

```java
} catch (Exception e) {
    // Invalid token, continue without authentication
}
```
- **catch (Exception e)**: Catch any error
- **e**: Exception object (not used here)
- **No action**: Just continue processing (user remains unauthenticated)
- **Simple terms**: "If token is invalid, just continue without setting authentication"

```java
}
```
- **End of if block**

```java
if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
```
- **email != null**: Successfully extracted email
- **SecurityContextHolder**: Spring Security's authentication holder
- **.getContext()**: Get current security context
- **.getAuthentication()**: Get current authentication (if any)
- **== null**: No authentication set yet
- **Why check?**: Avoid re-processing if already authenticated
- **Simple terms**: "If we have email and user not yet authenticated"

```java
if (jwtUtil.validateToken(token, email)) {
```
- **validateToken()**: Check token is valid and matches email
- **Checks**: Signature valid, not expired, email matches
- **Simple terms**: "Verify token is valid and belongs to this email"

```java
String role = jwtUtil.extractRole(token);
Integer userId = jwtUtil.extractUserId(token);
```
- **Simple terms**: "Extract user role and ID from token"

```java
UsernamePasswordAuthenticationToken authToken = 
    new UsernamePasswordAuthenticationToken(
        email, 
        null, 
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
    );
```
- **UsernamePasswordAuthenticationToken**: Spring Security's authentication object
- **email**: Principal (who the user is)
- **null**: Credentials (we don't need password here)
- **Collections.singletonList()**: Create list with single item
- **new SimpleGrantedAuthority()**: Create authority/role
- **"ROLE_" + role**: Prefix required by Spring Security
- **Example**: "EMPLOYEE" → "ROLE_EMPLOYEE"
- **Simple terms**: "Create authentication object with email and role"

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```
- **setDetails()**: Add extra info (IP address, session ID, etc.)
- **WebAuthenticationDetailsSource**: Creates details from request
- **Simple terms**: "Add request details to authentication"

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```
- **setAuthentication()**: Store authentication in security context
- **Effect**: User is now authenticated for this request
- **Simple terms**: "Tell Spring Security this user is authenticated"

```java
request.setAttribute("userId", userId);
request.setAttribute("userRole", role);
```
- **setAttribute()**: Store data in request
- **Why?**: Controllers can easily access userId and role
- **Simple terms**: "Store userId and role in request for controllers to use"

```java
}
}
```
- **End of validation checks**

```java
filterChain.doFilter(request, response);
```
- **doFilter()**: Pass request to next filter in chain
- **CRITICAL**: Must call this or request will hang
- **Simple terms**: "Continue processing request through filter chain"

```java
}
```
- **End of method**

**Filter chain visualization:**
```
Request → JwtAuthFilter → UsernamePasswordAuthenticationFilter → ... → Controller
                ↓
          Validates JWT
                ↓
    Sets authentication in SecurityContext
                ↓
          Calls next filter
```

---

### **JwtUtil.java** - JWT Token Utility

```java
@Component
public class JwtUtil {
```
- **@Component**: Spring-managed component
- **Simple terms**: "Utility class for JWT operations"

```java
private static final String SECRET = "MySecretKeyForPerformanceTrackApp2026VeryLongSecretKey";
```
- **static final**: Constant (never changes)
- **String SECRET**: Secret key for signing tokens
- **SECURITY NOTE**: In production, use environment variable!
- **Simple terms**: "Secret key for encrypting/decrypting tokens"

```java
private static final long EXPIRATION = 86400000; // 24 hours in milliseconds
```
- **86400000**: 24 * 60 * 60 * 1000 = 24 hours
- **Simple terms**: "Tokens expire after 24 hours"

**Time calculation:**
```
24 hours × 60 minutes × 60 seconds × 1000 milliseconds = 86,400,000 ms
```

```java
private Key getSignKey() {
    return Keys.hmacShaKeyFor(SECRET.getBytes());
}
```
- **private**: Only this class can use
- **Key**: Cryptographic key type
- **Keys.hmacShaKeyFor()**: Create HMAC key
- **SECRET.getBytes()**: Convert string to bytes
- **Simple terms**: "Convert secret string to cryptographic key"

---

#### **generateToken() Method**

```java
public String generateToken(String email, Integer userId, String role) {
```
- **Returns**: JWT token string
- **Parameters**: User data to include in token
- **Simple terms**: "Create a new JWT token"

```java
Map<String, Object> claims = new HashMap<>();
claims.put("userId", userId);
claims.put("role", role);
```
- **claims**: Data stored in token
- **Simple terms**: "Create data to store in token"

```java
return Jwts.builder()
```
- **Jwts.builder()**: Start building JWT
- **Builder pattern**: Chain method calls
- **Simple terms**: "Start creating JWT"

```java
.setClaims(claims)
```
- **setClaims()**: Add custom data
- **Simple terms**: "Add userId and role to token"

```java
.setSubject(email)
```
- **setSubject()**: Set main identifier (who token is for)
- **Simple terms**: "Set email as token subject"

```java
.setIssuedAt(new Date(System.currentTimeMillis()))
```
- **setIssuedAt()**: When token was created
- **new Date()**: Create date object
- **System.currentTimeMillis()**: Current time in milliseconds
- **Simple terms**: "Record when token was created"

```java
.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
```
- **setExpiration()**: When token expires
- **currentTime + EXPIRATION**: Current time + 24 hours
- **Simple terms**: "Set token to expire in 24 hours"

```java
.signWith(getSignKey(), SignatureAlgorithm.HS256)
```
- **signWith()**: Sign token with secret key
- **SignatureAlgorithm.HS256**: Use HMAC-SHA256 algorithm
- **Why sign?**: Prevents tampering
- **Simple terms**: "Sign token so it can't be modified"

```java
.compact();
```
- **compact()**: Build final token string
- **Returns**: Base64-encoded JWT
- **Simple terms**: "Finalize and return token string"

---

#### **Extract Methods**

```java
public String extractEmail(String token) {
    return extractClaims(token).getSubject();
}
```
- **extractClaims()**: Decode token
- **.getSubject()**: Get subject (email)
- **Simple terms**: "Get email from token"

```java
public Integer extractUserId(String token) {
    return (Integer) extractClaims(token).get("userId");
}
```
- **.get("userId")**: Get custom claim
- **(Integer)**: Cast to Integer
- **Simple terms**: "Get userId from token"

```java
public String extractRole(String token) {
    return (String) extractClaims(token).get("role");
}
```
- **Simple terms**: "Get role from token"

---

#### **extractClaims() Method**

```java
private Claims extractClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
}
```
- **private**: Internal method
- **Claims**: JWT claims object
- **parserBuilder()**: Start building parser
- **setSigningKey()**: Set key for verification
- **build()**: Create parser
- **parseClaimsJws()**: Parse and validate token
- **getBody()**: Get claims data
- **Simple terms**: "Decode token and extract data"

**What happens if token is invalid?**
- Throws exception (caught in filter)
- User remains unauthenticated

---

#### **validateToken() Method**

```java
public Boolean validateToken(String token, String email) {
    final String tokenEmail = extractEmail(token);
    return (tokenEmail.equals(email) && !isTokenExpired(token));
}
```
- **final**: Variable can't be reassigned
- **extractEmail()**: Get email from token
- **&&**: Both conditions must be true
- **!isTokenExpired()**: NOT expired
- **Simple terms**: "Check token is for this email and not expired"

---

#### **isTokenExpired() Method**

```java
private Boolean isTokenExpired(String token) {
    return extractClaims(token).getExpiration().before(new Date());
}
```
- **.getExpiration()**: Get expiration date from token
- **.before()**: Compare dates
- **new Date()**: Current date/time
- **Simple terms**: "Check if token expiration is before now"

---

## ⚠️ 9. EXCEPTION HANDLING

### **Custom Exception Classes**

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```
- **extends RuntimeException**: Unchecked exception
- **super(message)**: Call parent constructor
- **Simple terms**: "Exception thrown when resource (user/goal/etc) not found"

```java
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```
- **Simple terms**: "Exception for unauthorized access"

```java
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```
- **Simple terms**: "Exception for invalid requests"

---

### **GlobalExceptionHandler.java** - Centralized Error Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
```
- **@RestControllerAdvice**: Applies to all controllers
- **Centralized**: All errors handled in one place
- **Simple terms**: "Global error handler for entire application"

---

#### **handleNotFound() Method**

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
```
- **@ExceptionHandler**: Handle specific exception type
- **ResponseEntity**: Wrapper for HTTP response
- **<ApiResponse<Object>>**: Response body type
- **Simple terms**: "Handle ResourceNotFoundException"

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
}
```
- **ResponseEntity.status()**: Set HTTP status code
- **HttpStatus.NOT_FOUND**: 404 status
- **.body()**: Set response body
- **ApiResponse.error()**: Create error response
- **ex.getMessage()**: Get exception message
- **Simple terms**: "Return 404 error with message"

**HTTP Response:**
```
Status: 404 Not Found
Body: {
  "status": "error",
  "msg": "User not found",
  "data": null
}
```

---

#### **handleUnauthorized() Method**

```java
@ExceptionHandler(UnauthorizedException.class)
public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
}
```
- **HttpStatus.UNAUTHORIZED**: 401 status
- **Simple terms**: "Return 401 error for unauthorized access"

---

#### **handleBadRequest() Method**

```java
@ExceptionHandler(BadRequestException.class)
public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
}
```
- **HttpStatus.BAD_REQUEST**: 400 status
- **Simple terms**: "Return 400 error for bad requests"

---

#### **handleValidation() Method**

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
```
- **MethodArgumentNotValidException**: Validation failed (@NotBlank, @Email, etc.)
- **Simple terms**: "Handle validation errors from @Valid annotation"

```java
Map<String, String> errors = new HashMap<>();
ex.getBindingResult().getAllErrors().forEach(error -> {
```
- **getBindingResult()**: Get validation results
- **getAllErrors()**: Get all validation errors
- **.forEach()**: Loop through each error
- **error ->**: Lambda parameter (each validation error)

```java
String fieldName = ((FieldError) error).getField();
String errorMsg = error.getDefaultMessage();
errors.put(fieldName, errorMsg);
```
- **(FieldError) error**: Cast to FieldError
- **.getField()**: Get field name (e.g., "email")
- **.getDefaultMessage()**: Get error message (e.g., "Email is required")
- **errors.put()**: Add to map
- **Simple terms**: "Extract field name and error message"

**Example errors map:**
```java
{
  "email": "Email is required",
  "password": "Password is required",
  "name": "Name must not be blank"
}
```

```java
});
return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("Validation failed: " + errors));
}
```
- **Simple terms**: "Return 400 error with all validation errors"

---

#### **handleGeneral() Method**

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An error occurred: " + ex.getMessage()));
}
```
- **Exception.class**: Catch all exceptions
- **HttpStatus.INTERNAL_SERVER_ERROR**: 500 status
- **Fallback**: Catches any unexpected errors
- **Simple terms**: "Catch-all handler for unexpected errors"

---

## 🚀 10. MAIN APPLICATION CLASS

### **PerformanceTrackApplication.java**

```java
package com.project.performanceTrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
```
- **Package declaration**: Root package
- **Imports**: Spring Boot classes

```java
@SpringBootApplication
public class PerformanceTrackApplication {
```
- **@SpringBootApplication**: Combines three annotations:
  1. **@Configuration**: Class can define beans
  2. **@EnableAutoConfiguration**: Auto-configure based on dependencies
  3. **@ComponentScan**: Scan for components in this package and sub-packages
- **Simple terms**: "Main Spring Boot application class"

**What ComponentScan does:**
```
Scans for:
- @Controller
- @Service
- @Repository
- @Component
- @Configuration

In packages:
com.project.performanceTrack
com.project.performanceTrack.controller
com.project.performanceTrack.service
...
```

```java
public static void main(String[] args) {
```
- **public**: Accessible from anywhere
- **static**: Can run without creating object
- **void**: Doesn't return anything
- **main**: Method name (required)
- **String[] args**: Command-line arguments
- **Simple terms**: "Entry point of application"

```java
SpringApplication.run(PerformanceTrackApplication.class, args);
```
- **SpringApplication.run()**: Start Spring Boot application
- **PerformanceTrackApplication.class**: Main class
- **args**: Pass command-line arguments
- **What happens**:
  1. Scan for components
  2. Create application context
  3. Configure beans
  4. Start embedded Tomcat server
  5. Deploy application
  6. Listen for HTTP requests on port 8080 (default)
- **Simple terms**: "Start the Spring Boot application"

```java
}
}
```

---

## 🎯 COMPLETE APPLICATION FLOW

### **Example: Employee Creates a Goal**

**1. User sends request:**
```
POST /api/v1/goals
Authorization: Bearer eyJhbGci...
Body: {
  "title": "Learn Spring Boot",
  "desc": "Complete 3 projects",
  ...
}
```

**2. JwtAuthFilter intercepts:**
- Extracts token from Authorization header
- Validates token
- Sets authentication in SecurityContext
- Stores userId and userRole in request attributes

**3. SecurityFilterChain checks:**
- Is endpoint accessible? (Yes, authenticated users)
- Does user have required role? (Yes, EMPLOYEE)

**4. Request reaches GoalController:**
```java
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<Goal> createGoal(@Valid @RequestBody CreateGoalRequest req,
                                    HttpServletRequest httpReq) {
```
- Spring validates request body (@Valid)
- Controller extracts userId from request
- Calls GoalService

**5. GoalService processes:**
```java
public Goal createGoal(CreateGoalRequest req, Integer empId) {
```
- Validates business rules
- Creates Goal entity
- Saves to database via GoalRepository
- Creates notification for manager
- Creates audit log
- Returns saved goal

**6. Response sent back:**
```json
{
  "status": "success",
  "msg": "Goal created",
  "data": {
    "goalId": 42,
    "title": "Learn Spring Boot",
    ...
  }
}
```

**7. If error occurs:**
- Exception thrown (e.g., ResourceNotFoundException)
- GlobalExceptionHandler catches it
- Returns appropriate error response

---

## 📊 ARCHITECTURE SUMMARY

```
┌─────────────────────────────────────────────────────┐
│                    HTTP REQUEST                      │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│              JwtAuthFilter                           │
│  - Validates JWT token                               │
│  - Sets authentication                               │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│           SecurityFilterChain                        │
│  - Checks URL access rules                           │
│  - Verifies user roles                               │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│               CONTROLLER                             │
│  - Receives request                                  │
│  - Validates input (@Valid)                          │
│  - Extracts parameters                               │
│  - Calls service                                     │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│                SERVICE                               │
│  - Business logic                                    │
│  - Validation                                        │
│  - Calls repositories                                │
│  - Creates notifications                             │
│  - Creates audit logs                                │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│              REPOSITORY                              │
│  - Database queries                                  │
│  - Spring Data JPA                                   │
│  - Returns entities                                  │
└─────────────────────┬───────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│               DATABASE                               │
│  - MySQL/PostgreSQL                                  │
│  - Stores data                                       │
└─────────────────────────────────────────────────────┘
```

---

## 🎓 KEY CONCEPTS SUMMARY

### **Annotations**
- **@Service**: Business logic class
- **@Repository**: Database access class
- **@RestController**: API endpoint class
- **@Autowired**: Inject dependency
- **@Valid**: Validate request body
- **@PreAuthorize**: Check user role before method

### **Spring Data JPA**
- **findByFieldName**: Auto-generates query
- **save()**: Insert or update
- **findById()**: Find by primary key
- **Optional**: May or may not contain value

### **Stream API**
- **.stream()**: Convert to stream
- **.filter()**: Keep matching items
- **.map()**: Transform items
- **.count()**: Count items
- **.toList()**: Convert back to list

### **Exception Handling**
- **throw**: Raise exception
- **try-catch**: Handle exception
- **@ExceptionHandler**: Global error handler

### **JWT Authentication**
- **Token contains**: Email, userId, role
- **Filter validates**: Every request
- **SecurityContext**: Stores authentication
- **No sessions**: Stateless

---

This completes the **extreme detailed explanation** of your entire PerformanceTrack backend! Every line of code has been explained in simple terms with examples and context. 

Is there any specific part you'd like me to explain further or any questions about how it all works together?