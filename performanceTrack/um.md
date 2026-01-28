Here's your code with detailed explanatory comments:

```java
package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.dto.LoginRequest;
import com.project.performanceTrack.dto.LoginResponse;
import com.project.performanceTrack.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AUTHENTICATION CONTROLLER
 * ========================
 * Purpose: Handles all authentication-related operations in the system
 * Endpoints: Login, Logout, and Password Change
 * Base URL: /api/v1/auth
 * 
 * This controller is the entry point for user authentication. It receives HTTP requests,
 * validates them, delegates business logic to AuthService, and returns standardized responses.
 */
@RestController  // Tells Spring this class handles REST API requests and automatically converts responses to JSON
@RequestMapping("/api/v1/auth")  // All endpoints in this controller start with /api/v1/auth
public class AuthController {
    
    /**
     * DEPENDENCY INJECTION
     * AuthService contains the actual business logic for authentication
     * @Autowired tells Spring to automatically inject an instance of AuthService
     * This follows the principle of separation of concerns - controller handles HTTP, service handles logic
     */
    @Autowired
    private AuthService authSvc;
    
    /**
     * LOGIN ENDPOINT
     * =============
     * URL: POST /api/v1/auth/login
     * Purpose: Authenticates user and generates JWT token
     * 
     * @param req - Login credentials (email & password) from request body
     * @Valid - Triggers validation rules defined in LoginRequest DTO (e.g., email format, password length)
     * @RequestBody - Tells Spring to extract JSON from HTTP body and convert it to LoginRequest object
     * 
     * Flow:
     * 1. Client sends POST request with JSON: {"email": "user@example.com", "password": "pass123"}
     * 2. Spring validates the request using @Valid annotation
     * 3. Controller calls authSvc.login() to verify credentials
     * 4. Service returns LoginResponse containing JWT token and user info
     * 5. Controller wraps it in ApiResponse and returns to client
     * 
     * Response Example:
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIs...",
     *     "userId": 1,
     *     "role": "EMPLOYEE"
     *   }
     * }
     */
    @PostMapping("/login")  // Maps POST requests to /api/v1/auth/login
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authSvc.login(req);  // Delegate authentication to service layer
        return ApiResponse.success("Login successful", resp);  // Return standardized success response
    }
    
    /**
     * LOGOUT ENDPOINT
     * ==============
     * URL: POST /api/v1/auth/logout
     * Purpose: Invalidates user session and JWT token
     * 
     * @param req - HttpServletRequest object containing request metadata
     * 
     * How it works:
     * 1. User must be logged in (JWT token verified by JwtAuthFilter before reaching here)
     * 2. JwtAuthFilter extracts userId from token and stores it in request.attribute("userId")
     * 3. Controller retrieves userId from request attributes
     * 4. Service layer invalidates the token (adds to blacklist or updates database)
     * 
     * Why HttpServletRequest?
     * - We need to access request attributes set by the authentication filter
     * - getAttribute("userId") retrieves the authenticated user's ID
     * - This ensures only the logged-in user can log themselves out
     * 
     * Security Note:
     * - This endpoint requires a valid JWT token in Authorization header
     * - JwtAuthFilter runs before this method and validates the token
     */
    @PostMapping("/logout")  // Maps POST requests to /api/v1/auth/logout
    public ApiResponse<Void> logout(HttpServletRequest req) {
        // Extract authenticated user's ID from request (set by JwtAuthFilter)
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Invalidate session in service layer (blacklist token, update DB, etc.)
        authSvc.logout(userId);
        
        // Return success response (Void means no data payload)
        return ApiResponse.success("Logout successful");
    }
    
    /**
     * CHANGE PASSWORD ENDPOINT
     * =======================
     * URL: PUT /api/v1/auth/change-password
     * Purpose: Allows authenticated users to change their password
     * 
     * @param body - Map containing oldPassword and newPassword
     * @param req - HttpServletRequest to get authenticated user's ID
     * 
     * Why Map<String, String>?
     * - Quick way to accept flexible JSON without creating a dedicated DTO
     * - body.get("oldPassword") extracts the old password from JSON
     * - body.get("newPassword") extracts the new password from JSON
     * 
     * Request JSON Example:
     * {
     *   "oldPassword": "currentPass123",
     *   "newPassword": "newSecurePass456"
     * }
     * 
     * Security Flow:
     * 1. User must be authenticated (JWT token required)
     * 2. JwtAuthFilter validates token and sets userId in request
     * 3. Service verifies oldPassword matches current password in database
     * 4. If verified, service hashes newPassword and updates database
     * 5. Service may invalidate old tokens and generate new one
     * 
     * Best Practice Note:
     * - Always require old password to prevent unauthorized password changes
     * - New password should be validated (length, complexity) in service layer
     */
    @PutMapping("/change-password")  // PUT is used for update operations
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body,
                                            HttpServletRequest req) {
        // Extract authenticated user's ID from request attributes
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Extract passwords from request body
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        
        // Delegate password change logic to service
        authSvc.changePassword(userId, oldPwd, newPwd);
        
        // Return success response
        return ApiResponse.success("Password changed successfully");
    }
}
```

```java
package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.entity.AuditLog;
import com.project.performanceTrack.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AUDIT LOG CONTROLLER
 * ====================
 * Purpose: Provides admin users access to system audit trails
 * Base URL: /api/v1/audit-logs
 * Access: ADMIN role only
 * 
 * What are Audit Logs?
 * - Records of all important actions in the system (who did what, when)
 * - Examples: "User X logged in", "Manager Y approved goal", "Admin Z created user"
 * - Critical for compliance, security monitoring, and troubleshooting
 * 
 * Why Admin Only?
 * - Audit logs contain sensitive information about all users
 * - Only admins should see complete system activity
 * - Prevents users from knowing they're being monitored
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@PreAuthorize("hasRole('ADMIN')")  // Class-level security - ALL methods require ADMIN role
public class AuditLogController {
    
    /**
     * DIRECT REPOSITORY ACCESS
     * =======================
     * Note: This controller directly uses repository instead of service layer
     * 
     * Why?
     * - Audit log queries are simple (mostly filtering and retrieval)
     * - No complex business logic needed
     * - Reduces unnecessary layers of abstraction
     * 
     * When to use Service vs Repository directly?
     * - Service: Complex logic, multiple operations, business rules
     * - Repository: Simple CRUD, straightforward queries
     */
    @Autowired
    private AuditLogRepository auditRepo;
    
    /**
     * GET AUDIT LOGS WITH FILTERS
     * ===========================
     * URL: GET /api/v1/audit-logs?userId=1&action=LOGIN&startDt=...&endDt=...
     * Purpose: Retrieve audit logs with optional filtering
     * 
     * Query Parameters (all optional):
     * - userId: Filter by specific user's actions
     * - action: Filter by action type (LOGIN, LOGOUT, CREATE_USER, etc.)
     * - startDt: Filter logs from this date-time onwards
     * - endDt: Filter logs up to this date-time
     * 
     * Example URLs:
     * 1. All logs: GET /api/v1/audit-logs
     * 2. User's logs: GET /api/v1/audit-logs?userId=5
     * 3. Login actions: GET /api/v1/audit-logs?action=LOGIN
     * 4. Date range: GET /api/v1/audit-logs?startDt=2024-01-01T00:00:00&endDt=2024-01-31T23:59:59
     * 
     * @RequestParam(required = false):
     * - Makes the parameter optional
     * - If not provided in URL, value will be null
     * - Allows multiple filtering options without creating separate endpoints
     * 
     * @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME):
     * - Tells Spring how to parse date strings from URL
     * - ISO format: 2024-01-28T14:30:00
     * - Automatically converts string to LocalDateTime object
     */
    @GetMapping
    public ApiResponse<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDt) {
        
        /**
         * CONDITIONAL FILTERING LOGIC
         * ==========================
         * This if-else chain determines which repository method to call based on provided filters
         * Order matters: More specific filters first, general queries last
         * 
         * Priority:
         * 1. userId - Most specific (one user's logs)
         * 2. action - Specific action type (all users doing one action)
         * 3. date range - Time-based filtering
         * 4. all logs - No filter (fallback)
         */
        List<AuditLog> logs;
        
        // FILTER BY USER ID
        // Get all actions performed by a specific user
        if (userId != null) {
            logs = auditRepo.findByUser_UserIdOrderByTimestampDesc(userId);
            // findByUser_UserId - Navigates the relationship (AuditLog -> User -> userId)
            // OrderByTimestampDesc - Newest logs first (most recent activity at top)
        } 
        // FILTER BY ACTION TYPE
        // Get all logs of a specific action (e.g., all LOGIN attempts)
        else if (action != null) {
            logs = auditRepo.findByActionOrderByTimestampDesc(action);
        } 
        // FILTER BY DATE RANGE
        // Get logs within a specific time period
        else if (startDt != null && endDt != null) {
            logs = auditRepo.findByTimestampBetweenOrderByTimestampDesc(startDt, endDt);
            // Between - SQL BETWEEN operator for range queries
        } 
        // NO FILTER - GET ALL LOGS
        // Fallback when no filters provided
        else {
            logs = auditRepo.findAll();
            // Warning: This could return thousands of records in production
            // Consider adding pagination for large datasets
        }
        
        // Return filtered logs wrapped in standard response
        return ApiResponse.success("Audit logs retrieved", logs);
    }
    
    /**
     * EXPORT AUDIT LOGS
     * =================
     * URL: POST /api/v1/audit-logs/export
     * Purpose: Generate downloadable file of audit logs for compliance/reporting
     * 
     * Why POST instead of GET?
     * - Export is an action that creates a resource (file)
     * - POST is semantically correct for creating resources
     * - Allows passing complex filter criteria in request body (not shown here)
     * 
     * Request Body Example:
     * {
     *   "format": "CSV"  // or "EXCEL", "PDF", "JSON"
     * }
     * 
     * Response Example:
     * {
     *   "success": true,
     *   "message": "Audit logs export initiated",
     *   "data": "/exports/audit_logs_1706457600000.csv"
     * }
     * 
     * Current Implementation:
     * - This is a PLACEHOLDER implementation
     * - Real implementation would:
     *   1. Query audit logs based on filters
     *   2. Generate CSV/Excel/PDF file
     *   3. Save file to storage (local disk or cloud)
     *   4. Return download URL or file path
     * 
     * Production Considerations:
     * - Large datasets: Use async processing (return job ID, notify when complete)
     * - Storage: Save to cloud storage (AWS S3, Azure Blob)
     * - Security: Generate temporary signed URLs for downloads
     * - Cleanup: Auto-delete export files after 24 hours
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")  // Double-check ADMIN role (redundant but explicit)
    public ApiResponse<String> exportLogs(@RequestBody Map<String, String> body) {
        // Extract desired format from request, default to CSV if not specified
        String format = body.getOrDefault("format", "CSV");
        
        // Generate unique filename using current timestamp
        String filePath = "/exports/audit_logs_" + System.currentTimeMillis() + "." + format.toLowerCase();
        
        // TODO: Implement actual file generation logic here
        // - Query audit logs
        // - Convert to specified format (CSV/Excel/PDF)
        // - Save to file system or cloud storage
        
        // Return file path where export will be available
        return ApiResponse.success("Audit logs export initiated", filePath);
    }
}
```

```java
package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.enums.UserRole;
import com.project.performanceTrack.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * USER REPOSITORY
 * ==============
 * Purpose: Database access layer for User entity
 * Extends: JpaRepository<User, Integer>
 *   - User: The entity this repository manages
 *   - Integer: The type of the primary key (userId)
 * 
 * What is a Repository?
 * - Interface between your application and the database
 * - Provides methods to Create, Read, Update, Delete (CRUD) data
 * - Spring Data JPA automatically implements these methods at runtime
 * 
 * How Spring Data JPA Works:
 * 1. You define an interface with method names following naming conventions
 * 2. Spring generates the implementation automatically (no code needed!)
 * 3. Spring converts method names to SQL queries
 * 
 * Example: findByEmail("john@example.com")
 * Spring generates: SELECT * FROM users WHERE email = 'john@example.com'
 */
@Repository  // Tells Spring this is a database repository bean
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * INHERITED METHODS FROM JpaRepository
     * ===================================
     * You don't see these methods here, but they're automatically available:
     * 
     * - save(User user) - Insert or update user
     * - findById(Integer id) - Find user by ID
     * - findAll() - Get all users
     * - deleteById(Integer id) - Delete user by ID
     * - count() - Count total users
     * - existsById(Integer id) - Check if user exists
     * 
     * These are inherited from JpaRepository, so you get them for free!
     */
    
    /**
     * FIND USER BY EMAIL
     * =================
     * Method name: findByEmail
     * Generated SQL: SELECT * FROM users WHERE email = ?
     * 
     * Return type: Optional<User>
     * - Optional is a container that may or may not contain a value
     * - Prevents NullPointerException by forcing explicit handling of "not found" case
     * 
     * Why Optional?
     * - Email should be unique, so we expect 0 or 1 result
     * - If user exists: Optional.of(user)
     * - If not found: Optional.empty()
     * 
     * Usage in Service layer:
     * Optional<User> userOpt = userRepo.findByEmail("john@example.com");
     * if (userOpt.isPresent()) {
     *     User user = userOpt.get();
     *     // User found, proceed
     * } else {
     *     throw new UserNotFoundException();
     * }
     * 
     * Or use modern Java style:
     * User user = userRepo.findByEmail("john@example.com")
     *                    .orElseThrow(() -> new UserNotFoundException());
     */
    Optional<User> findByEmail(String email);
    
    /**
     * FIND USERS BY ROLE
     * =================
     * Method name: findByRole
     * Generated SQL: SELECT * FROM users WHERE role = ?
     * 
     * Return type: List<User>
     * - Returns all users with the specified role
     * - Empty list if no users found (never null)
     * 
     * Use cases:
     * - Get all managers: findByRole(UserRole.MANAGER)
     * - Get all employees: findByRole(UserRole.EMPLOYEE)
     * - Get all admins: findByRole(UserRole.ADMIN)
     * 
     * Example result:
     * List<User> managers = userRepo.findByRole(UserRole.MANAGER);
     * // Returns: [User{id=2, name="Alice"}, User{id=5, name="Bob"}]
     */
    List<User> findByRole(UserRole role);
    
    /**
     * FIND USERS BY DEPARTMENT
     * =======================
     * Method name: findByDepartment
     * Generated SQL: SELECT * FROM users WHERE department = ?
     * 
     * Use cases:
     * - Get all IT department employees
     * - Get all HR department employees
     * - Department-wise reports
     * 
     * Example:
     * List<User> itTeam = userRepo.findByDepartment("IT");
     * System.out.println("IT Department size: " + itTeam.size());
     */
    List<User> findByDepartment(String department);
    
    /**
     * FIND USERS BY STATUS
     * ===================
     * Method name: findByStatus
     * Generated SQL: SELECT * FROM users WHERE status = ?
     * 
     * UserStatus enum values: ACTIVE, INACTIVE, SUSPENDED
     * 
     * Use cases:
     * - Get all active users for system operations
     * - Get all inactive users for cleanup/archival
     * - Get suspended users for review
     * 
     * Example:
     * List<User> activeUsers = userRepo.findByStatus(UserStatus.ACTIVE);
     * // Only active users can log in and use the system
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * FIND TEAM MEMBERS BY MANAGER
     * ===========================
     * Method name: findByManager_UserId
     * Generated SQL: SELECT * FROM users WHERE manager_id = ?
     * 
     * Relationship Navigation:
     * - User entity has a field: @ManyToOne User manager
     * - This creates a foreign key: manager_id in users table
     * - Underscore (_) in method name navigates the relationship
     * - findByManager_UserId means: find by manager.userId
     * 
     * Database Structure:
     * users table:
     * | user_id | name    | manager_id |
     * |---------|---------|------------|
     * | 1       | Alice   | null       | (Manager)
     * | 2       | Bob     | 1          | (Reports to Alice)
     * | 3       | Charlie | 1          | (Reports to Alice)
     * | 4       | Diana   | 2          | (Reports to Bob)
     * 
     * Example Usage:
     * List<User> aliceTeam = userRepo.findByManager_UserId(1);
     * // Returns: [Bob, Charlie] - Alice's direct reports
     * 
     * List<User> bobTeam = userRepo.findByManager_UserId(2);
     * // Returns: [Diana] - Bob's direct reports
     * 
     * Use cases:
     * - Manager viewing their team
     * - Organizational hierarchy
     * - Performance review workflows
     * - Goal approval chains
     */
    List<User> findByManager_UserId(Integer managerId);
    
    /**
     * SPRING DATA JPA NAMING CONVENTIONS
     * ==================================
     * 
     * findBy + PropertyName:
     * - findByEmail → WHERE email = ?
     * - findByRole → WHERE role = ?
     * 
     * And / Or:
     * - findByEmailAndStatus → WHERE email = ? AND status = ?
     * - findByRoleOrDepartment → WHERE role = ? OR department = ?
     * 
     * Comparison operators:
     * - findByAgeGreaterThan → WHERE age > ?
     * - findByAgeLessThan → WHERE age < ?
     * - findBySalaryBetween → WHERE salary BETWEEN ? AND ?
     * 
     * Pattern matching:
     * - findByNameContaining → WHERE name LIKE %?%
     * - findByEmailStartingWith → WHERE email LIKE ?%
     * 
     * Ordering:
     * - findByRoleOrderByNameAsc → WHERE role = ? ORDER BY name ASC
     * 
     * Limiting results:
     * - findTop3ByDepartment → WHERE department = ? LIMIT 3
     * 
     * Null handling:
     * - findByManagerIsNull → WHERE manager IS NULL
     * - findByManagerIsNotNull → WHERE manager IS NOT NULL
     */
}
```

```java
package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.dto.CreateUserRequest;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * USER MANAGEMENT CONTROLLER
 * =========================
 * Purpose: Handles all user-related operations (CRUD operations on users)
 * Base URL: /api/v1/users
 * 
 * Access Control:
 * - Some endpoints require specific roles (ADMIN, MANAGER)
 * - @PreAuthorize annotation enforces role-based security
 * - JwtAuthFilter validates token before request reaches controller
 * 
 * Typical Flow:
 * Request → JwtAuthFilter → Security Check → Controller → Service → Repository → Database
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    /**
     * DEPENDENCY INJECTION
     * UserService contains business logic for user operations
     * Controller delegates to service, keeping controller thin and focused on HTTP handling
     */
    @Autowired
    private UserService userSvc;
    
    /**
     * GET ALL USERS
     * ============
     * URL: GET /api/v1/users
     * Access: ADMIN or MANAGER only
     * Purpose: Retrieve list of all users in the system
     * 
     * @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')"):
     * - hasAnyRole - User must have at least one of the specified roles
     * - ADMIN: Can see all users for administration
     * - MANAGER: Can see all users for team assignment, performance tracking
     * 
     * Why not EMPLOYEE?
     * - Employees shouldn't see complete user list (privacy)
     * - Employees only need to see their team members (separate endpoint)
     * 
     * Use cases:
     * - Admin: User management dashboard
     * - Manager: Assigning team members to projects
     * - Manager: Viewing department structure
     * 
     * Response Example:
     * {
     *   "success": true,
     *   "message": "Users retrieved",
     *   "data": [
     *     {"userId": 1, "name": "Alice", "email": "alice@company.com", "role": "ADMIN"},
     *     {"userId": 2, "name": "Bob", "email": "bob@company.com", "role": "MANAGER"},
     *     ...
     *   ]
     * }
     */
    @GetMapping  // Maps GET requests to /api/v1/users
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userSvc.getAllUsers();  // Service fetches all users from database
        return ApiResponse.success("Users retrieved", users);
    }
    
    /**
     * GET USER BY ID
     * =============
     * URL: GET /api/v1/users/{userId}
     * Access: Any authenticated user (no @PreAuthorize annotation)
     * Purpose: Retrieve detailed information about a specific user
     * 
     * @PathVariable Integer userId:
     * - Extracts userId from URL path
     * - Example: GET /api/v1/users/5 → userId = 5
     * - Spring automatically converts string "5" to Integer 5
     * 
     * No role restriction because:
     * - Users can view their own profile
     * - Employees can view colleague profiles (for collaboration)
     * - Service layer should implement additional security:
     *   * Employees can only view users in their department
     *   * Or only view basic info (hide sensitive fields like salary)
     * 
     * Use cases:
     * - User viewing their own profile
     * - Manager viewing team member profile
     * - Employee viewing colleague's contact info
     * 
     * Security Note:
     * - Consider implementing in service: if (requesterId != userId && !isManager) throw Forbidden
     * - Or return different detail levels based on role
     */
    @GetMapping("/{userId}")  // {userId} is a path variable
    public ApiResponse<User> getUserById(@PathVariable Integer userId) {
        User user = userSvc.getUserById(userId);  // Service fetches user by ID
        return ApiResponse.success("User retrieved", user);
    }
    
    /**
     * CREATE NEW USER
     * ==============
     * URL: POST /api/v1/users
     * Access: ADMIN only
     * Purpose: Create a new user account in the system
     * 
     * @Valid @RequestBody CreateUserRequest req:
     * - @RequestBody: Converts JSON from request body to CreateUserRequest object
     * - @Valid: Triggers validation rules defined in CreateUserRequest DTO
     * 
     * CreateUserRequest Example:
     * {
     *   "email": "newuser@company.com",
     *   "name": "John Doe",
     *   "role": "EMPLOYEE",
     *   "department": "IT",
     *   "managerId": 2
     * }
     * 
     * Validation in CreateUserRequest:
     * - @NotBlank String email - Email cannot be empty
     * - @Email String email - Must be valid email format
     * - @NotNull UserRole role - Role is required
     * - @Size(min=2, max=100) String name - Name length constraints
     * 
     * HttpServletRequest httpReq:
     * - Used to extract adminId from request attributes
     * - adminId is set by JwtAuthFilter after token validation
     * - We need adminId for audit logging (who created this user)
     * 
     * Security Flow:
     * 1. Only ADMIN can create users (enforced by @PreAuthorize)
     * 2. Service validates business rules (email unique, valid manager ID)
     * 3. Service hashes default password
     * 4. Service creates user in database
     * 5. Service logs audit trail (Admin X created User Y)
     * 
     * Why ADMIN only?
     * - Creating users affects system access and security
     * - Prevents unauthorized account creation
     * - Ensures proper onboarding process
     */
    @PostMapping  // Maps POST requests to /api/v1/users
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> createUser(@Valid @RequestBody CreateUserRequest req, 
                                        HttpServletRequest httpReq) {
        // Extract admin's ID for audit logging
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        
        // Delegate user creation to service layer
        User user = userSvc.createUser(req, adminId);
        
        // Return created user (with generated ID, timestamps, etc.)
        return ApiResponse.success("User created", user);
    }
    
    /**
     * UPDATE USER
     * ==========
     * URL: PUT /api/v1/users/{userId}
     * Access: ADMIN only
     * Purpose: Update existing user's information
     * 
     * @PathVariable Integer userId:
     * - ID of user to update from URL
     * - Example: PUT /api/v1/users/5 → Update user with ID 5
     * 
     * @Valid @RequestBody CreateUserRequest req:
     * - Note: We reuse CreateUserRequest DTO for updates
     * - This is common practice when create/update fields are similar
     * - Alternative: Create separate UpdateUserRequest DTO if fields differ
     * 
     * Update vs Create:
     * - Create: userId doesn't exist yet, generated by database
     * - Update: userId exists, we're modifying existing record
     * 
     * What can be updated?
     * - Name, email, department, role, manager, status
     * - Password: Separate endpoint (change-password)
     * - userId: Never (primary key is immutable)
     * 
     * Service layer logic:
     * 1. Verify user exists (throw UserNotFoundException if not)
     * 2. Validate new data (email uniqueness, valid manager ID)
     * 3. Update only changed fields (optimistic approach)
     * 4. Log audit trail (Admin X updated User Y)
     * 
     * Security considerations:
     * - Admin can change user's role (promote to manager, demote to employee)
     * - Admin can deactivate users (status = INACTIVE)
     * - Admin can reassign reporting structure (change manager)
     * - Consider adding approval workflow for critical changes
     * 
     * Response:
     * - Returns updated User object with all current values
     * - Frontend can update UI with latest data
     */
    @PutMapping("/{userId}")  // PUT is idempotent (same request multiple times = same result)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Integer userId,
                                        @Valid @RequestBody CreateUserRequest req,
                                        HttpServletRequest httpReq) {
        // Extract admin's ID for audit logging
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        
        // Delegate update to service layer
        User user = userSvc.updateUser(userId, req, adminId);
        
        // Return updated user
        return ApiResponse.success("User updated", user);
    }
    
    /**
     * GET TEAM MEMBERS
     * ===============
     * URL: GET /api/v1/users/{userId}/team
     * Access: MANAGER only
     * Purpose: Get list of direct reports (team members) for a manager
     * 
     * @PathVariable Integer userId:
     * - ID of the manager whose team we want to retrieve
     * - Example: GET /api/v1/users/2/team → Get manager #2's team
     * 
     * Why MANAGER only?
     * - Managers need to see their team for:
     *   * Performance reviews
     *   * Goal approval
     *   * Task assignment
     *   * Leave approvals
     * - Employees don't manage others, so they don't need this
     * - Admins can see all users via getAllUsers() endpoint
     * 
     * Security Enhancement (recommended):
     * Service should verify: requesterId == userId
     * - Ensures managers can only see THEIR OWN team
     * - Prevents Manager A from viewing Manager B's team
     * 
     * Example:
     * public List<User> getTeamMembers(Integer managerId, Integer requesterId) {
     *     if (!managerId.equals(requesterId)) {
     *         throw new UnauthorizedException("Cannot view other manager's team");
     *     }
     *     return userRepo.findByManager_UserId(managerId);
     * }
     * 
     * Database Query:
     * - Uses UserRepository.findByManager_UserId(managerId)
     * - SQL: SELECT * FROM users WHERE manager_id = ?
     * - Returns all users who report to this manager
     * 
     * Organizational Structure Example:
     * 
     * Alice (MANAGER, userId=2)
     * ├── Bob (EMPLOYEE, userId=5)
     * ├── Charlie (EMPLOYEE, userId=7)
     * └── Diana (EMPLOYEE, userId=9)
     * 
     * GET /api/v1/users/2/team returns: [Bob, Charlie, Diana]
     * 
     * Use cases:
     * - Manager dashboard showing team overview
     * - Performance review period - manager sees reviewees
     * - Goal setting - manager assigns goals to team
     * - Leave approval - manager sees who's on their team
     * 
     * Response Example:
     * {
     *   "success": true,
     *   "message": "Team members retrieved",
     *   "data": [
     *     {
     *       "userId": 5,
     *       "name": "Bob",
     *       "email": "bob@company.com",
     *       "role": "EMPLOYEE",
     *       "department": "IT"
     *     },
     *     {
     *       "userId": 7,
     *       "name": "Charlie",
     *       "email": "charlie@company.com",
     *       "role": "EMPLOYEE",
     *       "department": "IT"
     *     }
     *   ]
     * }
     */
    @GetMapping("/{userId}/team")  // Nested resource: users/{id}/team
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<User>> getTeam(@PathVariable Integer userId) {
        // Fetch team members from service
        List<User> team = userSvc.getTeamMembers(userId);
        
        // Return team list
        return ApiResponse.success("Team members retrieved", team);
    }
}

/**
 * ARCHITECTURAL NOTES
 * ==================
 * 
 * 1. Controller Responsibilities:
 *    - Receive HTTP requests
 *    - Validate request format (@Valid)
 *    - Enforce security (@PreAuthorize)
 *    - Delegate to service layer
 *    - Return standardized responses (ApiResponse)
 * 
 * 2. What Controller Should NOT Do:
 *    - Business logic (goes in service)
 *    - Database queries (goes in repository)
 *    - Complex validation (goes in service)
 *    - Transaction management (service layer handles this)
 * 
 * 3. RESTful URL Design:
 *    - /users - Collection resource
 *    - /users/{id} - Single resource
 *    - /users/{id}/team - Sub-resource (nested)
 *    - Use nouns, not verbs (❌ /users/getById, ✅ /users/{id})
 * 
 * 4. HTTP Methods:
 *    - GET: Retrieve data (no side effects, idempotent)
 *    - POST: Create new resource
 *    - PUT: Update existing resource (full replacement)
 *    - PATCH: Partial update (not used here)
 *    - DELETE: Remove resource (not implemented here)
 * 
 * 5. Security Layers:
 *    - JwtAuthFilter: Validates token, extracts userId
 *    - @PreAuthorize: Role-based authorization
 *    - Service layer: Business logic authorization (own data vs others' data)
 * 
 * 6. Error Handling (not shown here):
 *    - Should have @ExceptionHandler methods
 *    - Or global @ControllerAdvice for consistent error responses
 *    - Map exceptions to HTTP status codes
 */
```

These comments explain **why** we do things, **how** they work under the hood, and **what** the real-world implications are - perfect for understanding at a trainee level!
