# Advanced Features Implementation Plan for PerformanceTrack

## Overview
I'll create a comprehensive plan to add enterprise-grade features to your PerformanceTrack system. Let me break this down into strategic implementation areas with detailed user stories and tasks.

---

# 📋 EPIC 11: SCHEDULER & AUTOMATED TASKS

## User Story 11.1: Review Cycle Deadline Reminders
**As a system administrator**  
**I want automated reminders for upcoming review deadlines**  
**So that employees and managers don't miss important review milestones**

### Priority: HIGH
### Owner: Ankit

### Tasks:
1. **Create Scheduled Task Infrastructure**
   - Add Spring Scheduler dependency (`@EnableScheduling`)
   - Create `@Configuration` class for scheduler settings
   - Configure thread pool for scheduled tasks (minimum 5 threads)

2. **Implement Review Deadline Reminder Job**
   - Create `ReviewReminderScheduler` class with `@Component`
   - Add method `checkUpcomingReviewDeadlines()` with `@Scheduled(cron = "0 0 9 * * ?")`  // Daily at 9 AM
   - Query active review cycles ending in 30, 15, 7, 3, 1 days
   - For each milestone, find employees without submitted self-assessment
   - Create notifications for employees and their managers
   - Log reminder execution in audit logs

3. **Configure Reminder Thresholds**
   - Create `application.properties` entries:
     ```
     scheduler.review.reminder.days=30,15,7,3,1
     scheduler.review.reminder.enabled=true
     ```
   - Make reminder days configurable per review cycle

### Acceptance Criteria:
- ✅ Scheduler runs daily at 9 AM
- ✅ Reminders sent at 30, 15, 7, 3, 1 day milestones
- ✅ Only users without submitted assessments notified
- ✅ Can disable scheduler via config
- ✅ Execution logged in audit trail

---

## User Story 11.2: Goal Progress Check Automation
**As a manager**  
**I want automatic notifications for stale goals**  
**So that employees stay engaged with their objectives**

### Priority: HIGH
### Owner: Kashish

### Tasks:
1. **Create Stale Goal Detection Job**
   - Create `GoalProgressScheduler` class
   - Add method `checkStaleGoals()` with `@Scheduled(cron = "0 0 10 * * MON")`  // Weekly on Monday 10 AM
   - Find IN_PROGRESS goals with no progress updates in last 14 days
   - Calculate "days since last update" metric

2. **Generate Stale Goal Notifications**
   - Create notification for employee: "Your goal '[Title]' hasn't been updated in X days"
   - Create notification for manager: "Employee [Name] has Y goals without recent updates"
   - Set priority to MEDIUM for 14-21 days, HIGH for 21+ days
   - Set actionRequired = true

3. **Add Configuration**
   - `scheduler.goal.stale.days=14`
   - `scheduler.goal.stale.check.enabled=true`

### Acceptance Criteria:
- ✅ Runs every Monday at 10 AM
- ✅ Detects goals without updates for 14+ days
- ✅ Notifications sent to employees and managers
- ✅ Priority escalates based on staleness
- ✅ Can be disabled via configuration

---

## User Story 11.3: Completion Evidence Follow-Up
**As a manager**  
**I want reminders for pending evidence verification**  
**So that goal completions don't get delayed**

### Priority: MEDIUM
### Owner: Pratik

### Tasks:
1. **Create Evidence Verification Reminder Job**
   - Create `CompletionApprovalScheduler` class
   - Add method `checkPendingCompletions()` with `@Scheduled(cron = "0 0 14 * * TUE,THU")`  // Tue & Thu 2 PM
   - Find goals in PENDING_COMPLETION_APPROVAL for 3+ days
   - Find goals with ADDITIONAL_EVIDENCE_REQUIRED for 7+ days

2. **Send Targeted Reminders**
   - For managers: List of goals pending their review
   - For employees: Reminder about additional evidence needed
   - Include link to goal details in notification

3. **Configure Reminder Timing**
   - `scheduler.completion.pending.days=3`
   - `scheduler.completion.additional-evidence.days=7`

### Acceptance Criteria:
- ✅ Runs Tuesday and Thursday at 2 PM
- ✅ Manager reminded of pending verifications after 3 days
- ✅ Employee reminded of evidence requests after 7 days
- ✅ Notification includes goal details and links

---

## User Story 11.4: Notification Cleanup Job
**As a system administrator**  
**I want automatic cleanup of old notifications**  
**So that database doesn't grow indefinitely**

### Priority: MEDIUM
### Owner: Ankit

### Tasks:
1. **Create Notification Cleanup Job**
   - Create `NotificationCleanupScheduler` class
   - Add method `cleanupOldNotifications()` with `@Scheduled(cron = "0 0 2 * * SUN")`  // Sunday 2 AM
   - Delete READ notifications older than 90 days
   - Keep UNREAD notifications indefinitely
   - Log cleanup statistics (count deleted, oldest date retained)

2. **Add Cleanup Configuration**
   - `scheduler.notification.cleanup.enabled=true`
   - `scheduler.notification.cleanup.retention.days=90`
   - `scheduler.notification.cleanup.batch.size=1000`

3. **Implement Batch Deletion**
   - Delete in batches of 1000 to avoid long-running transactions
   - Use `@Transactional` with proper isolation level

### Acceptance Criteria:
- ✅ Runs every Sunday at 2 AM
- ✅ Deletes READ notifications older than 90 days
- ✅ Never deletes UNREAD notifications
- ✅ Batch processing prevents performance issues
- ✅ Cleanup statistics logged

---

## User Story 11.5: Performance Metrics Report Generation
**As an admin**  
**I want weekly automated performance reports**  
**So that trends are tracked consistently**

### Priority: LOW
### Owner: Akashat

### Tasks:
1. **Create Weekly Report Generator**
   - Create `ReportGenerationScheduler` class
   - Add method `generateWeeklyReport()` with `@Scheduled(cron = "0 0 22 * * SUN")`  // Sunday 10 PM
   - Calculate weekly metrics: goals completed, reviews submitted, average ratings
   - Store report in `reports` table
   - Notify admin users that report is ready

2. **Configure Report Settings**
   - `scheduler.report.weekly.enabled=true`
   - `scheduler.report.format=PDF`
   - `scheduler.report.notify.admins=true`

### Acceptance Criteria:
- ✅ Generates report every Sunday at 10 PM
- ✅ Report includes all weekly metrics
- ✅ Stored in database with metadata
- ✅ Admin users notified

---

# 📋 EPIC 12: COMPREHENSIVE LOGGING

## User Story 12.1: Centralized Logging Infrastructure
**As a developer**  
**I want structured logging across all layers**  
**So that debugging and monitoring are efficient**

### Priority: HIGH
### Owner: Kashish

### Tasks:
1. **Configure Logback**
   - Create `src/main/resources/logback-spring.xml`
   - Define log patterns with timestamp, thread, level, logger, message, MDC
   - Configure file appenders:
     - `logs/application.log` (all logs, rolling daily, keep 30 days)
     - `logs/error.log` (ERROR level only, rolling daily, keep 60 days)
     - `logs/audit.log` (audit events only, rolling daily, keep 365 days)
   - Configure console appender for DEV profile only

2. **Setup Log Levels by Profile**
   - **DEV**: `logging.level.root=INFO`, `logging.level.com.project.performanceTrack=DEBUG`
   - **TEST**: `logging.level.root=WARN`, `logging.level.com.project.performanceTrack=INFO`
   - **PROD**: `logging.level.root=ERROR`, `logging.level.com.project.performanceTrack=WARN`

3. **Configure Rolling Policy**
   - Max file size: 10MB
   - Total size cap: 1GB
   - Max history: 30 days (application), 60 days (error), 365 days (audit)

### Acceptance Criteria:
- ✅ Logs written to separate files by type
- ✅ Log rotation working correctly
- ✅ Console logs only in DEV
- ✅ Log levels appropriate per environment

---

## User Story 12.2: Method-Level Logging with AOP
**As a developer**  
**I want automatic logging of service method entry/exit**  
**So that execution flow is traceable**

### Priority: MEDIUM
### Owner: Pratik

### Tasks:
1. **Create Logging Aspect**
   - Add Spring AOP dependency
   - Create `@Aspect` class `LoggingAspect`
   - Create `@Around` advice for all service methods
   - Log method name, parameters (mask sensitive data), execution time
   - Log return value (mask sensitive data)
   - Log exceptions with full stack trace

2. **Implement Sensitive Data Masking**
   - Create `DataMaskingUtil` class
   - Mask passwords, tokens, SSN, credit cards
   - Use regex patterns for detection
   - Replace with `***MASKED***`

3. **Configure AOP Logging**
   - `logging.aop.enabled=true`
   - `logging.aop.log-parameters=true`
   - `logging.aop.log-execution-time=true`
   - `logging.aop.mask-sensitive-data=true`

### Acceptance Criteria:
- ✅ All service methods logged automatically
- ✅ Execution time captured
- ✅ Sensitive data masked in logs
- ✅ Can disable via configuration

---

## User Story 12.3: HTTP Request/Response Logging
**As a developer**  
**I want all API requests logged**  
**So that API usage is auditable**

### Priority: MEDIUM
### Owner: Rudra

### Tasks:
1. **Create Request Logging Filter**
   - Create `RequestLoggingFilter` implementing `OncePerRequestFilter`
   - Log request: method, URI, headers (except Authorization), body
   - Log response: status code, headers, body (if enabled)
   - Capture request processing time
   - Use MDC to add request ID to all logs

2. **Add Request ID Generation**
   - Generate UUID for each request
   - Add to MDC: `MDC.put("requestId", uuid)`
   - Include in response header: `X-Request-ID`
   - Clear MDC after request completion

3. **Configure Request Logging**
   - `logging.http.requests.enabled=true`
   - `logging.http.requests.log-headers=true`
   - `logging.http.requests.log-body=false`  // Disable in PROD
   - `logging.http.requests.max-body-size=1024`

### Acceptance Criteria:
- ✅ All requests logged with unique ID
- ✅ Request ID in response header
- ✅ Request/response bodies configurable
- ✅ Sensitive headers excluded

---

## User Story 12.4: Database Query Logging
**As a developer**  
**I want slow query detection**  
**So that performance issues are identified**

### Priority: LOW
### Owner: Sharen

### Tasks:
1. **Configure Hibernate Logging**
   - Enable SQL logging: `spring.jpa.show-sql=false` (use logger instead)
   - Enable query logging: `logging.level.org.hibernate.SQL=DEBUG`
   - Enable parameter logging: `logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE`
   - Format SQL: `spring.jpa.properties.hibernate.format_sql=true`

2. **Add Query Performance Logging**
   - Configure datasource proxy for query timing
   - Log queries taking >1 second with WARN level
   - Include query, parameters, execution time

3. **Profile-Specific Configuration**
   - DEV: All SQL logged
   - TEST: Slow queries only
   - PROD: Slow queries only (>2 seconds)

### Acceptance Criteria:
- ✅ SQL queries logged in DEV
- ✅ Slow queries detected and logged
- ✅ Query parameters visible in logs
- ✅ Different thresholds per environment

---

## User Story 12.5: Business Event Logging
**As a business analyst**  
**I want key business events logged separately**  
**So that business metrics can be analyzed**

### Priority: MEDIUM
### Owner: Akashat

### Tasks:
1. **Create Business Event Logger**
   - Create separate `business-events.log` file
   - Create `BusinessEventLogger` utility class
   - Define event types: GOAL_CREATED, GOAL_COMPLETED, REVIEW_SUBMITTED, etc.
   - Log with structured format: timestamp, eventType, userId, entityId, metadata (JSON)

2. **Integrate into Services**
   - Add business event logging in:
     - GoalService: GOAL_CREATED, GOAL_APPROVED, GOAL_COMPLETED
     - PerformanceReviewService: REVIEW_SUBMITTED, REVIEW_COMPLETED
     - UserService: USER_CREATED, USER_UPDATED

3. **Configure Business Event Logging**
   - `logging.business-events.enabled=true`
   - `logging.business-events.format=JSON`
   - Separate appender in logback.xml

### Acceptance Criteria:
- ✅ Business events in separate log file
- ✅ JSON format for easy parsing
- ✅ All key events logged
- ✅ Metadata includes relevant IDs

---

# 📋 EPIC 13: MULTI-MODULE PROJECT STRUCTURE

## User Story 13.1: Parent POM Setup
**As a build engineer**  
**I want a parent POM with common configuration**  
**So that module configuration is consistent**

### Priority: HIGH
### Owner: Ankit

### Tasks:
1. **Create Parent POM Structure**
   ```
   performancetrack-parent/
   ├── pom.xml (parent)
   ├── performancetrack-common/
   │   └── pom.xml
   ├── performancetrack-api/
   │   └── pom.xml
   └── performancetrack-scheduler/
       └── pom.xml
   ```

2. **Configure Parent POM**
   - Set `<packaging>pom</packaging>`
   - Define common properties: Java version, Spring Boot version, dependency versions
   - Define `<dependencyManagement>` section with all dependencies
   - Define `<pluginManagement>` for Maven plugins
   - Define profiles: dev, test, prod

3. **Define Dependency Versions**
   ```xml
   <properties>
       <java.version>21</java.version>
       <spring-boot.version>3.2.1</spring-boot.version>
       <lombok.version>1.18.30</lombok.version>
       <jwt.version>0.11.5</jwt.version>
       <modelmapper.version>3.2.0</modelmapper.version>
   </properties>
   ```

### Acceptance Criteria:
- ✅ Parent POM compiles successfully
- ✅ All dependency versions centralized
- ✅ Child modules inherit configuration
- ✅ Profiles defined for all environments

---

## User Story 13.2: Profile-Based Configuration
**As a DevOps engineer**  
**I want environment-specific profiles**  
**So that application behaves correctly per environment**

### Priority: HIGH
### Owner: Kashish

### Tasks:
1. **Create Profile-Specific Property Files**
   ```
   src/main/resources/
   ├── application.properties (common)
   ├── application-dev.properties
   ├── application-test.properties
   └── application-prod.properties
   ```

2. **Configure DEV Profile**
   - Database: `spring.datasource.url=jdbc:mysql://localhost:3306/perftrack_dev`
   - Logging: DEBUG level
   - Security: Relaxed CORS, token expiry 24 hours
   - Scheduler: All enabled with short intervals for testing
   - Show SQL: true
   - DDL: update

3. **Configure TEST Profile**
   - Database: In-memory H2 or separate test DB
   - Logging: INFO level
   - Security: Standard CORS, token expiry 1 hour
   - Scheduler: Disabled
   - Show SQL: false
   - DDL: create-drop

4. **Configure PROD Profile**
   - Database: Production MySQL with connection pooling
   - Logging: WARN/ERROR level
   - Security: Strict CORS, token expiry 30 minutes
   - Scheduler: All enabled with production intervals
   - Show SQL: false
   - DDL: validate
   - Enable SSL/TLS

5. **Add Profile Activation**
   - Default: dev
   - Maven: `mvn spring-boot:run -Dspring-boot.run.profiles=prod`
   - JAR: `java -jar app.jar --spring.profiles.active=prod`

### Acceptance Criteria:
- ✅ Each profile has separate configuration
- ✅ DEV uses local database
- ✅ TEST uses in-memory database
- ✅ PROD uses production database
- ✅ Profile activated correctly via command line

---

## User Story 13.3: Common Module Extraction
**As a developer**  
**I want shared code in common module**  
**So that code reuse is maximized**

### Priority: MEDIUM
### Owner: Pratik

### Tasks:
1. **Create performancetrack-common Module**
   - Move to common:
     - All entities
     - All enums
     - All DTOs
     - Utility classes
     - Exception classes
     - Security utilities (JwtUtil)

2. **Configure Common Module POM**
   - Add only necessary dependencies (no web, no security)
   - Dependencies: Spring Data JPA, Validation, Lombok

3. **Update API Module**
   - Add dependency on common module
   - Remove moved classes
   - Update imports

### Acceptance Criteria:
- ✅ Common module compiles independently
- ✅ API module uses common module
- ✅ No code duplication
- ✅ All tests pass after refactoring

---

# 📋 EPIC 14: UTILITY FUNCTIONS FOR NOTIFICATION & AUDIT

## User Story 14.1: Notification Utility Class
**As a developer**  
**I want reusable notification creation methods**  
**So that notification logic is consistent**

### Priority: HIGH
### Owner: Ankit

### Tasks:
1. **Create NotificationUtil Class**
   ```java
   @Component
   @RequiredArgsConstructor
   public class NotificationUtil {
       private final NotificationRepository notificationRepository;
       private final UserRepository userRepository;
       
       // Method signatures to implement...
   }
   ```

2. **Implement Notification Creation Methods**
   - `createGoalNotification(Integer userId, NotificationType type, Goal goal, String customMessage)`
   - `createReviewNotification(Integer userId, NotificationType type, PerformanceReview review, String customMessage)`
   - `createSystemNotification(Integer userId, NotificationType type, String message, String priority)`
   - `createNotificationForTeam(Integer managerId, NotificationType type, String message)`
   - `createNotificationForAllManagers(NotificationType type, String message)`

3. **Add Notification Templating**
   - Create message templates in `notification-templates.properties`
   - Use placeholders: `{userName}`, `{goalTitle}`, `{managerName}`, etc.
   - Support for dynamic message composition

4. **Implement Batch Notification Creation**
   - `createBulkNotifications(List<Integer> userIds, NotificationType type, String message)`
   - Use batch insert for performance

### Acceptance Criteria:
- ✅ All services use NotificationUtil
- ✅ No duplicate notification creation code
- ✅ Message templates externalized
- ✅ Batch creation optimized

---

## User Story 14.2: Audit Logging Utility Class
**As a developer**  
**I want standardized audit logging**  
**So that all actions are consistently tracked**

### Priority: HIGH
### Owner: Ankit

### Tasks:
1. **Create AuditUtil Class**
   ```java
   @Component
   @RequiredArgsConstructor
   public class AuditUtil {
       private final AuditLogRepository auditLogRepository;
       
       // Method signatures to implement...
   }
   ```

2. **Implement Audit Methods**
   - `logAction(Integer userId, String action, String details)`
   - `logEntityAction(Integer userId, String action, String entityType, Integer entityId, String details)`
   - `logSuccessAction(Integer userId, String action, String entityType, Integer entityId, String details)`
   - `logFailedAction(Integer userId, String action, String details, Exception exception)`
   - `logSecurityEvent(Integer userId, String action, String ipAddress, String details)`

3. **Add IP Address Extraction**
   - Create method to extract IP from HttpServletRequest
   - Handle proxy headers (X-Forwarded-For)
   - Store in audit log

4. **Implement Async Audit Logging**
   - Use `@Async` for audit log creation
   - Configure async executor in configuration
   - Prevent audit logging from blocking main operations

### Acceptance Criteria:
- ✅ All services use AuditUtil
- ✅ IP address captured correctly
- ✅ Async logging doesn't block requests
- ✅ Failed actions logged with exceptions

---

## User Story 14.3: Audit Logging Aspect (AOP)
**As a developer**  
**I want automatic audit logging via annotations**  
**So that audit logging is declarative**

### Priority: MEDIUM
### Owner: Kashish

### Tasks:
1. **Create Custom Audit Annotation**
   ```java
   @Target(ElementType.METHOD)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Audited {
       String action();
       String entityType() default "";
       boolean logParameters() default false;
   }
   ```

2. **Create Audit Aspect**
   - Create `@Aspect` class `AuditAspect`
   - Intercept methods annotated with `@Audited`
   - Extract user from SecurityContext
   - Extract entity ID from method parameters or return value
   - Call AuditUtil to log

3. **Apply to Service Methods**
   - Annotate key service methods:
     ```java
     @Audited(action = "GOAL_CREATED", entityType = "GOAL")
     public Goal createGoal(...) { ... }
     ```

### Acceptance Criteria:
- ✅ Annotated methods auto-logged
- ✅ User ID extracted from security context
- ✅ Entity ID extracted correctly
- ✅ Can disable annotation-based logging

---

# 📋 EPIC 15: RATE LIMITING

## User Story 15.1: API Rate Limiting Implementation
**As a system administrator**  
**I want rate limiting on sensitive endpoints**  
**So that API abuse is prevented**

### Priority: HIGH
### Owner: Rudra

### Tasks:
1. **Add Bucket4j Dependency**
   ```xml
   <dependency>
       <groupId>com.github.vladimir-bukhtoyarov</groupId>
       <artifactId>bucket4j-core</artifactId>
       <version>8.1.0</version>
   </dependency>
   ```

2. **Create Rate Limit Configuration**
   - Create `RateLimitConfig` class
   - Define rate limits per endpoint type:
     - Authentication: 5 requests/minute per IP
     - Goal creation: 10 requests/minute per user
     - Report generation: 5 requests/hour per user
     - Search/Filter: 60 requests/minute per user

3. **Implement Rate Limit Filter**
   - Create `RateLimitFilter` implementing `OncePerRequestFilter`
   - Use Bucket4j to track requests
   - Store buckets in-memory (ConcurrentHashMap) or Redis
   - Return 429 Too Many Requests when limit exceeded
   - Include Retry-After header

4. **Configure Rate Limiting**
   - `ratelimit.enabled=true`
   - `ratelimit.auth.requests-per-minute=5`
   - `ratelimit.goal.requests-per-minute=10`
   - `ratelimit.report.requests-per-hour=5`

### Acceptance Criteria:
- ✅ Rate limits enforced per endpoint
- ✅ 429 status returned when exceeded
- ✅ Retry-After header present
- ✅ Can disable via configuration

---

## User Story 15.2: User-Specific Rate Limiting
**As a system administrator**  
**I want different rate limits per user role**  
**So that admins have higher limits**

### Priority: MEDIUM
### Owner: Sharen

### Tasks:
1. **Implement Role-Based Rate Limits**
   - Define limits per role:
     - ADMIN: 2x standard limit
     - MANAGER: 1.5x standard limit
     - EMPLOYEE: 1x standard limit
   - Extract role from JWT token
   - Apply appropriate bucket

2. **Add IP-Based Rate Limiting**
   - Global IP rate limit: 100 requests/minute
   - Track by IP address
   - Bypass for whitelisted IPs

3. **Configure Role-Based Limits**
   - `ratelimit.employee.multiplier=1`
   - `ratelimit.manager.multiplier=1.5`
   - `ratelimit.admin.multiplier=2`
   - `ratelimit.ip.global-limit=100`

### Acceptance Criteria:
- ✅ Admins have higher limits
- ✅ IP-based limiting works
- ✅ Whitelisted IPs bypass limits

---

## User Story 15.3: Rate Limit Monitoring & Alerts
**As a system administrator**  
**I want alerts for rate limit violations**  
**So that abuse patterns are detected**

### Priority: LOW
### Owner: Akashat

### Tasks:
1. **Log Rate Limit Violations**
   - Log when limit exceeded
   - Include: user ID, IP, endpoint, timestamp
   - Separate log file: `rate-limit-violations.log`

2. **Create Violation Tracking**
   - Count violations per user/IP in time window
   - Alert if >10 violations in 1 hour
   - Create notification for admin users

3. **Add Metrics**
   - Expose rate limit metrics via Actuator
   - Track: total requests, rejected requests, violation count

### Acceptance Criteria:
- ✅ Violations logged separately
- ✅ Admins alerted for repeated violations
- ✅ Metrics available via Actuator

---

# 📋 EPIC 16: PAGINATION

## User Story 16.1: Goal Listing Pagination
**As a user**  
**I want paginated goal results**  
**So that large goal lists load quickly**

### Priority: HIGH
### Owner: Rudra

### Tasks:
1. **Update GoalRepository**
   - Change return type from `List<Goal>` to `Page<Goal>`
   - Add `Pageable` parameter to query methods
   - Example: `Page<Goal> findByAssignedToUser_UserId(Integer userId, Pageable pageable)`

2. **Update GoalService**
   - Accept `Pageable` parameter
   - Return `Page<Goal>`
   - Add sorting support (by createdDate, priority, status)

3. **Update GoalController**
   - Add `@RequestParam` for pagination:
     - `page` (default 0)
     - `size` (default 20, max 100)
     - `sort` (default "createdDate,desc")
   - Return `Page` object with metadata
   - Example response:
     ```json
     {
       "content": [...],
       "totalElements": 150,
       "totalPages": 8,
       "size": 20,
       "number": 0
     }
     ```

4. **Configure Pagination Defaults**
   - `spring.data.web.pageable.default-page-size=20`
   - `spring.data.web.pageable.max-page-size=100`

### Acceptance Criteria:
- ✅ Goal list paginated with 20 items/page
- ✅ Client can specify page/size/sort
- ✅ Total count returned in response
- ✅ Sorting works correctly

---

## User Story 16.2: Performance Review Pagination
**As a user**  
**I want paginated review results**  
**So that review lists are manageable**

### Priority: HIGH
### Owner: Pratik

### Tasks:
1. **Update PerformanceReviewRepository**
   - Add `Pageable` to all query methods
   - Return `Page<PerformanceReview>`

2. **Update PerformanceReviewService & Controller**
   - Accept pagination parameters
   - Return paginated results
   - Support sorting by: submittedDate, status, managerRating

### Acceptance Criteria:
- ✅ Review list paginated
- ✅ Multiple sort fields supported
- ✅ Filtering works with pagination

---

## User Story 16.3: Notification Pagination
**As a user**  
**I want paginated notifications**  
**So that notification list is performant**

### Priority: HIGH
### Owner: Kashish

### Tasks:
1. **Update NotificationRepository**
   - Add Pageable to findByUser methods
   - Return `Page<Notification>`
   - Sort by createdDate desc by default

2. **Update NotificationController**
   - Add pagination parameters
   - Return paginated results
   - Support filtering + pagination

### Acceptance Criteria:
- ✅ Notifications paginated (default 50/page)
- ✅ Filtering and pagination work together
- ✅ Newest notifications first

---

## User Story 16.4: Audit Log Pagination
**As an admin**  
**I want paginated audit logs**  
**So that large audit trails are navigable**

### Priority: MEDIUM
### Owner: Ankit

### Tasks:
1. **Update AuditLogRepository**
   - Add Pageable to all query methods
   - Return `Page<AuditLog>`
   - Default sort: timestamp desc

2. **Update AuditLogController**
   - Add pagination parameters
   - Support complex filtering + pagination
   - Export respects pagination (export current page)

### Acceptance Criteria:
- ✅ Audit logs paginated
- ✅ Filtering works with pagination
- ✅ Export exports current page or all

---

## User Story 16.5: User & Feedback Pagination
**As a user**  
**I want pagination on all list endpoints**  
**So that application is consistently performant**

### Priority: MEDIUM
### Owner: Sharen

### Tasks:
1. **Update UserRepository**
   - Add Pageable to findAll, findByRole, findByDepartment

2. **Update FeedbackRepository**
   - Add Pageable to query methods

3. **Update Controllers**
   - Add pagination to UserController
   - Add pagination to FeedbackController

### Acceptance Criteria:
- ✅ All list endpoints paginated
- ✅ Consistent pagination behavior
- ✅ Documentation updated

---

# 📊 IMPLEMENTATION SUMMARY

## Phase 1: Critical Foundation (Week 1)
**Priority: HIGH**

1. **Logging Infrastructure** (Kashish, Pratik, Rudra)
   - Story 12.1: Logback configuration
   - Story 12.2: AOP logging
   - Story 12.3: Request logging

2. **Utility Classes** (Ankit, Kashish)
   - Story 14.1: NotificationUtil
   - Story 14.2: AuditUtil
   - Story 14.3: Audit aspect

3. **Parent POM & Profiles** (Ankit, Kashish)
   - Story 13.1: Parent POM
   - Story 13.2: Profile configuration

---

## Phase 2: Core Features (Week 2)
**Priority: HIGH**

1. **Pagination** (Rudra, Pratik, Kashish, Sharen, Ankit)
   - Story 16.1: Goals pagination
   - Story 16.2: Reviews pagination
   - Story 16.3: Notifications pagination
   - Story 16.4: Audit logs pagination
   - Story 16.5: Users & feedback pagination

2. **Critical Schedulers** (Ankit, Kashish)
   - Story 11.1: Review reminders
   - Story 11.2: Stale goal detection

---

## Phase 3: Enhanced Features (Week 3)
**Priority: MEDIUM**

1. **Rate Limiting** (Rudra, Sharen)
   - Story 15.1: Basic rate limiting
   - Story 15.2: Role-based limits

2. **Additional Schedulers** (Pratik, Ankit, Akashat)
   - Story 11.3: Completion follow-up
   - Story 11.4: Notification cleanup
   - Story 11.5: Weekly reports

3. **Advanced Logging** (Sharen, Akashat)
   - Story 12.4: Database query logging
   - Story 12.5: Business event logging

---

## Phase 4: Refinement (Week 4)
**Priority: LOW-MEDIUM**

1. **Module Refactoring** (Pratik)
   - Story 13.3: Common module extraction

2. **Monitoring** (Akashat)
   - Story 15.3: Rate limit monitoring

---

## Team Task Distribution

### Ankit (Lead - 10 tasks)
- Parent POM & profiles (2 stories)
- Utility classes (2 stories)
- Schedulers (2 stories)
- Pagination (1 story)
- **Total: 7 stories**

### Kashish (8 tasks)
- Logging infrastructure (2 stories)
- Utility audit aspect (1 story)
- Profiles (1 story)
- Schedulers (1 story)
- Pagination (1 story)
- **Total: 6 stories**

### Rudra (6 tasks)
- Logging (1 story)
- Pagination (1 story)
- Rate limiting (1 story)
- **Total: 3 stories**

### Pratik (7 tasks)
- Logging AOP (1 story)
- Schedulers (1 story)
- Pagination (1 story)
- Module refactoring (1 story)
- **Total: 4 stories**

### Sharen (5 tasks)
- Logging (1 story)
- Rate limiting (1 story)
- Pagination (1 story)
- **Total: 3 stories**

### Akashat (4 tasks)
- Business logging (1 story)
- Scheduler (1 story)
- Rate limit monitoring (1 story)
- **Total: 3 stories**

---

## Configuration Files to Create

1. **logback-spring.xml**
2. **application-dev.properties**
3. **application-test.properties**
4. **application-prod.properties**
5. **notification-templates.properties**
6. **Parent pom.xml**
7. **Scheduler configuration class**
8. **Rate limit configuration class**

---

## Expected Outcomes

✅ **Logging**: 5 separate log files with rotation  
✅ **Schedulers**: 5 automated jobs running on schedule  
✅ **Profiles**: 3 environments with proper isolation  
✅ **Utilities**: 100% code reuse for notifications & audits  
✅ **Rate Limiting**: API abuse prevented  
✅ **Pagination**: All lists optimized for performance  

---

This plan provides enterprise-grade enhancements that will make your PerformanceTrack system production-ready! Each story has clear tasks and acceptance criteria. Would you like me to start with detailed code for any specific story?
