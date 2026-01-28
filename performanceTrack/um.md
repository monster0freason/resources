I'll add detailed comments to explain each file:

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
 * Authentication Controller
 * 
 * Purpose: Handles all authentication-related HTTP requests
 * Endpoints: login, logout, change password
 * Access: Public for login, authenticated users for logout/password change
 */
@RestController // Marks this class as a REST API controller that returns JSON responses
@RequestMapping("/api/v1/auth") // Base URL path for all endpoints in this controller
public class AuthController {
    
    @Autowired // Automatically injects AuthService instance
    private AuthService authSvc;
    
    /**
     * Login Endpoint
     * 
     * URL: POST /api/v1/auth/login
     * Purpose: Authenticates user and returns JWT token
     * Request Body: { "email": "user@example.com", "password": "password123" }
     * Response: JWT token + user details
     * Access: Public (no authentication required)
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        // @Valid - Validates the request body against LoginRequest validation rules
        // @RequestBody - Converts JSON request body to LoginRequest object
        LoginResponse resp = authSvc.login(req); // Calls service layer to authenticate
        return ApiResponse.success("Login successful", resp); // Returns success response with token
    }
    
    /**
     * Logout Endpoint
     * 
     * URL: POST /api/v1/auth/logout
     * Purpose: Logs out the current user and creates audit trail
     * Response: Success message
     * Access: Authenticated users only (requires valid JWT token)
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest req) {
        // HttpServletRequest - Gets the HTTP request object to extract user info
        Integer userId = (Integer) req.getAttribute("userId"); // userId set by JWT filter/interceptor
        authSvc.logout(userId); // Logs the logout action in audit log
        return ApiResponse.success("Logout successful"); // Returns success with no data
    }
    
    /**
     * Change Password Endpoint
     * 
     * URL: PUT /api/v1/auth/change-password
     * Purpose: Allows authenticated users to change their password
     * Request Body: { "oldPassword": "old123", "newPassword": "new456" }
     * Response: Success message
     * Access: Authenticated users only
     */
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body,
                                            HttpServletRequest req) {
        // @RequestBody Map - Accepts flexible JSON object with key-value pairs
        Integer userId = (Integer) req.getAttribute("userId"); // Get logged-in user ID
        String oldPwd = body.get("oldPassword"); // Extract old password from request
        String newPwd = body.get("newPassword"); // Extract new password from request
        authSvc.changePassword(userId, oldPwd, newPwd); // Validates and updates password
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
 * Audit Log Controller
 * 
 * Purpose: Provides admin access to view and export system audit logs
 * Tracks: All user actions (login, logout, data changes, etc.)
 * Access: ADMIN role only
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@PreAuthorize("hasRole('ADMIN')") // ALL endpoints in this controller require ADMIN role
public class AuditLogController {
    
    @Autowired
    private AuditLogRepository auditRepo; // Direct repository access (simple CRUD operations)
    
    /**
     * Get Audit Logs with Filters
     * 
     * URL: GET /api/v1/audit-logs?userId=1&action=LOGIN&startDt=2024-01-01T00:00:00
     * Purpose: Retrieves audit logs with optional filtering
     * Query Parameters:
     *   - userId: Filter by specific user
     *   - action: Filter by action type (LOGIN, LOGOUT, etc.)
     *   - startDt/endDt: Filter by date range
     * Response: List of audit logs
     */
    @GetMapping
    public ApiResponse<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) Integer userId, // Optional filter by user
            @RequestParam(required = false) String action, // Optional filter by action
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDt, // Start date
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDt) { // End date
        
        List<AuditLog> logs;
        
        // Apply filters based on what parameters are provided
        if (userId != null) {
            // Find all logs for specific user, ordered newest first
            logs = auditRepo.findByUser_UserIdOrderByTimestampDesc(userId);
        } else if (action != null) {
            // Find all logs for specific action type
            logs = auditRepo.findByActionOrderByTimestampDesc(action);
        } else if (startDt != null && endDt != null) {
            // Find all logs within date range
            logs = auditRepo.findByTimestampBetweenOrderByTimestampDesc(startDt, endDt);
        } else {
            // No filters - return all logs
            logs = auditRepo.findAll();
        }
        
        return ApiResponse.success("Audit logs retrieved", logs);
    }
    
    /**
     * Export Audit Logs
     * 
     * URL: POST /api/v1/audit-logs/export
     * Purpose: Exports audit logs to file (CSV, JSON, etc.)
     * Request Body: { "format": "CSV" }
     * Response: File path where export is saved
     * Note: This is a placeholder implementation
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')") // Redundant but explicit - only admins can export
    public ApiResponse<String> exportLogs(@RequestBody Map<String, String> body) {
        String format = body.getOrDefault("format", "CSV"); // Default to CSV if not specified
        // Generate unique filename with timestamp
        String filePath = "/exports/audit_logs_" + System.currentTimeMillis() + "." + format.toLowerCase();
        // TODO: In real implementation, this would generate actual file using Apache POI or similar
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
 * User Repository
 * 
 * Purpose: Database access layer for User entity
 * Extends: JpaRepository<User, Integer>
 *   - User: Entity type
 *   - Integer: Primary key type (userId)
 * Provides: Automatic CRUD operations + custom query methods
 */
@Repository // Marks this as a data access component
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * Find User by Email
     * 
     * Purpose: Used for login authentication
     * Query: SELECT * FROM users WHERE email = ?
     * Returns: Optional<User> - empty if not found
     * Spring Data JPA auto-generates this query from method name
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find Users by Role
     * 
     * Purpose: Get all users with specific role (ADMIN, MANAGER, EMPLOYEE)
     * Query: SELECT * FROM users WHERE role = ?
     * Returns: List of users (empty list if none found)
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find Users by Department
     * 
     * Purpose: Get all users in a department (HR, IT, Sales, etc.)
     * Query: SELECT * FROM users WHERE department = ?
     * Returns: List of users in that department
     */
    List<User> findByDepartment(String department);
    
    /**
     * Find Users by Status
     * 
     * Purpose: Get active/inactive users
     * Query: SELECT * FROM users WHERE status = ?
     * Returns: List of users with that status
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * Find Team Members by Manager ID
     * 
     * Purpose: Get all employees reporting to a specific manager
     * Query: SELECT * FROM users WHERE manager_id = ?
     * Returns: List of employees under that manager
     * Note: Uses manager relationship (manager_UserId refers to User.manager.userId)
     */
    List<User> findByManager_UserId(Integer managerId);
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
 * User Management Controller
 * 
 * Purpose: CRUD operations for user management
 * Access: Role-based (ADMIN, MANAGER, EMPLOYEE)
 * Features: Create, read, update users, view team members
 */
@RestController
@RequestMapping("/api/v1/users") // Base URL for user operations
public class UserController {
    
    @Autowired
    private UserService userSvc; // Business logic layer
    
    /**
     * Get All Users
     * 
     * URL: GET /api/v1/users
     * Purpose: Retrieve list of all users in system
     * Response: List of User objects
     * Access: ADMIN and MANAGER roles only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Requires ADMIN OR MANAGER role
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userSvc.getAllUsers();
        return ApiResponse.success("Users retrieved", users);
    }
    
    /**
     * Get User by ID
     * 
     * URL: GET /api/v1/users/{userId}
     * Example: GET /api/v1/users/5
     * Purpose: Retrieve specific user details
     * Response: Single User object
     * Access: Any authenticated user (can view others' profiles)
     */
    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable Integer userId) {
        // @PathVariable - Extracts userId from URL path
        User user = userSvc.getUserById(userId);
        return ApiResponse.success("User retrieved", user);
    }
    
    /**
     * Create New User
     * 
     * URL: POST /api/v1/users
     * Purpose: Add new user to the system
     * Request Body: { "name": "John Doe", "email": "john@example.com", ... }
     * Response: Created User object
     * Access: ADMIN only
     * Audit: Logs who created the user (adminId)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only admins can create users
    public ApiResponse<User> createUser(@Valid @RequestBody CreateUserRequest req, 
                                        HttpServletRequest httpReq) {
        // Get ID of admin performing the action (for audit trail)
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        User user = userSvc.createUser(req, adminId); // Service creates user and logs action
        return ApiResponse.success("User created", user);
    }
    
    /**
     * Update Existing User
     * 
     * URL: PUT /api/v1/users/{userId}
     * Example: PUT /api/v1/users/5
     * Purpose: Update user details (name, email, role, department, etc.)
     * Request Body: { "name": "John Updated", ... }
     * Response: Updated User object
     * Access: ADMIN only
     * Audit: Logs who updated the user
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Integer userId,
                                        @Valid @RequestBody CreateUserRequest req,
                                        HttpServletRequest httpReq) {
        Integer adminId = (Integer) httpReq.getAttribute("userId"); // For audit trail
        User user = userSvc.updateUser(userId, req, adminId);
        return ApiResponse.success("User updated", user);
    }
    
    /**
     * Get Team Members
     * 
     * URL: GET /api/v1/users/{userId}/team
     * Example: GET /api/v1/users/3/team
     * Purpose: Get all employees reporting to a manager
     * Response: List of User objects (team members)
     * Access: MANAGER role only
     * Use Case: Manager viewing their direct reports
     */
    @GetMapping("/{userId}/team")
    @PreAuthorize("hasRole('MANAGER')") // Only managers can view teams
    public ApiResponse<List<User>> getTeam(@PathVariable Integer userId) {
        List<User> team = userSvc.getTeamMembers(userId); // Gets users where manager_id = userId
        return ApiResponse.success("Team members retrieved", team);
    }
}
```

```java
package com.project.performanceTrack.service;

import com.project.performanceTrack.dto.LoginRequest;
import com.project.performanceTrack.dto.LoginResponse;
import com.project.performanceTrack.entity.AuditLog;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.exception.UnauthorizedException;
import com.project.performanceTrack.repository.AuditLogRepository;
import com.project.performanceTrack.repository.UserRepository;
import com.project.performanceTrack.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Authentication Service
 * 
 * Purpose: Business logic for authentication operations
 * Functions: Login validation, token generation, password management, audit logging
 * Security: Uses BCrypt password hashing and JWT tokens
 */
@Service // Marks this as a service layer component
public class AuthService {
    
    @Autowired
    private UserRepository userRepo; // Database access for users
    
    @Autowired
    private PasswordEncoder pwdEncoder; // BCrypt password hasher
    
    @Autowired
    private JwtUtil jwtUtil; // JWT token generator/validator
    
    @Autowired
    private AuditLogRepository auditRepo; // Database access for audit logs
    
    /**
     * User Login Method
     * 
     * Process:
     * 1. Find user by email
     * 2. Verify password matches hash in database
     * 3. Check if user account is active
     * 4. Generate JWT token
     * 5. Log successful login
     * 6. Return token and user details
     * 
     * Security: Passwords never stored in plain text, only BCrypt hashes
     */
    public LoginResponse login(LoginRequest req) {
        // Step 1: Find user by email
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        // Note: Generic error message prevents user enumeration attacks
        
        // Step 2: Verify password
        if (!pwdEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            // pwdEncoder.matches() - Compares plain text password with BCrypt hash
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Step 3: Check if user account is active
        if (user.getStatus().name().equals("INACTIVE")) {
            throw new UnauthorizedException("Account is inactive");
        }
        
        // Step 4: Generate JWT token
        // Token contains: email, userId, role (encoded and signed)
        String token = jwtUtil.generateToken(
            user.getEmail(), 
            user.getUserId(), 
            user.getRole().name()
        );
        
        // Step 5: Create audit log for successful login
        AuditLog log = new AuditLog();
        log.setUser(user); // Link log to user
        log.setAction("LOGIN"); // Action type
        log.setDetails("User logged in successfully"); // Action description
        log.setStatus("SUCCESS"); // Result status
        log.setTimestamp(LocalDateTime.now()); // When it happened
        auditRepo.save(log); // Save to database
        
        // Step 6: Return login response with token and user info
        return new LoginResponse(
            token, // JWT token for future requests
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getDepartment()
        );
    }
    
    /**
     * User Logout Method
     * 
     * Process:
     * 1. Find user by ID
     * 2. Create audit log entry
     * 
     * Note: JWT tokens are stateless, so logout only creates audit trail
     * Token remains valid until expiration (typically handled client-side)
     */
    public void logout(Integer userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            // Create audit log for logout action
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction("LOGOUT");
            log.setDetails("User logged out");
            log.setStatus("SUCCESS");
            log.setTimestamp(LocalDateTime.now());
            auditRepo.save(log);
        }
        // If user not found, silently ignore (already logged out or invalid)
    }
    
    /**
     * Change Password Method
     * 
     * Process:
     * 1. Find user by ID
     * 2. Verify old password is correct
     * 3. Hash and save new password
     * 4. Create audit log
     * 
     * Security: 
     * - Requires old password verification (prevents unauthorized changes)
     * - New password is BCrypt hashed before storage
     * - Action is logged for security audit
     */
    public void changePassword(Integer userId, String oldPwd, String newPwd) {
        // Step 1: Find user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        // Step 2: Verify old password is correct
        if (!pwdEncoder.matches(oldPwd, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        // Step 3: Update password with new hash
        user.setPasswordHash(pwdEncoder.encode(newPwd)); // BCrypt hash new password
        userRepo.save(user); // Save to database
        
        // Step 4: Create audit log
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction("PASSWORD_CHANGED");
        log.setDetails("User changed password");
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
    }
}
```

**Key Concepts Summary:**

1. **Controller Layer**: Handles HTTP requests/responses, validates input, manages authorization
2. **Service Layer**: Contains business logic, coordinates between repositories
3. **Repository Layer**: Database access using Spring Data JPA
4. **Security**: JWT tokens, BCrypt password hashing, role-based access control
5. **Audit Trail**: All important actions logged for compliance/security
