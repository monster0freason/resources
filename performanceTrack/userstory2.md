# PerformanceTrack Enhancement Plan - Professional Enterprise Features

I'll create a detailed plan for adding these enterprise features while keeping the project structure intact and changes minimal. Let me break this down into manageable user stories and tasks.

---

## 📋 EPIC 11: ENTERPRISE FEATURES ENHANCEMENT

### Owner: Ankit (Lead), Support from All Team Members

---

## 🎯 ENHANCEMENT OVERVIEW

### Features to Add:
1. **Schedulers** - Automated tasks for maintenance and notifications
2. **Logging (SLF4J + Logback)** - Comprehensive application logging
3. **Multi-Profile Maven Setup** - Parent POM with dev/test/prod profiles
4. **Utility Classes** - Reusable notification and audit utilities
5. **Rate Limiting** - API throttling for security
6. **Pagination** - Efficient data retrieval for large datasets

---

## 📊 PRIORITY MATRIX

| Feature | Priority | Complexity | Impact | Stories |
|---------|----------|------------|--------|---------|
| Logging | HIGH | LOW | HIGH | 1 |
| Utility Classes | HIGH | LOW | HIGH | 1 |
| Pagination | HIGH | MEDIUM | HIGH | 1 |
| Maven Profiles | MEDIUM | LOW | MEDIUM | 1 |
| Schedulers | MEDIUM | MEDIUM | MEDIUM | 1 |
| Rate Limiting | LOW | MEDIUM | MEDIUM | 1 |

---

## 🔧 USER STORY 11.1: LOGGING INFRASTRUCTURE

**As a developer/operations team**  
**I want comprehensive logging throughout the application**  
**So that I can debug issues and monitor application health**

### Tasks - Ankit (Lead: 4 hours)

#### 11.1.1: Logback Configuration (1 hour)
- [ ] Create `src/main/resources/logback-spring.xml`
- [ ] Configure log levels by environment:
  - **DEV**: DEBUG for com.project.performanceTrack, INFO for others
  - **TEST**: INFO for all
  - **PROD**: WARN for application, ERROR for others
- [ ] Setup log file rotation:
  - Daily rollover
  - Max file size: 10MB
  - Keep 30 days of logs
- [ ] Configure console appender for development
- [ ] Configure file appender for production (`/var/log/performancetrack/`)

#### 11.1.2: Logging Pattern Standardization (1 hour)
- [ ] Define log format pattern:
  ```
  %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
  ```
- [ ] Add correlation ID pattern for request tracking
- [ ] Document logging levels:
  - **ERROR**: System failures, exceptions
  - **WARN**: Business rule violations, potential issues
  - **INFO**: Business events (login, goal created, review submitted)
  - **DEBUG**: Detailed flow information (method entry/exit, variable values)
  - **TRACE**: Very detailed debugging (SQL queries, JSON payloads)

#### 11.1.3: Strategic Logging Implementation (1.5 hours)
- [ ] **Controllers**: Log incoming requests and responses
  - Entry: `log.info("Received {} request: {}", method, endpoint)`
  - Exit: `log.info("Returning response with status: {}", status)`
  - Error: `log.error("Error processing request", exception)`
  
- [ ] **Services**: Log business operations
  - Entry: `log.debug("Executing {}.{} with params: {}", class, method, params)`
  - Success: `log.info("Successfully completed: {}", operation)`
  - Validation: `log.warn("Validation failed: {}", reason)`
  - Error: `log.error("Operation failed: {}", operation, exception)`

- [ ] **Security**: Log authentication/authorization
  - Success: `log.info("User {} logged in successfully", email)`
  - Failure: `log.warn("Failed login attempt for user: {}", email)`
  - Access Denied: `log.warn("Access denied for user {} to resource {}", userId, resource)`

- [ ] **Repository/Database**: Log slow queries (optional)
  - Slow query: `log.warn("Slow query detected: {} ms", executionTime)`

#### 11.1.4: Sensitive Data Protection (0.5 hours)
- [ ] Create custom log sanitizer to mask:
  - Passwords: `password=***`
  - JWT tokens: `token=***`
  - Email (partial): `r***@company.com`
- [ ] Document sensitive fields to never log
- [ ] Add unit tests for sanitizer

### Acceptance Criteria
✅ All layers (Controller, Service, Repository) have appropriate logging  
✅ Log files rotate daily and maintain 30-day history  
✅ Different log levels configured per environment  
✅ No sensitive data (passwords, full tokens) in logs  
✅ Console logs readable during development  
✅ File logs structured for production monitoring  

### Implementation Locations
| File/Package | Logging Focus |
|--------------|---------------|
| `AuthController` | Login/logout/password change |
| `AuthService` | Authentication logic, token generation |
| `GoalService` | Goal CRUD, approval workflow |
| `PerformanceReviewService` | Review workflow |
| `JwtAuthFilter` | Token validation |
| `GlobalExceptionHandler` | Exception details |

---

## 🛠️ USER STORY 11.2: UTILITY CLASSES FOR NOTIFICATION & AUDIT

**As a developer**  
**I want reusable utility classes for notifications and audit logs**  
**So that I can reduce code duplication and maintain consistency**

### Tasks - Ankit (Lead: 3 hours)

#### 11.2.1: Notification Utility Class (1.5 hours)
- [ ] Create `com.project.performanceTrack.util.NotificationUtil`
- [ ] Implement methods:
  ```java
  // Goal-related notifications
  void notifyGoalSubmitted(User employee, User manager, Goal goal)
  void notifyGoalApproved(User employee, Goal goal)
  void notifyGoalChangeRequested(User employee, Goal goal, String comments)
  void notifyGoalResubmitted(User manager, User employee, Goal goal)
  void notifyGoalCompletionSubmitted(User manager, User employee, Goal goal)
  void notifyGoalCompletionApproved(User employee, Goal goal)
  void notifyAdditionalEvidenceRequired(User employee, Goal goal, String reason)
  
  // Review-related notifications
  void notifySelfAssessmentSubmitted(User manager, User employee, PerformanceReview review)
  void notifyReviewCompleted(User employee, PerformanceReview review)
  void notifyReviewAcknowledged(User manager, User employee)
  
  // User-related notifications
  void notifyAccountCreated(User user)
  
  // Reminder notifications
  void notifyReviewCycleEnding(User user, ReviewCycle cycle, int daysRemaining)
  void notifyPendingApprovals(User manager, int pendingCount)
  ```

- [ ] Add priority mapping:
  - `HIGH`: Completion approvals, password changes
  - `MEDIUM`: Goal submissions, review submissions
  - `LOW`: Reminders, acknowledgments

- [ ] Set `actionRequired` flag logic:
  - `true`: Pending approvals, change requests
  - `false`: Informational notifications

- [ ] Inject `NotificationRepository` via constructor
- [ ] Add `@Component` annotation
- [ ] Add logging to each utility method
- [ ] Write unit tests with Mockito

#### 11.2.2: Audit Utility Class (1.5 hours)
- [ ] Create `com.project.performanceTrack.util.AuditUtil`
- [ ] Implement methods:
  ```java
  // Authentication audits
  void logLogin(User user, String ipAddress, boolean success)
  void logLogout(User user)
  void logPasswordChange(User user)
  
  // User management audits
  void logUserCreated(User admin, User newUser)
  void logUserUpdated(User admin, User updatedUser)
  
  // Goal audits
  void logGoalCreated(User employee, Goal goal)
  void logGoalApproved(User manager, Goal goal)
  void logGoalUpdated(User employee, Goal goal, String changeType)
  void logGoalCompletionSubmitted(User employee, Goal goal)
  void logGoalCompletionApproved(User manager, Goal goal)
  
  // Review audits
  void logSelfAssessmentSubmitted(User employee, PerformanceReview review)
  void logManagerReviewSubmitted(User manager, PerformanceReview review)
  void logReviewAcknowledged(User employee, PerformanceReview review)
  
  // Review cycle audits
  void logReviewCycleCreated(User admin, ReviewCycle cycle)
  void logReviewCycleUpdated(User admin, ReviewCycle cycle)
  
  // Report audits
  void logReportGenerated(User user, Report report)
  
  // Generic audit method
  void logAction(User user, String action, String details, 
                 String entityType, Integer entityId, String status)
  ```

- [ ] Auto-capture timestamp in all methods
- [ ] Extract IP address from `HttpServletRequest` (optional parameter)
- [ ] Inject `AuditLogRepository` via constructor
- [ ] Add `@Component` annotation
- [ ] Add logging to utility methods
- [ ] Write unit tests

#### 11.2.3: Refactor Existing Code to Use Utilities (Same as above tasks - minimal changes)
- [ ] Replace notification creation in `GoalService` with `NotificationUtil` calls
- [ ] Replace notification creation in `PerformanceReviewService` with `NotificationUtil` calls
- [ ] Replace notification creation in `UserService` with `NotificationUtil` calls
- [ ] Replace audit log creation in all services with `AuditUtil` calls
- [ ] Remove duplicate code
- [ ] Run regression tests to ensure no functionality broken

### Acceptance Criteria
✅ All notification creation uses `NotificationUtil`  
✅ All audit log creation uses `AuditUtil`  
✅ Code duplication reduced by >60%  
✅ Utilities are well-documented with Javadoc  
✅ Unit tests achieve >90% coverage on utilities  
✅ Existing functionality unchanged (regression tests pass)  

### Refactoring Impact
| Service | Before (Lines) | After (Lines) | Reduction |
|---------|---------------|---------------|-----------|
| `GoalService` | ~500 | ~350 | 30% |
| `PerformanceReviewService` | ~350 | ~250 | 28% |
| `UserService` | ~200 | ~150 | 25% |
| `AuthService` | ~150 | ~120 | 20% |

---

## 📄 USER STORY 11.3: PAGINATION FOR LIST ENDPOINTS

**As a frontend developer**  
**I want paginated responses for list APIs**  
**So that large datasets load efficiently**

### Tasks - Kashish (Lead: 2 hours)

#### 11.3.1: Pagination Infrastructure (0.5 hours)
- [ ] Add Spring Data Commons dependency (already included)
- [ ] Create `PageResponse<T>` wrapper class:
  ```java
  @Data
  public class PageResponse<T> {
      private List<T> content;
      private int pageNumber;
      private int pageSize;
      private long totalElements;
      private int totalPages;
      private boolean first;
      private boolean last;
  }
  ```
- [ ] Create utility method in `ApiResponse`:
  ```java
  public static <T> ApiResponse<PageResponse<T>> success(String msg, Page<T> page)
  ```

#### 11.3.2: Update Repository Methods (0.5 hours)
- [ ] **GoalRepository**: Change return types from `List<Goal>` to `Page<Goal>`
  - `findByAssignedToUser_UserId(Integer userId, Pageable pageable)`
  - `findByAssignedManager_UserId(Integer managerId, Pageable pageable)`
  - `findByStatus(GoalStatus status, Pageable pageable)`

- [ ] **PerformanceReviewRepository**: Change return types
  - `findByUser_UserId(Integer userId, Pageable pageable)`
  - `findByCycle_CycleId(Integer cycleId, Pageable pageable)`

- [ ] **NotificationRepository**: Change return types
  - `findByUser_UserIdOrderByCreatedDateDesc(Integer userId, Pageable pageable)`
  - `findByUser_UserIdAndStatusOrderByCreatedDateDesc(Integer userId, NotificationStatus status, Pageable pageable)`

- [ ] **AuditLogRepository**: Change return types
  - `findByUser_UserIdOrderByTimestampDesc(Integer userId, Pageable pageable)`
  - `findByActionOrderByTimestampDesc(String action, Pageable pageable)`

#### 11.3.3: Update Service Layer (0.5 hours)
- [ ] Update `GoalService.getGoalsByUser()` to accept `Pageable`
- [ ] Update `GoalService.getGoalsByManager()` to accept `Pageable`
- [ ] Update `PerformanceReviewService.getReviewsByUser()` to accept `Pageable`
- [ ] Update `PerformanceReviewService.getReviewsByCycle()` to accept `Pageable`
- [ ] Update `NotificationController` methods to accept `Pageable`

#### 11.3.4: Update Controllers (0.5 hours)
- [ ] Add `Pageable` parameters to controller methods:
  ```java
  @GetMapping
  public ApiResponse<PageResponse<Goal>> getGoals(
      HttpServletRequest httpReq,
      @RequestParam(required = false) Integer userId,
      @RequestParam(required = false) Integer mgrId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdDate") String sortBy,
      @RequestParam(defaultValue = "DESC") String sortDir
  )
  ```

- [ ] Create `PageRequest` objects:
  ```java
  Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
      Sort.by(sortBy).ascending() : 
      Sort.by(sortBy).descending();
  Pageable pageable = PageRequest.of(page, size, sort);
  ```

- [ ] Return `PageResponse` wrapped in `ApiResponse`

- [ ] Update endpoints:
  - `GET /api/v1/goals`
  - `GET /api/v1/performance-reviews`
  - `GET /api/v1/notifications`
  - `GET /api/v1/audit-logs`
  - `GET /api/v1/users` (optional)

#### 11.3.5: Testing & Documentation (Same as above)
- [ ] Update Postman collection with pagination parameters
- [ ] Add examples:
  - Default: `GET /api/v1/goals?page=0&size=20`
  - Custom: `GET /api/v1/goals?page=1&size=10&sortBy=title&sortDir=ASC`
- [ ] Test with large datasets (100+ records)
- [ ] Update API documentation (Swagger)

### Acceptance Criteria
✅ All list endpoints support pagination (page, size, sortBy, sortDir)  
✅ Default page size is 20  
✅ Maximum page size enforced (e.g., 100)  
✅ Response includes pagination metadata (totalElements, totalPages)  
✅ Sorting works for multiple fields  
✅ Postman tests updated with pagination examples  
✅ Performance improved for large datasets  

### Pagination Endpoints
| Endpoint | Default Sort | Sortable Fields |
|----------|--------------|-----------------|
| `/api/v1/goals` | `createdDate DESC` | title, priority, status, startDate, endDate, createdDate |
| `/api/v1/performance-reviews` | `createdDate DESC` | status, submittedDate, reviewCompletedDate |
| `/api/v1/notifications` | `createdDate DESC` | createdDate, status, priority |
| `/api/v1/audit-logs` | `timestamp DESC` | timestamp, action, user |

---

## 🗓️ USER STORY 11.4: SCHEDULED TASKS

**As a system administrator**  
**I want automated scheduled tasks**  
**So that maintenance and notifications happen automatically**

### Tasks - Pratik (Lead: 3 hours)

#### 11.4.1: Enable Scheduling (0.5 hours)
- [ ] Add `@EnableScheduling` to main application class
- [ ] Create `com.project.performanceTrack.scheduler` package
- [ ] Create `SchedulerConfig` class for configuration
- [ ] Configure thread pool size for schedulers:
  ```java
  @Configuration
  public class SchedulerConfig implements SchedulingConfigurer {
      @Override
      public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
          taskRegistrar.setScheduler(taskExecutor());
      }
      
      @Bean
      public Executor taskExecutor() {
          return Executors.newScheduledThreadPool(5);
      }
  }
  ```

#### 11.4.2: Notification Reminder Scheduler (1 hour)
- [ ] Create `NotificationScheduler` class
- [ ] Inject `NotificationUtil`, `GoalRepository`, `ReviewCycleRepository`, `UserRepository`

**Task 1: Pending Approval Reminders (Daily at 9 AM)**
```java
@Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
public void sendPendingApprovalReminders()
```
- [ ] Find all managers with pending goal approvals (status = PENDING, older than 2 days)
- [ ] Count pending goals per manager
- [ ] Send notification: "You have X pending goal approvals"
- [ ] Log execution

**Task 2: Review Cycle Ending Reminders (Daily at 10 AM)**
```java
@Scheduled(cron = "0 0 10 * * *") // Every day at 10 AM
public void sendReviewCycleEndingReminders()
```
- [ ] Find active review cycle
- [ ] Calculate days remaining until end date
- [ ] If days remaining = 30, 15, 7, or 3:
  - Find all employees
  - Send reminder: "Review cycle ends in X days. Complete your goals."
- [ ] Log execution

**Task 3: Pending Completion Reminders (Every Monday at 9 AM)**
```java
@Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
public void sendPendingCompletionReminders()
```
- [ ] Find managers with goals in PENDING_COMPLETION_APPROVAL status (older than 3 days)
- [ ] Count pending completions per manager
- [ ] Send notification: "You have X goals pending completion approval"
- [ ] Log execution

#### 11.4.3: Data Cleanup Scheduler (1 hour)
- [ ] Create `CleanupScheduler` class
- [ ] Inject `NotificationRepository`, `AuditLogRepository`

**Task 1: Old Notification Cleanup (Monthly - 1st day at 2 AM)**
```java
@Scheduled(cron = "0 0 2 1 * *") // 1st of every month at 2 AM
public void cleanupOldNotifications()
```
- [ ] Delete READ notifications older than 90 days
- [ ] Log count of deleted notifications
- [ ] Send summary to admin

**Task 2: Old Audit Log Archival (Quarterly - 1st day at 3 AM)**
```java
@Scheduled(cron = "0 0 3 1 1,4,7,10 *") // Jan 1, Apr 1, Jul 1, Oct 1 at 3 AM
public void archiveOldAuditLogs()
```
- [ ] Find audit logs older than 1 year
- [ ] Archive to CSV file (optional - just log count for MVP)
- [ ] Delete archived logs (optional - keep for MVP)
- [ ] Log execution

**Task 3: Inactive User Cleanup (Yearly - Jan 1 at 4 AM)**
```java
@Scheduled(cron = "0 0 4 1 1 *") // Every Jan 1 at 4 AM
public void cleanupInactiveUsers()
```
- [ ] Find users with status INACTIVE for >1 year
- [ ] Log count
- [ ] Send report to admin (do not auto-delete for MVP)

#### 11.4.4: Health Check Scheduler (0.5 hours - Optional)
- [ ] Create `HealthCheckScheduler` class

**Task: Database Connection Check (Every 5 minutes)**
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void checkDatabaseConnection()
```
- [ ] Execute simple query: `SELECT 1`
- [ ] If fails, log error and send alert to admin
- [ ] Track consecutive failures

### Acceptance Criteria
✅ All schedulers run at specified times  
✅ Notification reminders sent correctly  
✅ Cleanup tasks execute without errors  
✅ Scheduler execution logged  
✅ No performance impact on application  
✅ Schedulers can be disabled via configuration  
✅ Exception handling prevents scheduler crashes  

### Scheduler Configuration
| Scheduler | Frequency | Time | Purpose |
|-----------|-----------|------|---------|
| Pending Approvals | Daily | 9 AM | Remind managers |
| Review Cycle Ending | Daily | 10 AM | Remind employees |
| Pending Completions | Weekly (Mon) | 9 AM | Remind managers |
| Old Notifications | Monthly | 2 AM (1st) | Cleanup |
| Old Audit Logs | Quarterly | 3 AM (1st) | Archive/cleanup |
| Inactive Users | Yearly | 4 AM (Jan 1) | Report |
| DB Health Check | Every 5 min | - | Monitor |

---

## 🏗️ USER STORY 11.5: MULTI-PROFILE MAVEN SETUP

**As a developer**  
**I want separate build profiles for dev/test/prod**  
**So that environment-specific configurations are managed properly**

### Tasks - Ankit (Lead: 2 hours)

#### 11.5.1: Parent POM Structure (0.5 hours)
- [ ] Keep existing `pom.xml` (no parent POM needed for MVP)
- [ ] Add profiles section to existing `pom.xml`:
  ```xml
  <profiles>
      <profile>
          <id>dev</id>
          <activation>
              <activeByDefault>true</activeByDefault>
          </activation>
          <properties>
              <spring.profiles.active>dev</spring.profiles.active>
          </properties>
      </profile>
      
      <profile>
          <id>test</id>
          <properties>
              <spring.profiles.active>test</spring.profiles.active>
          </properties>
      </profile>
      
      <profile>
          <id>prod</id>
          <properties>
              <spring.profiles.active>prod</spring.profiles.active>
          </properties>
      </profile>
  </profiles>
  ```

#### 11.5.2: Environment-Specific Properties (1 hour)
- [ ] Create `application-dev.properties`:
  ```properties
  # Database
  spring.datasource.url=jdbc:mysql://localhost:3306/performance_track_dev
  spring.jpa.show-sql=true
  spring.jpa.hibernate.ddl-auto=update
  
  # Logging
  logging.level.com.project.performanceTrack=DEBUG
  logging.level.org.springframework.web=DEBUG
  
  # JWT
  jwt.expiration=86400000  # 24 hours
  
  # Scheduler
  scheduler.enabled=true
  ```

- [ ] Create `application-test.properties`:
  ```properties
  # Database (H2 in-memory)
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.datasource.driver-class-name=org.h2.Driver
  spring.jpa.hibernate.ddl-auto=create-drop
  
  # Logging
  logging.level.com.project.performanceTrack=INFO
  
  # JWT
  jwt.expiration=3600000  # 1 hour
  
  # Scheduler
  scheduler.enabled=false
  ```

- [ ] Create `application-prod.properties`:
  ```properties
  # Database
  spring.datasource.url=${DB_URL}
  spring.datasource.username=${DB_USERNAME}
  spring.datasource.password=${DB_PASSWORD}
  spring.jpa.show-sql=false
  spring.jpa.hibernate.ddl-auto=validate
  
  # Logging
  logging.level.com.project.performanceTrack=WARN
  logging.level.root=ERROR
  logging.file.name=/var/log/performancetrack/app.log
  
  # JWT
  jwt.expiration=28800000  # 8 hours
  jwt.secret=${JWT_SECRET}
  
  # Scheduler
  scheduler.enabled=true
  
  # Security
  server.ssl.enabled=true  # For HTTPS (optional)
  ```

#### 11.5.3: Build & Run Configuration (0.5 hours)
- [ ] Document Maven commands:
  ```bash
  # Build for dev (default)
  mvn clean package
  
  # Build for test
  mvn clean package -Ptest
  
  # Build for prod
  mvn clean package -Pprod
  
  # Run with specific profile
  mvn spring-boot:run -Pdev
  java -jar target/performanceTrack-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
  ```

- [ ] Add conditional scheduler enabling:
  ```java
  @Configuration
  @EnableScheduling
  @ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
  public class SchedulerConfig { ... }
  ```

- [ ] Update `.gitignore`:
  ```
  application-local.properties
  *.log
  /logs/
  ```

### Acceptance Criteria
✅ Three profiles (dev, test, prod) configured  
✅ Each profile has separate properties file  
✅ Dev profile active by default  
✅ Test profile uses H2 in-memory database  
✅ Prod profile reads from environment variables  
✅ Schedulers disabled in test profile  
✅ Build commands documented  
✅ No sensitive data in version control  

### Profile Configuration Summary
| Property | Dev | Test | Prod |
|----------|-----|------|------|
| Database | MySQL (local) | H2 (memory) | MySQL (cloud) |
| DDL Auto | update | create-drop | validate |
| Show SQL | true | false | false |
| Log Level | DEBUG | INFO | WARN |
| JWT Expiry | 24h | 1h | 8h |
| Schedulers | Enabled | Disabled | Enabled |

---

## 🚦 USER STORY 11.6: RATE LIMITING

**As a system administrator**  
**I want rate limiting on APIs**  
**So that abuse and DDoS attacks are prevented**

### Tasks - Akashat (Lead: 3 hours)

#### 11.6.1: Rate Limiting Infrastructure (1 hour)
- [ ] Add Bucket4j dependency to `pom.xml`:
  ```xml
  <dependency>
      <groupId>com.github.vladimir-bukhtoyarov</groupId>
      <artifactId>bucket4j-core</artifactId>
      <version>8.1.0</version>
  </dependency>
  ```

- [ ] Create `com.project.performanceTrack.config.RateLimitConfig`
- [ ] Define rate limit rules:
  ```java
  public enum RateLimitType {
      LOGIN(5, 1),           // 5 requests per minute
      PASSWORD_CHANGE(3, 5), // 3 requests per 5 minutes
      GOAL_CREATE(20, 1),    // 20 requests per minute
      API_GENERAL(100, 1);   // 100 requests per minute (default)
      
      private final long capacity;
      private final long refillMinutes;
  }
  ```

- [ ] Create bucket storage (in-memory ConcurrentHashMap for MVP):
  ```java
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
  ```

#### 11.6.2: Rate Limit Interceptor (1 hour)
- [ ] Create `RateLimitInterceptor` implementing `HandlerInterceptor`
- [ ] Extract client identifier:
  - Use user ID if authenticated
  - Use IP address if not authenticated
- [ ] Check bucket for rate limit:
  ```java
  public boolean preHandle(HttpServletRequest request, ...) {
      String key = getUserIdOrIp(request);
      RateLimitType limitType = determineLimitType(request);
      Bucket bucket = resolveBucket(key, limitType);
      
      if (bucket.tryConsume(1)) {
          return true; // Allow request
      } else {
          response.setStatus(429); // Too Many Requests
          response.getWriter().write("Rate limit exceeded. Try again later.");
          return false;
      }
  }
  ```

- [ ] Register interceptor in `WebMvcConfigurer`:
  ```java
  @Configuration
  public class WebConfig implements WebMvcConfigurer {
      @Autowired
      private RateLimitInterceptor rateLimitInterceptor;
      
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
          registry.addInterceptor(rateLimitInterceptor)
                  .addPathPatterns("/api/v1/**")
                  .excludePathPatterns("/api/v1/auth/login"); // Special handling
      }
  }
  ```

#### 11.6.3: Apply Rate Limits to Critical Endpoints (1 hour)
- [ ] **Authentication Endpoints** (Strict):
  - `POST /api/v1/auth/login` → 5 req/min per IP
  - `PUT /api/v1/auth/change-password` → 3 req/5min per user

- [ ] **Write Endpoints** (Moderate):
  - `POST /api/v1/goals` → 20 req/min per user
  - `POST /api/v1/performance-reviews` → 10 req/min per user
  - `POST /api/v1/users` → 10 req/min per admin

- [ ] **Read Endpoints** (Lenient):
  - `GET /api/v1/**` → 100 req/min per user (default)

- [ ] **Report Generation** (Strict):
  - `POST /api/v1/reports/generate` → 5 req/hour per user

- [ ] Add custom annotation for rate limiting (optional):
  ```java
  @RateLimit(capacity = 10, refillMinutes = 1)
  @PostMapping("/goals")
  public ApiResponse<Goal> createGoal(...) { ... }
  ```

- [ ] Log rate limit violations:
  ```java
  log.warn("Rate limit exceeded for user/IP: {} on endpoint: {}", key, request.getRequestURI());
  ```

- [ ] Add audit log entry for repeated violations (optional)

### Acceptance Criteria
✅ Rate limiting active on all API endpoints  
✅ Login endpoint has strictest limit (5 req/min)  
✅ Write endpoints have moderate limits  
✅ Read endpoints have lenient limits  
✅ 429 status code returned when limit exceeded  
✅ Rate limits per user (authenticated) or IP (unauthenticated)  
✅ No performance degradation under normal load  
✅ Rate limit violations logged  

### Rate Limit Configuration
| Endpoint | Limit | Window | Identifier |
|----------|-------|--------|------------|
| `POST /api/v1/auth/login` | 5 | 1 min | IP address |
| `PUT /api/v1/auth/change-password` | 3 | 5 min | User ID |
| `POST /api/v1/goals` | 20 | 1 min | User ID |
| `POST /api/v1/performance-reviews` | 10 | 1 min | User ID |
| `POST /api/v1/reports/generate` | 5 | 1 hour | User ID |
| `GET /api/v1/**` | 100 | 1 min | User ID |

---

## 📊 IMPLEMENTATION PRIORITY & TIMELINE

### Phase 1: Foundation (Week 1)
**Priority: HIGH | Time: 6 hours**
- [ ] **Day 1-2**: User Story 11.1 - Logging (Ankit) - 4 hours
- [ ] **Day 2-3**: User Story 11.2 - Utility Classes (Ankit) - 3 hours
- [ ] **Day 3**: User Story 11.5 - Maven Profiles (Ankit) - 2 hours

**Milestone**: Application has logging, utilities, and multi-profile support

### Phase 2: Optimization (Week 2)
**Priority: HIGH | Time: 5 hours**
- [ ] **Day 1-2**: User Story 11.3 - Pagination (Kashish) - 2 hours
- [ ] **Day 2-3**: User Story 11.4 - Schedulers (Pratik) - 3 hours

**Milestone**: Application optimized for large datasets and automation

### Phase 3: Security (Week 2-3)
**Priority: MEDIUM | Time: 3 hours**
- [ ] **Day 4-5**: User Story 11.6 - Rate Limiting (Akashat) - 3 hours

**Milestone**: Application protected against abuse

---

## 📋 TESTING CHECKLIST

### Logging Tests
- [ ] Log files created in correct locations
- [ ] Log rotation working (create 10MB file, verify rotation)
- [ ] Different log levels per environment
- [ ] No sensitive data in logs (search for "password", "token")
- [ ] Console logs readable in dev mode

### Utility Tests
- [ ] All notification utility methods create correct notifications
- [ ] All audit utility methods create correct audit logs
- [ ] Refactored services use utilities (no direct repository calls)
- [ ] Unit tests for utilities achieve >90% coverage
- [ ] Integration tests pass after refactoring

### Pagination Tests
- [ ] Page 0, size 20 returns first 20 records
- [ ] Page 1, size 20 returns next 20 records
- [ ] Sorting by different fields works (ASC/DESC)
- [ ] Total elements count accurate
- [ ] Empty page returns empty list (not error)
- [ ] Invalid page number handled gracefully

### Scheduler Tests
- [ ] Schedulers execute at correct times (use `@Scheduled(fixedDelay = 5000)` for testing)
- [ ] Notification reminders sent to correct users
- [ ] Cleanup tasks delete correct records
- [ ] Schedulers handle exceptions without crashing
- [ ] Schedulers disabled in test profile
- [ ] Scheduler execution logged

### Profile Tests
- [ ] Build succeeds for all profiles (dev, test, prod)
- [ ] Each profile loads correct properties file
- [ ] Test profile uses H2 database
- [ ] Prod profile reads from environment variables
- [ ] Application runs with each profile

### Rate Limiting Tests
- [ ] Login endpoint blocks after 5 requests in 1 minute
- [ ] Rate limit resets after window expires
- [ ] Different users have separate rate limits
- [ ] 429 status code returned when limited
- [ ] Rate limit violations logged
- [ ] Normal usage not affected

---

## 📖 DOCUMENTATION REQUIREMENTS

### Logging Documentation
- [ ] Update README with logging section:
  - Log file locations
  - Log levels by environment
  - How to change log levels
  - How to search logs
- [ ] Create `LOGGING.md` with:
  - Logging best practices
  - What to log at each level
  - Sensitive data guidelines

### Utility Documentation
- [ ] Add Javadoc to all utility methods
- [ ] Create `UTILITIES.md` with:
  - How to use NotificationUtil
  - How to use AuditUtil
  - Examples of common patterns

### Pagination Documentation
- [ ] Update API documentation (Swagger) with pagination parameters
- [ ] Update Postman collection with pagination examples
- [ ] Create `PAGINATION.md` with:
  - How to use pagination
  - Query parameter reference
  - Best practices

### Scheduler Documentation
- [ ] Create `SCHEDULERS.md` with:
  - List of all schedulers
  - Execution frequency
  - How to enable/disable
  - How to monitor

### Profile Documentation
- [ ] Update README with profile section:
  - How to build for each profile
  - How to run with each profile
  - Environment variables required for prod
- [ ] Create `DEPLOYMENT.md` with production deployment guide

### Rate Limiting Documentation
- [ ] Update API documentation with rate limits
- [ ] Create `RATE_LIMITING.md` with:
  - Rate limits per endpoint
  - How to handle 429 errors
  - How to request limit increase

---

## 🎯 SUCCESS METRICS

### Code Quality
- [ ] Logging added to all layers (Controller, Service, Security)
- [ ] Code duplication reduced by >60% with utilities
- [ ] No hardcoded notification/audit creation in services
- [ ] All utility classes have >90% test coverage

### Performance
- [ ] Pagination reduces response size by >80% for large lists
- [ ] Pagination response time <200ms for 1000+ records
- [ ] Schedulers execute without blocking main application
- [ ] Rate limiting adds <10ms latency

### Reliability
- [ ] All schedulers run on time (±1 second)
- [ ] Log rotation prevents disk space issues
- [ ] Rate limiting blocks abuse without false positives
- [ ] No scheduler crashes from exceptions

### Maintainability
- [ ] Environment switching requires only profile change
- [ ] New notifications added in <5 minutes (use utility)
- [ ] New audit logs added in <5 minutes (use utility)
- [ ] New schedulers added in <30 minutes

---

## 🚀 ROLLOUT PLAN

### Week 1: Foundation
1. **Logging** (Monday-Tuesday)
   - Add Logback configuration
   - Add logging to all layers
   - Test log files and rotation

2. **Utilities** (Wednesday-Thursday)
   - Create NotificationUtil and AuditUtil
   - Refactor existing services
   - Run regression tests

3. **Profiles** (Friday)
   - Create profile properties files
   - Test build and run for each profile

### Week 2: Optimization & Security
1. **Pagination** (Monday)
   - Update repositories and services
   - Update controllers
   - Test with large datasets

2. **Schedulers** (Tuesday-Wednesday)
   - Create scheduler classes
   - Add notification reminders
   - Add cleanup tasks
   - Test scheduler execution

3. **Rate Limiting** (Thursday-Friday)
   - Add Bucket4j dependency
   - Create rate limit interceptor
   - Apply limits to endpoints
   - Test rate limiting

### Week 3: Testing & Documentation
1. **Comprehensive Testing** (Monday-Wednesday)
   - Run all unit tests
   - Run all integration tests
   - Performance testing
   - Security testing

2. **Documentation** (Thursday-Friday)
   - Update README
   - Create feature-specific docs
   - Update API documentation
   - Update Postman collection

3. **Code Review & Deployment** (Friday)
   - Team code review
   - Final testing
   - Deploy to production (if ready)

---

## 📞 TEAM RESPONSIBILITIES

| Feature | Owner | Support | Hours | Complexity |
|---------|-------|---------|-------|------------|
| Logging | Ankit | All | 4 | LOW |
| Utilities | Ankit | All | 3 | LOW |
| Pagination | Kashish | Ankit | 2 | MEDIUM |
| Schedulers | Pratik | Ankit | 3 | MEDIUM |
| Maven Profiles | Ankit | - | 2 | LOW |
| Rate Limiting | Akashat | Ankit | 3 | MEDIUM |
| **TOTAL** | - | - | **17** | - |

---

## ✅ FINAL CHECKLIST

### Before Starting
- [ ] All team members understand the plan
- [ ] Development environment set up
- [ ] Dependencies verified
- [ ] Git branches created

### During Development
- [ ] Daily standup to track progress
- [ ] Code committed frequently
- [ ] Tests written alongside code
- [ ] Documentation updated continuously

### Before Completion
- [ ] All user stories completed
- [ ] All tests passing
- [ ] Code reviewed by team
- [ ] Documentation complete
- [ ] Postman collection updated
- [ ] Demo prepared

### After Completion
- [ ] Features demoed to stakeholders
- [ ] Feedback collected
- [ ] Production deployment checklist ready
- [ ] Monitoring plan in place

---

## 🎓 LEARNING OUTCOMES

By completing this epic, the team will learn:

1. **Professional Logging**
   - SLF4J and Logback usage
   - Log levels and when to use them
   - Log file management and rotation

2. **Code Reusability**
   - Creating utility classes
   - Reducing code duplication
   - Dependency injection patterns

3. **Data Optimization**
   - Spring Data pagination
   - Query optimization
   - Response size management

4. **Task Automation**
   - Spring @Scheduled annotation
   - Cron expressions
   - Background job management

5. **Environment Management**
   - Maven profiles
   - Environment-specific configuration
   - Externalized configuration

6. **API Security**
   - Rate limiting strategies
   - Abuse prevention
   - Performance under load

---

This plan maintains the **MINIMUM VIABLE** approach while adding professional enterprise features. The changes are **MINIMAL** and focused on the **MOST NECESSARY** improvements for a production-ready fresher project.

Total estimated time: **17 hours** across the team over 2-3 weeks.
