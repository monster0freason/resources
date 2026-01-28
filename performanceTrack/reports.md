```java
package com.project.performanceTrack.service;

import com.project.performanceTrack.entity.AuditLog;
import com.project.performanceTrack.entity.Report;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.entity.Goal;
import com.project.performanceTrack.entity.PerformanceReview;
import com.project.performanceTrack.enums.GoalStatus;
import com.project.performanceTrack.exception.ResourceNotFoundException;
import com.project.performanceTrack.repository.ReportRepository;
import com.project.performanceTrack.repository.UserRepository;
import com.project.performanceTrack.repository.AuditLogRepository;
import com.project.performanceTrack.repository.GoalRepository;
import com.project.performanceTrack.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * ReportService - Business logic layer for Analytics and Reporting Module
 * 
 * PURPOSE:
 * This service is the brain of the analytics system in PerformanceTrack.
 * It handles all calculations, data aggregations, and business logic for:
 * - Report generation and metadata storage
 * - Role-based dashboard metrics
 * - Performance analytics across users, departments, and cycles
 * - Goal tracking and completion analytics
 * 
 * ARCHITECTURE:
 * Service Layer → Repository Layer → Database
 * Controller calls this service → Service processes business logic → Repositories fetch data
 * 
 * ROLE-BASED ACCESS:
 * - ADMIN: Full system-wide analytics and reports
 * - MANAGER: Team-level metrics and reports for direct reports
 * - EMPLOYEE: Personal dashboard with individual goal metrics
 * 
 * KEY FEATURES:
 * 1. Report Generation: Create and track report metadata with audit logging
 * 2. Dashboard Metrics: Role-specific KPIs for quick insights
 * 3. Performance Summary: Aggregated review ratings by cycle/department
 * 4. Goal Analytics: Organization-wide goal status breakdown
 * 5. Department Performance: Comparative metrics across departments
 */
@Service  // Marks this as a Spring Service component for dependency injection
public class ReportService {
    
    // ============================================
    // DEPENDENCY INJECTION
    // ============================================
    // Spring automatically injects these repository beans at runtime
    // These repositories provide database access without writing SQL queries
    
    @Autowired
    private ReportRepository reportRepo;  
    // Purpose: Store and retrieve Report entities (metadata about generated reports)
    // Usage: Save report records, fetch by ID, fetch by user
    
    @Autowired
    private UserRepository userRepo;  
    // Purpose: Access User/Employee data and organizational relationships
    // Usage: Validate users, fetch team members, get department lists
    
    @Autowired
    private AuditLogRepository auditRepo;  
    // Purpose: Create audit trail entries for compliance and tracking
    // Usage: Log all report generation activities for security audits
    
    @Autowired
    private GoalRepository goalRepo;  
    // Purpose: Fetch Goal data for analytics calculations
    // Usage: Get goals by user/manager/status for metrics and dashboards
    
    @Autowired
    private PerformanceReviewRepository reviewRepo;  
    // Purpose: Access PerformanceReview data for rating analytics
    // Usage: Calculate average ratings, review counts by cycle/department
    
    // ============================================
    // REPORT CRUD OPERATIONS
    // ============================================
    
    /**
     * Get all reports in the system
     * 
     * USE CASE:
     * Admin dashboard showing complete report history across all users
     * Useful for: Report library, audit views, system monitoring
     * 
     * FLOW:
     * 1. Call repository's findAll() method
     * 2. JPA fetches all Report records from database
     * 3. Return complete list to controller
     * 
     * SECURITY:
     * Controller has @PreAuthorize ensuring only ADMIN/MANAGER can call this
     * 
     * @return List of all Report entities in database
     */
    public List<Report> getAllReports() {
        return reportRepo.findAll();  // JPA built-in method for SELECT * FROM reports
    }
    
    /**
     * Get a specific report by its ID
     * 
     * USE CASE:
     * View/download a previously generated report
     * Click on report in history → fetch details → show download link
     * 
     * FLOW:
     * 1. Call repository's findById() method
     * 2. If found: return the Report entity
     * 3. If not found: throw ResourceNotFoundException (returns 404 to client)
     * 
     * ERROR HANDLING:
     * Optional.orElseThrow() pattern ensures we never return null
     * Exception is caught by @ControllerAdvice and converted to proper HTTP response
     * 
     * @param reportId The unique identifier (primary key) of the report
     * @return Report entity if exists
     * @throws ResourceNotFoundException if report with given ID doesn't exist
     */
    public Report getReportById(Integer reportId) {
        return reportRepo.findById(reportId)  // Returns Optional<Report>
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        // Lambda expression creates exception only if Optional is empty
    }
    
    /**
     * Get all reports generated by a specific user
     * 
     * USE CASE:
     * "My Reports" page showing user's report generation history
     * Results sorted by newest first for better UX
     * 
     * FLOW:
     * 1. Call custom repository method with userId
     * 2. JPA generates query: SELECT * FROM reports WHERE generated_by = ? ORDER BY generated_date DESC
     * 3. Return sorted list to controller
     * 
     * QUERY EXPLANATION:
     * - findByGeneratedBy_UserId: Navigate the relationship (Report → User → userId)
     * - OrderByGeneratedDateDesc: Sort by generatedDate field descending
     * Spring Data JPA automatically generates this query from the method name
     * 
     * @param userId The ID of the user who generated the reports
     * @return List of reports by that user, newest first
     */
    public List<Report> getReportsByUser(Integer userId) {
        return reportRepo.findByGeneratedBy_UserIdOrderByGeneratedDateDesc(userId);
    }
    
    /**
     * Generate a new report and save its metadata
     * 
     * USE CASE:
     * Manager/Admin clicks "Generate Report" button with selected parameters
     * System creates report metadata, logs the action, returns confirmation
     * 
     * NOTE: This is a PLACEHOLDER implementation
     * In production, this would:
     * 1. Fetch actual data based on scope/metrics
     * 2. Generate PDF/Excel file using libraries (JasperReports, Apache POI)
     * 3. Save file to cloud storage (S3, Azure Blob)
     * 4. Store the actual file URL in filePath
     * 
     * CURRENT IMPLEMENTATION:
     * Only saves report metadata (what was requested, when, by whom)
     * Actual file generation would be added in sprint 2/3
     * 
     * FLOW:
     * 1. Validate user exists in database
     * 2. Create new Report entity and populate fields
     * 3. Generate placeholder file path
     * 4. Save report to database (gets auto-generated ID)
     * 5. Create audit log entry for compliance tracking
     * 6. Return saved report to controller
     * 
     * PARAMETERS:
     * @param scope Type/category of report (e.g., "Employee Performance", "Goal Analytics", "Department Summary")
     * @param metrics Comma-separated list of metrics to include (e.g., "completion_rate,avg_rating,goal_count")
     * @param format Output format (e.g., "PDF", "Excel", "CSV")
     * @param userId ID of user requesting report generation (from JWT token)
     * 
     * @return The saved Report entity with generated reportId and metadata
     * @throws ResourceNotFoundException if userId is invalid
     */
    public Report generateReport(String scope, String metrics, String format, Integer userId) {
        // STEP 1: Validate user exists
        // If user doesn't exist, this throws exception and stops execution
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // STEP 2: Create report entity and populate fields
        Report report = new Report();
        report.setScope(scope);           // What the report covers (subject matter)
        report.setMetrics(metrics);       // Which metrics/KPIs are included
        report.setFormat(format);         // Output format (PDF/Excel/CSV)
        report.setGeneratedBy(user);      // Who requested it (foreign key to User)
        report.setGeneratedDate(LocalDateTime.now());  // Timestamp of generation
        
        // STEP 3: Generate file path
        // Uses current timestamp to ensure unique filename
        // In production: This would be actual cloud storage URL after file upload
        // Example: "/reports/1706451234567.pdf"
        report.setFilePath("/reports/" + System.currentTimeMillis() + "." + format.toLowerCase());
        
        // STEP 4: Save report to database
        // JPA automatically generates INSERT statement
        // Database auto-generates reportId (primary key)
        Report saved = reportRepo.save(report);
        
        // STEP 5: Create audit log for compliance
        // Audit logs track all important actions for security and compliance
        AuditLog log = new AuditLog();
        log.setUser(user);                          // Who performed the action
        log.setAction("REPORT_GENERATED");          // What action was performed
        log.setDetails("Generated " + scope + " report in " + format + " format");  // Action details
        log.setRelatedEntityType("Report");         // What type of entity was affected
        log.setRelatedEntityId(saved.getReportId());  // Specific entity ID (links to report)
        log.setStatus("SUCCESS");                   // Whether action succeeded
        log.setTimestamp(LocalDateTime.now());      // When it happened
        auditRepo.save(log);  // Save audit entry to database
        
        // STEP 6: Return saved report with generated ID
        return saved;
    }
    
    // ============================================
    // DASHBOARD METRICS (ROLE-BASED)
    // ============================================
    
    /**
     * Get dashboard metrics customized for user's role
     * 
     * PURPOSE:
     * Provide role-specific KPIs for dashboard home page
     * Each role sees metrics relevant to their responsibilities
     * 
     * ARCHITECTURE DECISION:
     * Using if-else on role instead of strategy pattern or separate methods because:
     * 1. Only 3 roles (manageable complexity)
     * 2. All metrics returned in same format (Map<String, Object>)
     * 3. Single endpoint simplifies frontend logic
     * 
     * ROLE-SPECIFIC METRICS:
     * 
     * EMPLOYEE:
     * - totalGoals: Count of all goals assigned to them
     * - completedGoals: Count of completed goals
     * - inProgressGoals: Count of goals being worked on
     * - pendingGoals: Count of goals waiting for manager approval
     * - completionRate: Percentage of goals completed
     * 
     * MANAGER:
     * - teamSize: Number of direct reports
     * - totalTeamGoals: All goals assigned to team members
     * - pendingApprovals: Goals waiting for manager's initial approval
     * - pendingCompletions: Goals marked complete, waiting for manager verification
     * 
     * ADMIN:
     * - totalUsers: All users in system
     * - totalGoals: All goals across organization
     * - totalReviews: All performance reviews
     * - completedGoals: Count of completed goals system-wide
     * 
     * USAGE:
     * Frontend calls GET /api/v1/reports/dashboard
     * → JWT filter extracts userId and role
     * → Controller passes to this method
     * → Returns appropriate metrics
     * → Frontend renders dashboard cards
     * 
     * @param userId ID of user viewing dashboard (from JWT token)
     * @param role Role of user ("EMPLOYEE", "MANAGER", "ADMIN" - from JWT token)
     * @return Map containing relevant metrics for that role
     */
    public Map<String, Object> getDashboardMetrics(Integer userId, String role) {
        // Create empty map to hold metrics
        // Using Map<String, Object> for flexibility (values can be numbers, strings, etc.)
        Map<String, Object> metrics = new HashMap<>();
        
        if (role.equals("EMPLOYEE")) {
            // ========== EMPLOYEE DASHBOARD ==========
            // Focus: Personal goal tracking and progress
            
            // Fetch all goals assigned to this employee
            // Repository method: findByAssignedToUser_UserId
            // SQL: SELECT * FROM goals WHERE assigned_to_user_id = ?
            List<Goal> myGoals = goalRepo.findByAssignedToUser_UserId(userId);
            
            // Count goals by status using Java 8 Streams API
            // Stream: Convert list to stream for functional operations
            // filter: Keep only goals matching the condition
            // count: Return number of matching elements
            
            long completedGoals = myGoals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.COMPLETED)  // Keep only COMPLETED goals
                    .count();  // Count matching goals
            
            long inProgressGoals = myGoals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)  // Keep only IN_PROGRESS goals
                    .count();
            
            long pendingGoals = myGoals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.PENDING)  // Keep only PENDING goals
                    .count();
            
            // Populate metrics map with employee data
            metrics.put("totalGoals", myGoals.size());  // Total count of goals
            metrics.put("completedGoals", completedGoals);
            metrics.put("inProgressGoals", inProgressGoals);
            metrics.put("pendingGoals", pendingGoals);
            
            // Calculate completion rate percentage
            // Ternary operator: condition ? valueIfTrue : valueIfFalse
            // Check myGoals.size() > 0 to avoid division by zero
            // Formula: (completed / total) * 100
            metrics.put("completionRate", 
                    myGoals.size() > 0 ? (completedGoals * 100.0 / myGoals.size()) : 0);
            // Using 100.0 (double) ensures floating-point division for accurate percentage
            
        } else if (role.equals("MANAGER")) {
            // ========== MANAGER DASHBOARD ==========
            // Focus: Team oversight and pending actions
            
            // Fetch all goals where this user is the assigned manager
            // Repository method: findByAssignedManager_UserId
            // SQL: SELECT * FROM goals WHERE assigned_manager_id = ?
            List<Goal> teamGoals = goalRepo.findByAssignedManager_UserId(userId);
            
            // Fetch all employees reporting to this manager
            // Repository method: findByManager_UserId
            // SQL: SELECT * FROM users WHERE manager_id = ?
            List<User> teamMembers = userRepo.findByManager_UserId(userId);
            
            // Populate metrics map with manager data
            metrics.put("teamSize", teamMembers.size());  // Number of direct reports
            metrics.put("totalTeamGoals", teamGoals.size());  // All team goals
            
            // Count goals requiring manager action
            // PENDING: New goals waiting for manager's initial approval
            metrics.put("pendingApprovals", 
                    teamGoals.stream()
                            .filter(g -> g.getStatus() == GoalStatus.PENDING)
                            .count());
            
            // PENDING_COMPLETION_APPROVAL: Employee marked goal as complete,
            // now waiting for manager to verify and approve completion
            metrics.put("pendingCompletions", 
                    teamGoals.stream()
                            .filter(g -> g.getStatus() == GoalStatus.PENDING_COMPLETION_APPROVAL)
                            .count());
            
        } else {
            // ========== ADMIN DASHBOARD ==========
            // Focus: System-wide overview and statistics
            
            // Fetch all entities for organization-wide metrics
            // These are typically smaller datasets for dashboard quick stats
            List<User> allUsers = userRepo.findAll();  // All employees in system
            List<Goal> allGoals = goalRepo.findAll();  // All goals across organization
            List<PerformanceReview> allReviews = reviewRepo.findAll();  // All performance reviews
            
            // Populate metrics map with admin data
            metrics.put("totalUsers", allUsers.size());  // Total employee count
            metrics.put("totalGoals", allGoals.size());  // Total goal count
            metrics.put("totalReviews", allReviews.size());  // Total review count
            
            // Count completed goals across entire organization
            metrics.put("completedGoals", 
                    allGoals.stream()
                            .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                            .count());
            
            // Admin could also see additional metrics like:
            // - Active performance cycles
            // - Recent system activity
            // - Department distribution
            // These can be added in future sprints based on requirements
        }
        
        // Return the populated metrics map
        // Controller wraps this in ApiResponse and sends as JSON to frontend
        return metrics;
    }
    
    // ============================================
    // PERFORMANCE SUMMARY
    // ============================================
    
    /**
     * Get aggregated performance review statistics
     * 
     * PURPOSE:
     * Provide high-level performance metrics for analysis
     * Supports filtering by performance cycle and/or department
     * 
     * USE CASES:
     * 1. View performance summary for Q1 2024 cycle
     * 2. Compare Engineering dept performance across all cycles
     * 3. View organization-wide performance for specific cycle
     * 4. Annual performance report generation
     * 
     * FILTERING LOGIC:
     * - If cycleId provided: Filter reviews by that cycle only
     * - If dept provided: Further filter by department
     * - If both null: Show all reviews system-wide
     * 
     * METRICS CALCULATED:
     * - totalReviews: Count of reviews matching filters
     * - avgSelfRating: Average of employee self-ratings (1-5 scale)
     * - avgManagerRating: Average of manager ratings (1-5 scale)
     * 
     * RATING SCALE (typically):
     * 1 = Needs Improvement
     * 2 = Below Expectations
     * 3 = Meets Expectations
     * 4 = Exceeds Expectations
     * 5 = Outstanding
     * 
     * GAP ANALYSIS:
     * Comparing avgSelfRating vs avgManagerRating can reveal:
     * - Self-rating > Manager-rating: Employee may be overconfident
     * - Manager-rating > Self-rating: Employee may be underestimating performance
     * - Similar ratings: Good alignment and realistic self-assessment
     * 
     * @param cycleId (Optional) Performance cycle ID to filter by
     * @param dept (Optional) Department name to filter by
     * @return Map containing performance summary metrics
     */
    public Map<String, Object> getPerformanceSummary(Integer cycleId, String dept) {
        // Initialize result map
        Map<String, Object> summary = new HashMap<>();
        
        // STEP 1: Fetch reviews based on cycleId filter
        List<PerformanceReview> reviews;
        if (cycleId != null) {
            // Fetch reviews for specific cycle only
            // Repository method: findByCycle_CycleId
            // SQL: SELECT * FROM performance_reviews WHERE cycle_id = ?
            reviews = reviewRepo.findByCycle_CycleId(cycleId);
        } else {
            // Fetch ALL reviews (no cycle filter)
            reviews = reviewRepo.findAll();
        }
        
        // STEP 2: Apply department filter if provided
        // Using Java Streams for in-memory filtering
        if (dept != null && !dept.isEmpty()) {
            reviews = reviews.stream()
                    // Navigate relationship: PerformanceReview → User → Department
                    .filter(r -> dept.equals(r.getUser().getDepartment()))
                    .toList();  // Convert stream back to List (Java 16+)
            // Alternative for older Java versions: .collect(Collectors.toList())
        }
        
        // STEP 3: Calculate metrics from filtered reviews
        
        // Count total reviews
        long totalReviews = reviews.size();
        
        // Calculate average self-rating
        // Stream pipeline: reviews → filter non-null → map to int → calculate average
        double avgSelfRating = reviews.stream()
                .filter(r -> r.getEmployeeSelfRating() != null)  // Exclude reviews without self-rating
                .mapToInt(PerformanceReview::getEmployeeSelfRating)  // Extract rating value
                .average()  // Calculate average (returns OptionalDouble)
                .orElse(0.0);  // Default to 0.0 if no valid ratings found
        
        // Calculate average manager rating
        // Same logic as self-rating
        double avgManagerRating = reviews.stream()
                .filter(r -> r.getManagerRating() != null)  // Exclude reviews without manager rating
                .mapToInt(PerformanceReview::getManagerRating)  // Extract rating value
                .average()  // Calculate average
                .orElse(0.0);  // Default to 0.0 if no valid ratings
        
        // STEP 4: Populate summary map
        summary.put("totalReviews", totalReviews);
        summary.put("avgSelfRating", avgSelfRating);
        summary.put("avgManagerRating", avgManagerRating);
        summary.put("cycleId", cycleId);  // Echo back filter parameters for context
        summary.put("department", dept);
        
        // Return summary to controller
        return summary;
    }
    
    // ============================================
    // GOAL ANALYTICS
    // ============================================
    
    /**
     * Get comprehensive goal analytics across the organization
     * 
     * PURPOSE:
     * Provide system-wide view of goal distribution and completion
     * Helps admins understand organizational goal health
     * 
     * USE CASES:
     * 1. Admin dashboard - "Goal Status Overview" widget
     * 2. Management reports - Organizational effectiveness metrics
     * 3. Identifying bottlenecks (high pending/pending_completion counts)
     * 4. Tracking overall goal completion trends over time
     * 
     * METRICS PROVIDED:
     * - totalGoals: Total number of goals in system
     * - pending: Goals waiting for manager approval (workflow step 1)
     * - inProgress: Goals actively being worked on (workflow step 2)
     * - pendingCompletion: Goals awaiting manager completion approval (workflow step 3)
     * - completed: Goals fully completed and approved (workflow step 4)
     * - rejected: Goals that were rejected by manager
     * - completionRate: Percentage of all goals that are completed
     * 
     * GOAL WORKFLOW REMINDER:
     * PENDING → (manager approves) → IN_PROGRESS → (employee completes) → 
     * PENDING_COMPLETION_APPROVAL → (manager approves) → COMPLETED
     * 
     * ANALYSIS INSIGHTS:
     * - High pending count: Managers may be approval bottleneck
     * - High pendingCompletion: Managers slow to verify completions
     * - Low completion rate: Goals may be too ambitious or unrealistic
     * - High rejected count: Misalignment between employees and managers
     * 
     * @return Map containing goal analytics across all statuses
     */
    public Map<String, Object> getGoalAnalytics() {
        // Initialize result map
        Map<String, Object> analytics = new HashMap<>();
        
        // Fetch ALL goals from database
        // For large organizations (10,000+ goals), consider pagination or caching
        List<Goal> allGoals = goalRepo.findAll();
        
        // Count goals by status using Java 8 Streams
        // Pattern: stream() → filter(condition) → count()
        
        // PENDING: Waiting for manager approval to start
        long pending = allGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.PENDING)
                .count();
        
        // IN_PROGRESS: Actively being worked on by employee
        long inProgress = allGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)
                .count();
        
        // PENDING_COMPLETION_APPROVAL: Employee marked complete, awaiting manager verification
        long pendingCompletion = allGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.PENDING_COMPLETION_APPROVAL)
                .count();
        
        // COMPLETED: Fully completed and approved by manager
        long completed = allGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                .count();
        
        // REJECTED: Manager rejected the goal (needs revision or cancellation)
        long rejected = allGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.REJECTED)
                .count();
        
        // Populate analytics map
        analytics.put("totalGoals", allGoals.size());  // Total goal count
        analytics.put("pending", pending);
        analytics.put("inProgress", inProgress);
        analytics.put("pendingCompletion", pendingCompletion);
        analytics.put("completed", completed);
        analytics.put("rejected", rejected);
        
        // Calculate completion rate percentage
        // Only completed goals count towards completion rate
        // Formula: (completed / total) * 100
        analytics.put("completionRate", 
                allGoals.size() > 0 ? (completed * 100.0 / allGoals.size()) : 0);
        
        // Return analytics to controller
        return analytics;
    }
    
    // ============================================
    // DEPARTMENT PERFORMANCE
    // ============================================
    
    /**
     * Get comparative performance metrics for each department
     * 
     * PURPOSE:
     * Enable department-to-department performance comparison
     * Identify high-performing and underperforming departments
     * Support resource allocation and improvement initiatives
     * 
     * USE CASES:
     * 1. Executive dashboard - "Department Performance Comparison" chart
     * 2. HR reports - Identify departments needing support
     * 3. Budget allocation - Reward high-performing departments
     * 4. Quarterly business reviews
     * 
     * METRICS PER DEPARTMENT:
     * - department: Department name
     * - employeeCount: Number of employees in department
     * - totalGoals: All goals assigned to department employees
     * - completedGoals: Goals completed by department employees
     * - completionRate: Percentage of department goals completed
     * 
     * ALGORITHM:
     * 1. Extract unique department names from all users
     * 2. For each department:
     *    a. Get all users in that department
     *    b. Aggregate all goals for those users
     *    c. Count completed goals
     *    d. Calculate completion rate
     * 3. Return list of department metrics
     * 
     * PERFORMANCE CONSIDERATIONS:
     * Current implementation has N+1 query problem:
     * - 1 query to get departments
     * - N queries (one per department) to get users
     * - M queries (one per user) to get goals
     * 
     * For large organizations, consider:
     * - @Query with JOIN FETCH to reduce queries
     * - Caching department metrics (refresh hourly/daily)
     * - Database view with pre-aggregated statistics
     * 
     * @return List of maps, each containing metrics for one department
     */
    public List<Map<String, Object>> getDepartmentPerformance() {
        // Initialize result list
        // Each element is a map containing one department's metrics
        List<Map<String, Object>> performance = new ArrayList<>();
        
        // STEP 1: Get all unique department names
        List<User> allUsers = userRepo.findAll();  // Fetch all users
        
        // Extract unique departments using Stream operations
        List<String> departments = allUsers.stream()
                .map(User::getDepartment)  // Extract department field from each user
                .filter(dept -> dept != null && !dept.isEmpty())  // Remove null/empty departments
                .distinct()  // Keep only unique department names
                .toList();  // Convert to List
        
        // Example result: ["Engineering", "Sales", "Marketing", "HR"]
        
        // STEP 2: Calculate metrics for each department
        for (String dept : departments) {
            // Create map for this department's metrics
            Map<String, Object> deptMetrics = new HashMap<>();
            
            // Get all users in this specific department
            // Repository method: findByDepartment
            // SQL: SELECT * FROM users WHERE department = ?
            List<User> deptUsers = userRepo.findByDepartment(dept);
            
            // Aggregate all goals for users in this department
            List<Goal> deptGoals = new ArrayList<>();
            for (User user : deptUsers) {
                // For each user in department, fetch their goals
                // Repository method: findByAssignedToUser_UserId
                // SQL: SELECT * FROM goals WHERE assigned_to_user_id = ?
                deptGoals.addAll(goalRepo.findByAssignedToUser_UserId(user.getUserId()));
            }
            // NOTE: This creates N queries where N = number of users in department
            // Optimization: Use custom query with JOIN to fetch all in one query
            
            // Count completed goals in this department
            long completedGoals = deptGoals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                    .count();
            
            // Populate department metrics map
            deptMetrics.put("department", dept);  // Department name
            deptMetrics.put("employeeCount", deptUsers.size());  // Number of employees
            deptMetrics.put("totalGoals", deptGoals.size());  // Total goals assigned
            deptMetrics.put("completedGoals", completedGoals);  // Completed goals
            
            // Calculate department completion rate
            // Formula: (completed / total) * 100
            deptMetrics.put("completionRate", 
                    deptGoals.size() > 0 ? (completedGoals * 100.0 / deptGoals.size()) : 0);
            
            // Add this department's metrics to result list
            performance.add(deptMetrics);
        }
        
        // Return list of department performance maps
        // Example result:
        // [
        //   {department: "Engineering", employeeCount: 50, totalGoals: 200, completedGoals: 150, completionRate: 75.0},
        //   {department: "Sales", employeeCount: 30, totalGoals: 120, completedGoals: 100, completionRate: 83.33},
        //   ...
        // ]
        return performance;
    }
}
```

```java
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
 * ReportController - REST API endpoints for Analytics and Reporting Module
 * 
 * PURPOSE:
 * This controller exposes HTTP endpoints for report generation and analytics.
 * It's the entry point for all analytics-related requests from the frontend.
 * 
 * ARCHITECTURE:
 * Frontend → HTTP Request → Controller (this class) → Service → Repository → Database
 *          ← HTTP Response ← Controller ← Service ← Repository ← Database
 * 
 * RESPONSIBILITIES:
 * 1. Receive HTTP requests and extract parameters
 * 2. Extract user context from JWT token (userId, role)
 * 3. Call appropriate service methods
 * 4. Wrap results in standardized ApiResponse
 * 5. Return JSON response to frontend
 * 
 * BASE URL: /api/v1/reports
 * All endpoints in this controller are prefixed with this path
 * 
 * SECURITY:
 * - Class-level @PreAuthorize: Only ADMIN and MANAGER roles can access ANY endpoint here
 * - JWT Filter runs before this controller, validates token, extracts user info
 * - User info is stored in HttpServletRequest attributes by JWT filter
 * 
 * RESPONSE FORMAT:
 * All endpoints return ApiResponse<T> which has structure:
 * {
 *   "success": true/false,
 *   "message": "Human-readable message",
 *   "data": <actual data object>
 * }
 * 
 * ERROR HANDLING:
 * Exceptions thrown by service layer are caught by @ControllerAdvice
 * and converted to appropriate HTTP status codes and error responses
 */
@RestController  // Marks this as REST controller (combines @Controller + @ResponseBody)
                 // All methods automatically serialize return values to JSON
@RequestMapping("/api/v1/reports")  // Base path for all endpoints in this controller
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")  // Security: Only ADMIN/MANAGER can access
                                                  // This annotation checks JWT token roles
public class ReportController {
    
    // ============================================
    // DEPENDENCY INJECTION
    // ============================================
    
    @Autowired
    private ReportService reportSvc;  // Spring injects ReportService instance
                                       // This service contains all business logic
    
    // ============================================
    // REPORT CRUD ENDPOINTS
    // ============================================
    
    /**
     * GET /api/v1/reports - Retrieve all reports
     * 
     * PURPOSE:
     * Fetch complete list of generated reports for report library or admin view
     * 
     * FLOW:
     * 1. Spring receives GET request to /api/v1/reports
     * 2. @PreAuthorize checks user has ADMIN or MANAGER role
     * 3. Method is invoked
     * 4. Calls service to fetch all reports
     * 5. Wraps reports in ApiResponse
     * 6. Spring converts ApiResponse to JSON
     * 7. Returns JSON response with 200 OK status
     * 
     * EXAMPLE REQUEST:
     * GET /api/v1/reports
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Reports retrieved",
     *   "data": [
     *     {
     *       "reportId": 1,
     *       "scope": "Goal Analytics",
     *       "format": "PDF",
     *       "generatedDate": "2024-01-15T10:30:00",
     *       ...
     *     },
     *     ...
     *   ]
     * }
     * 
     * @return ApiResponse wrapping list of all reports
     */
    @GetMapping  // Maps GET requests to base path (/api/v1/reports)
    public ApiResponse<List<Report>> getAllReports() {
        // Call service to fetch all reports
        List<Report> reports = reportSvc.getAllReports();
        
        // Wrap in standard ApiResponse format
        // ApiResponse.success() is a static factory method that creates success response
        return ApiResponse.success("Reports retrieved", reports);
    }
    
    /**
     * GET /api/v1/reports/{reportId} - Get specific report by ID
     * 
     * PURPOSE:
     * Fetch details of a single report (for viewing or downloading)
     * 
     * PATH VARIABLE:
     * {reportId} - The report's unique identifier extracted from URL
     * 
     * FLOW:
     * 1. Spring extracts reportId from URL path
     * 2. @PathVariable annotation binds URL value to method parameter
     * 3. Calls service to fetch report
     * 4. If found: Returns report in ApiResponse
     * 5. If not found: Service throws ResourceNotFoundException
     *    → @ControllerAdvice catches it → Returns 404 error response
     * 
     * EXAMPLE REQUEST:
     * GET /api/v1/reports/5
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * EXAMPLE SUCCESS RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Report retrieved",
     *   "data": {
     *     "reportId": 5,
     *     "scope": "Department Performance",
     *     "metrics": "completion_rate,goal_count",
     *     "format": "Excel",
     *     "filePath": "/reports/1706451234567.xlsx",
     *     "generatedDate": "2024-01-15T10:30:00",
     *     "generatedBy": {...}
     *   }
     * }
     * 
     * EXAMPLE ERROR RESPONSE (404 Not Found):
     * {
     *   "success": false,
     *   "message": "Report not found",
     *   "data": null
     * }
     * 
     * @param reportId The report's primary key from URL path
     * @return ApiResponse containing the report if found
     */
    @GetMapping("/{reportId}")  // Maps GET /api/v1/reports/{reportId}
    public ApiResponse<Report> getReportById(@PathVariable Integer reportId) {
        // @PathVariable tells Spring to extract {reportId} from URL
        // Example: URL = /api/v1/reports/5 → reportId = 5
        
        // Call service to fetch report by ID
        Report report = reportSvc.getReportById(reportId);
        
        // Wrap and return
        return ApiResponse.success("Report retrieved", report);
    }
    
    /**
     * POST /api/v1/reports/generate - Generate a new report
     * 
     * PURPOSE:
     * Create a new report based on user-specified parameters
     * 
     * REQUEST BODY:
     * {
     *   "scope": "Goal Analytics",           // Required: Type of report
     *   "metrics": "completion_rate,total",   // Required: Metrics to include
     *   "format": "PDF"                       // Optional: Defaults to "PDF"
     * }
     * 
     * FLOW:
     * 1. Spring receives POST request with JSON body
     * 2. @RequestBody deserializes JSON to Map<String, String>
     * 3. Extract userId from HttpServletRequest (set by JWT filter)
     * 4. Extract parameters from request body
     * 5. Call service to generate report
     * 6. Service creates report metadata and audit log
     * 7. Return success response with created report
     * 
     * JWT FILTER INTEGRATION:
     * - JWT filter runs before this controller
     * - It validates JWT token from Authorization header
     * - Extracts userId and userRole from token
     * - Stores them in HttpServletRequest.setAttribute()
     * - We retrieve them using getAttribute()
     * 
     * EXAMPLE REQUEST:
     * POST /api/v1/reports/generate
     * Headers: 
     *   Authorization: Bearer <JWT_TOKEN>
     *   Content-Type: application/json
     * Body:
     * {
     *   "scope": "Goal Analytics",
     *   "metrics": "completion_rate,pending_count,total_goals",
     *   "format": "Excel"
     * }
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Report generated",
     *   "data": {
     *     "reportId": 15,
     *     "scope": "Goal Analytics",
     *     "metrics": "completion_rate,pending_count,total_goals",
     *     "format": "Excel",
     *     "filePath": "/reports/1706451234567.xlsx",
     *     "generatedDate": "2024-01-15T10:30:00",
     *     "generatedBy": {...}
     *   }
     * }
     * 
     * @param body Map containing request parameters (scope, metrics, format)
     * @param httpReq HttpServletRequest to extract user context set by JWT filter
     * @return ApiResponse containing the generated report metadata
     */
    @PostMapping("/generate")  // Maps POST /api/v1/reports/generate
    public ApiResponse<Report> generateReport(
            @RequestBody Map<String, String> body,  // Deserialize JSON body to Map
            HttpServletRequest httpReq) {  // Inject HttpServletRequest to access JWT data
        
        // Extract userId from request attributes (set by JWT filter)
        // JWT filter validates token and stores user info in request
        Integer userId = (Integer) httpReq.getAttribute("userId");
        
        // Extract report parameters from request body
        String scope = body.get("scope");      // Type of report (e.g., "Goal Analytics")
        String metrics = body.get("metrics");  // Metrics to include (comma-separated)
        
        // Get format with default value if not provided
        // getOrDefault() returns "PDF" if "format" key doesn't exist in map
        String format = body.getOrDefault("format", "PDF");
        
        // Call service to generate report
        // Service handles:
        // 1. Creating Report entity
        // 2. Saving to database
        // 3. Creating audit log entry
        Report report = reportSvc.generateReport(scope, metrics, format, userId);
        
        // Return success response with generated report
        return ApiResponse.success("Report generated", report);
    }
    
    // ============================================
    // ANALYTICS ENDPOINTS
    // ============================================
    
    /**
     * GET /api/v1/reports/dashboard - Get role-based dashboard metrics
     * 
     * PURPOSE:
     * Provide KPIs for dashboard home page based on user's role
     * Each role sees metrics relevant to their responsibilities
     * 
     * FLOW:
     * 1. JWT filter extracts userId and userRole from token
     * 2. Stores them in HttpServletRequest attributes
     * 3. Controller retrieves userId and role
     * 4. Service calculates role-appropriate metrics
     * 5. Returns metrics in ApiResponse
     * 
     * ROLE-BASED RESPONSES:
     * 
     * EMPLOYEE sees:
     * {
     *   "totalGoals": 10,
     *   "completedGoals": 6,
     *   "inProgressGoals": 3,
     *   "pendingGoals": 1,
     *   "completionRate": 60.0
     * }
     * 
     * MANAGER sees:
     * {
     *   "teamSize": 8,
     *   "totalTeamGoals": 80,
     *   "pendingApprovals": 5,
     *   "pendingCompletions": 12
     * }
     * 
     * ADMIN sees:
     * {
     *   "totalUsers": 150,
     *   "totalGoals": 1500,
     *   "totalReviews": 300,
     *   "completedGoals": 900
     * }
     * 
     * EXAMPLE REQUEST:
     * GET /api/v1/reports/dashboard
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Dashboard metrics retrieved",
     *   "data": {
     *     "teamSize": 8,
     *     "totalTeamGoals": 80,
     *     "pendingApprovals": 5,
     *     "pendingCompletions": 12
     *   }
     * }
     * 
     * @param httpReq HttpServletRequest to access JWT data (userId, userRole)
     * @return ApiResponse containing role-specific dashboard metrics
     */
    @GetMapping("/dashboard")  // Maps GET /api/v1/reports/dashboard
    public ApiResponse<Map<String, Object>> getDashboard(HttpServletRequest httpReq) {
        // Extract user context from request attributes (set by JWT filter)
        String role = (String) httpReq.getAttribute("userRole");  // "EMPLOYEE", "MANAGER", or "ADMIN"
        Integer userId = (Integer) httpReq.getAttribute("userId");  // User's primary key
        
        // Call service to calculate role-based metrics
        // Service has if-else logic to return appropriate metrics for each role
        Map<String, Object> metrics = reportSvc.getDashboardMetrics(userId, role);
        
        // Return metrics in standard response format
        return ApiResponse.success("Dashboard metrics retrieved", metrics);
    }
    
    /**
     * GET /api/v1/reports/performance-summary - Get performance review summary
     * 
     * PURPOSE:
     * Aggregate performance review statistics with optional filtering
     * Useful for: Cycle reports, department analysis, executive summaries
     * 
     * QUERY PARAMETERS (both optional):
     * - cycleId: Filter by specific performance cycle (e.g., Q1 2024)
     * - dept: Filter by department name (e.g., "Engineering")
     * 
     * FILTERING COMBINATIONS:
     * 1. No parameters: All reviews, all departments, all cycles
     * 2. Only cycleId: All departments for specific cycle
     * 3. Only dept: All cycles for specific department
     * 4. Both: Specific cycle AND specific department
     * 
     * FLOW:
     * 1. Spring extracts query parameters from URL
     * 2. @RequestParam binds them to method parameters (null if not present)
     * 3. Service fetches and filters reviews
     * 4. Service calculates average ratings
     * 5. Returns summary statistics
     * 
     * EXAMPLE REQUESTS:
     * 
     * 1. All reviews:
     *    GET /api/v1/reports/performance-summary
     * 
     * 2. Specific cycle:
     *    GET /api/v1/reports/performance-summary?cycleId=5
     * 
     * 3. Specific department:
     *    GET /api/v1/reports/performance-summary?dept=Engineering
     * 
     * 4. Cycle AND department:
     *    GET /api/v1/reports/performance-summary?cycleId=5&dept=Engineering
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Performance summary retrieved",
     *   "data": {
     *     "totalReviews": 25,
     *     "avgSelfRating": 3.8,
     *     "avgManagerRating": 3.5,
     *     "cycleId": 5,
     *     "department": "Engineering"
     *   }
     * }
     * 
     * USE CASES:
     * - Executive dashboard: "Q1 2024 Performance Overview"
     * - Department head: "Engineering Department Performance"
     * - HR analysis: Compare self vs manager ratings for alignment
     * 
     * @param cycleId (Optional) Performance cycle ID to filter by
     * @param dept (Optional) Department name to filter by
     * @return ApiResponse containing performance summary statistics
     */
    @GetMapping("/performance-summary")  // Maps GET /api/v1/reports/performance-summary
    public ApiResponse<Map<String, Object>> getPerformanceSummary(
            @RequestParam(required = false) Integer cycleId,  // Optional query param
            @RequestParam(required = false) String dept) {     // Optional query param
        
        // @RequestParam(required = false) means:
        // - Parameter is optional in URL
        // - Value is null if not provided
        // - No error if parameter is missing
        
        // Call service to calculate summary with filters
        Map<String, Object> summary = reportSvc.getPerformanceSummary(cycleId, dept);
        
        // Return summary in standard response format
        return ApiResponse.success("Performance summary retrieved", summary);
    }
    
    /**
     * GET /api/v1/reports/goal-analytics - Get organization-wide goal analytics
     * 
     * PURPOSE:
     * Provide comprehensive breakdown of goal statuses across organization
     * Helps identify bottlenecks and track overall goal health
     * 
     * METRICS PROVIDED:
     * - totalGoals: Total number of goals
     * - pending: Goals waiting for manager approval
     * - inProgress: Goals being actively worked on
     * - pendingCompletion: Goals awaiting completion approval
     * - completed: Fully completed goals
     * - rejected: Goals rejected by managers
     * - completionRate: Percentage of completed goals
     * 
     * NO PARAMETERS:
     * This endpoint always returns system-wide analytics (no filtering)
     * 
     * FLOW:
     * 1. Spring receives GET request
     * 2. No parameters to extract
     * 3. Service fetches all goals and counts by status
     * 4. Returns comprehensive analytics
     * 
     * EXAMPLE REQUEST:
     * GET /api/v1/reports/goal-analytics
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Goal analytics retrieved",
     *   "data": {
     *     "totalGoals": 1500,
     *     "pending": 120,
     *     "inProgress": 800,
     *     "pendingCompletion": 230,
     *     "completed": 300,
     *     "rejected": 50,
     *     "completionRate": 20.0
     *   }
     * }
     * 
     * USE CASES:
     * - Admin dashboard: "Goal Status Distribution" pie chart
     * - Management review: "Organizational Goal Health"
     * - Identify bottlenecks: High pending counts indicate approval delays
     * 
     * @return ApiResponse containing goal analytics across all statuses
     */
    @GetMapping("/goal-analytics")  // Maps GET /api/v1/reports/goal-analytics
    public ApiResponse<Map<String, Object>> getGoalAnalytics() {
        // No parameters - always returns system-wide analytics
        
        // Call service to calculate goal analytics
        Map<String, Object> analytics = reportSvc.getGoalAnalytics();
        
        // Return analytics in standard response format
        return ApiResponse.success("Goal analytics retrieved", analytics);
    }
    
    /**
     * GET /api/v1/reports/department-performance - Get performance by department
     * 
     * PURPOSE:
     * Compare performance metrics across all departments
     * Enable benchmarking and identify high/low performing departments
     * 
     * RETURNS:
     * List of department metrics, one entry per department
     * Each entry contains:
     * - department: Department name
     * - employeeCount: Number of employees
     * - totalGoals: Total goals for department
     * - completedGoals: Completed goals count
     * - completionRate: Percentage of goals completed
     * 
     * NO PARAMETERS:
     * This endpoint always returns metrics for ALL departments
     * 
     * FLOW:
     * 1. Spring receives GET request
     * 2. Service identifies all unique departments
     * 3. For each department, aggregates employee goals
     * 4. Calculates completion metrics
     * 5. Returns list of department performances
     * 
     * EXAMPLE REQUEST:
     * GET /api/v1/reports/department-performance
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "success": true,
     *   "message": "Department performance retrieved",
     *   "data": [
     *     {
     *       "department": "Engineering",
     *       "employeeCount": 50,
     *       "totalGoals": 200,
     *       "completedGoals": 150,
     *       "completionRate": 75.0
     *     },
     *     {
     *       "department": "Sales",
     *       "employeeCount": 30,
     *       "totalGoals": 120,
     *       "completedGoals": 100,
     *       "completionRate": 83.33
     *     },
     *     {
     *       "department": "Marketing",
     *       "employeeCount": 20,
     *       "totalGoals": 80,
     *       "completedGoals": 60,
     *       "completionRate": 75.0
     *     }
     *   ]
     * }
     * 
     * USE CASES:
     * - Executive dashboard: "Department Comparison" bar chart
     * - HR reports: Identify departments needing support
     * - Budget allocation: Reward high-performing departments
     * - Benchmarking: Compare department to department
     * 
     * FRONTEND VISUALIZATION:
     * This data is perfect for:
     * - Bar charts (completion rate by department)
     * - Table view (sortable department metrics)
     * - Heatmap (employee count vs completion rate)
     * 
     * @return ApiResponse containing list of department performance metrics
     */
    @GetMapping("/department-performance")  // Maps GET /api/v1/reports/department-performance
    public ApiResponse<List<Map<String, Object>>> getDeptPerformance() {
        // No parameters - always returns all departments
        
        // Call service to calculate department-wise performance
        List<Map<String, Object>> performance = reportSvc.getDepartmentPerformance();
        
        // Return performance list in standard response format
        return ApiResponse.success("Department performance retrieved", performance);
    }
}
```

```java
package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReportRepository - Data access layer for Report entity
 * 
 * PURPOSE:
 * This repository provides database operations for the Report entity.
 * It's the bridge between Java code and the database.
 * 
 * SPRING DATA JPA:
 * By extending JpaRepository, we automatically get these methods for FREE:
 * - save(Report) - Insert or update report
 * - findById(Integer) - Find report by primary key
 * - findAll() - Get all reports
 * - delete(Report) - Delete a report
 * - count() - Count total reports
 * - existsById(Integer) - Check if report exists
 * ...and many more!
 * 
 * CUSTOM QUERY METHODS:
 * Spring Data JPA can generate queries automatically from method names.
 * We just declare the method signature, Spring generates the implementation!
 * 
 * METHOD NAMING CONVENTION:
 * findBy<FieldName><Operator><...>OrderBy<FieldName><Direction>
 * 
 * Examples:
 * - findByScope → WHERE scope = ?
 * - findByGeneratedBy_UserId → WHERE generated_by.user_id = ?
 * - OrderByGeneratedDateDesc → ORDER BY generated_date DESC
 * 
 * ENTITY RELATIONSHIP:
 * Report entity has:
 * - reportId (Primary Key)
 * - scope, metrics, format (String fields)
 * - generatedBy (ManyToOne relationship to User entity)
 * - generatedDate (LocalDateTime field)
 * - filePath (String field)
 * 
 * DATABASE TABLE:
 * This repository operates on the 'reports' table (or whatever @Table specifies)
 */
@Repository  // Marks this as a Spring Data repository component
             // Spring will create implementation at runtime
public interface ReportRepository extends JpaRepository<Report, Integer> {
    // JpaRepository<Report, Integer> means:
    // - Report: The entity type this repository manages
    // - Integer: The type of the entity's primary key (reportId)
    
    /**
     * Find reports by scope (type of report)
     * 
     * GENERATED QUERY:
     * SELECT * FROM reports WHERE scope = ?
     * 
     * METHOD NAME BREAKDOWN:
     * - findBy: Query prefix (tells Spring to generate SELECT query)
     * - Scope: Field name in Report entity
     * - No operator: Defaults to equals (=)
     * 
     * USE CASE:
     * Get all reports of a specific type
     * Example: Find all "Goal Analytics" reports
     * 
     * EXAMPLE USAGE:
     * List<Report> goalReports = reportRepo.findByScope("Goal Analytics");
     * 
     * SQL EQUIVALENT:
     * SELECT r.* FROM reports r WHERE r.scope = 'Goal Analytics'
     * 
     * @param scope The type/category of report to search for
     * @return List of reports matching the scope (empty list if none found)
     */
    List<Report> findByScope(String scope);
    
    /**
     * Find reports generated by a specific user, ordered by date descending
     * 
     * GENERATED QUERY:
     * SELECT * FROM reports r 
     * JOIN users u ON r.generated_by_id = u.user_id 
     * WHERE u.user_id = ? 
     * ORDER BY r.generated_date DESC
     * 
     * METHOD NAME BREAKDOWN:
     * - findBy: Query prefix
     * - GeneratedBy: Report entity field (ManyToOne relationship to User)
     * - _UserId: Navigate into User entity and use userId field
     * - OrderBy: Start of ordering clause
     * - GeneratedDate: Field to sort by
     * - Desc: Descending order (newest first)
     * 
     * RELATIONSHIP NAVIGATION:
     * Report has field: User generatedBy
     * User has field: Integer userId
     * Underscore (_) navigates the relationship: generatedBy_UserId
     * 
     * USE CASE:
     * "My Reports" page showing user's report history
     * Most recent reports appear first for better UX
     * 
     * EXAMPLE USAGE:
     * List<Report> myReports = reportRepo.findByGeneratedBy_UserIdOrderByGeneratedDateDesc(25);
     * 
     * RESULT:
     * Returns reports generated by user with userId=25
     * Sorted from newest to oldest (latest generated_date first)
     * 
     * ALTERNATIVE APPROACHES:
     * 
     * 1. Using @Query annotation (more control):
     * @Query("SELECT r FROM Report r WHERE r.generatedBy.userId = :userId ORDER BY r.generatedDate DESC")
     * List<Report> findUserReportsSorted(@Param("userId") Integer userId);
     * 
     * 2. Using native SQL:
     * @Query(value = "SELECT * FROM reports WHERE generated_by_id = ? ORDER BY generated_date DESC", nativeQuery = true)
     * List<Report> findUserReportsNative(Integer userId);
     * 
     * @param userId The user ID who generated the reports
     * @return List of reports by that user, sorted newest to oldest
     */
    List<Report> findByGeneratedBy_UserIdOrderByGeneratedDateDesc(Integer userId);
    
    // ============================================
    // ADDITIONAL METHODS WE COULD ADD
    // ============================================
    
    // Find reports by format (PDF, Excel, CSV)
    // List<Report> findByFormat(String format);
    
    // Find reports generated within date range
    // List<Report> findByGeneratedDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find reports by scope and format
    // List<Report> findByScopeAndFormat(String scope, String format);
    
    // Count reports by user
    // long countByGeneratedBy_UserId(Integer userId);
    
    // Check if report exists with specific scope
    // boolean existsByScope(String scope);
    
    // Delete reports by scope
    // void deleteByScope(String scope);
    
    // Custom query with JOIN FETCH to avoid N+1 problem
    // @Query("SELECT r FROM Report r JOIN FETCH r.generatedBy WHERE r.reportId = :id")
    // Optional<Report> findByIdWithUser(@Param("id") Integer id);
}
```

---

## 📋 SUMMARY OF COMPONENTS

### **ReportService.java**
- **Purpose**: Business logic for analytics and reporting
- **Key Methods**:
  - `getAllReports()` - Fetch all reports
  - `getReportById()` - Fetch specific report
  - `generateReport()` - Create report metadata with audit log
  - `getDashboardMetrics()` - Role-based dashboard KPIs
  - `getPerformanceSummary()` - Performance review aggregations
  - `getGoalAnalytics()` - Organization-wide goal statistics
  - `getDepartmentPerformance()` - Department-wise comparison

### **ReportController.java**
- **Purpose**: REST API endpoints for analytics
- **Base URL**: `/api/v1/reports`
- **Security**: Only ADMIN and MANAGER roles
- **Endpoints**:
  - `GET /` - All reports
  - `GET /{reportId}` - Single report
  - `POST /generate` - Generate new report
  - `GET /dashboard` - Role-based dashboard
  - `GET /performance-summary` - Performance analytics
  - `GET /goal-analytics` - Goal statistics
  - `GET /department-performance` - Department comparison

### **ReportRepository.java**
- **Purpose**: Database access for Report entity
- **Extends**: JpaRepository (provides CRUD methods automatically)
- **Custom Methods**:
  - `findByScope()` - Filter by report type
  - `findByGeneratedBy_UserIdOrderByGeneratedDateDesc()` - User's reports, newest first
