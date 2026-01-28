I'll add detailed comments to explain the code in a way that's easy to understand for a trainee level.

```java
package com.project.performanceTrack.service;

import com.project.performanceTrack.dto.CreateUserRequest;
import com.project.performanceTrack.entity.AuditLog;
import com.project.performanceTrack.entity.Notification;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.enums.NotificationStatus;
import com.project.performanceTrack.enums.NotificationType;
import com.project.performanceTrack.exception.BadRequestException;
import com.project.performanceTrack.exception.ResourceNotFoundException;
import com.project.performanceTrack.repository.AuditLogRepository;
import com.project.performanceTrack.repository.NotificationRepository;
import com.project.performanceTrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserService: Business logic layer for user management
 * 
 * This service handles all user-related operations like:
 * - Creating new users
 * - Retrieving user information
 * - Updating user details
 * - Managing team relationships
 * 
 * It acts as a middleman between the Controller (which receives HTTP requests)
 * and the Repository (which talks to the database)
 */
@Service  // Tells Spring this is a service component that contains business logic
public class UserService {
    
    // ========== DEPENDENCIES (Auto-injected by Spring) ==========
    
    @Autowired  // Spring automatically provides the UserRepository instance
    private UserRepository userRepo;  // Handles database operations for User table
    
    @Autowired
    private PasswordEncoder pwdEncoder;  // Encrypts passwords before saving to database
    
    @Autowired
    private NotificationRepository notifRepo;  // Handles database operations for Notification table
    
    @Autowired
    private AuditLogRepository auditRepo;  // Handles database operations for AuditLog table
    
    
    // ========== METHOD 1: GET ALL USERS ==========
    
    /**
     * Retrieves all users from the database
     * 
     * Use case: Admin dashboard showing all users in the system
     * 
     * @return List of all User objects
     */
    public List<User> getAllUsers() {
        // findAll() is a built-in JPA method that runs: SELECT * FROM users
        return userRepo.findAll();
    }
    
    
    // ========== METHOD 2: GET USER BY ID ==========
    
    /**
     * Retrieves a specific user by their ID
     * 
     * Use case: Viewing profile details of a specific user
     * 
     * @param userId - The ID of the user to retrieve
     * @return User object if found
     * @throws ResourceNotFoundException if user doesn't exist
     */
    public User getUserById(Integer userId) {
        // findById() returns Optional<User> - might be empty if user doesn't exist
        return userRepo.findById(userId)
                // orElseThrow() - if user not found, throw custom exception
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    
    // ========== METHOD 3: CREATE NEW USER (ADMIN ONLY) ==========
    
    /**
     * Creates a new user account in the system
     * 
     * Workflow:
     * 1. Validate email doesn't already exist
     * 2. Create user with encrypted password
     * 3. Link to manager if specified
     * 4. Send welcome notification to new user
     * 5. Log the action in audit trail
     * 
     * @param req - Contains user details (name, email, password, role, etc.)
     * @param adminId - ID of the admin creating this user (for audit logging)
     * @return The newly created User object
     * @throws BadRequestException if email already exists
     * @throws ResourceNotFoundException if specified manager doesn't exist
     */
    public User createUser(CreateUserRequest req, Integer adminId) {
        
        // STEP 1: Check if email already exists in the database
        // We don't want duplicate emails in the system
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        
        // STEP 2: Create a new User entity (object that maps to database table)
        User user = new User();
        user.setName(req.getName());  // Set user's full name
        user.setEmail(req.getEmail());  // Set user's email
        
        // IMPORTANT: Never store plain text passwords!
        // pwdEncoder.encode() converts "password123" into something like:
        // "$2a$10$EixZaYVK1fsbw1ZfbX3OXe..." (irreversible encryption)
        user.setPasswordHash(pwdEncoder.encode(req.getPassword()));
        
        user.setRole(req.getRole());  // Set role: ADMIN, MANAGER, or EMPLOYEE
        user.setDepartment(req.getDept());  // Set department: Engineering, HR, etc.
        user.setStatus(req.getStatus());  // Set status: ACTIVE or INACTIVE
        
        
        // STEP 3: Set manager relationship (if manager ID is provided)
        // Not all users have managers (e.g., CEO doesn't have a manager)
        if (req.getMgrId() != null) {
            // First, verify the manager exists in database
            User mgr = userRepo.findById(req.getMgrId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            user.setManager(mgr);  // Create foreign key relationship
        }
        
        
        // STEP 4: Save user to database
        // This executes: INSERT INTO users (...) VALUES (...)
        User savedUser = userRepo.save(user);
        // savedUser now has an auto-generated userId from the database
        
        
        // STEP 5: Create a welcome notification for the new user
        Notification notif = new Notification();
        notif.setUser(savedUser);  // Link notification to the new user
        notif.setType(NotificationType.ACCOUNT_CREATED);  // Type of notification
        notif.setMessage("Your account has been created. You can now log in.");
        notif.setStatus(NotificationStatus.UNREAD);  // New notification is unread
        notifRepo.save(notif);  // Save notification to database
        
        
        // STEP 6: Create audit log for tracking who did what
        // Audit logs help with security and compliance
        User admin = userRepo.findById(adminId).orElse(null);  // Get admin user
        
        AuditLog log = new AuditLog();
        log.setUser(admin);  // Who performed the action
        log.setAction("USER_CREATED");  // What action was performed
        log.setDetails("Created user: " + savedUser.getName() + " (" + savedUser.getRole() + ")");
        log.setRelatedEntityType("User");  // Which type of entity was affected
        log.setRelatedEntityId(savedUser.getUserId());  // Which specific entity was affected
        log.setStatus("SUCCESS");  // Was the action successful?
        log.setTimestamp(LocalDateTime.now());  // When did this happen?
        auditRepo.save(log);  // Save audit log to database
        
        
        // STEP 7: Return the created user
        return savedUser;
    }
    
    
    // ========== METHOD 4: GET TEAM MEMBERS FOR A MANAGER ==========
    
    /**
     * Retrieves all employees who report to a specific manager
     * 
     * Use case: Manager viewing their team on the dashboard
     * 
     * @param mgrId - The ID of the manager
     * @return List of User objects who report to this manager
     */
    public List<User> getTeamMembers(Integer mgrId) {
        // This uses a custom query method defined in UserRepository
        // It finds all users where manager_id = mgrId
        // SQL equivalent: SELECT * FROM users WHERE manager_id = ?
        return userRepo.findByManager_UserId(mgrId);
    }
    
    
    // ========== METHOD 5: UPDATE USER (ADMIN ONLY) ==========
    
    /**
     * Updates an existing user's information
     * 
     * Workflow:
     * 1. Find the user to update
     * 2. Update their information
     * 3. Save changes to database
     * 4. Log the action in audit trail
     * 
     * Note: This method does NOT update email or password
     * (Those would need separate methods for security reasons)
     * 
     * @param userId - ID of the user to update
     * @param req - New user details
     * @param adminId - ID of admin performing the update (for audit logging)
     * @return Updated User object
     * @throws ResourceNotFoundException if user or manager doesn't exist
     */
    public User updateUser(Integer userId, CreateUserRequest req, Integer adminId) {
        
        // STEP 1: Find the existing user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        
        // STEP 2: Update the user's fields with new values
        user.setName(req.getName());  // Update name
        user.setRole(req.getRole());  // Update role (e.g., promote EMPLOYEE to MANAGER)
        user.setDepartment(req.getDept());  // Update department (e.g., transfer to new team)
        user.setStatus(req.getStatus());  // Update status (e.g., set to INACTIVE)
        
        
        // STEP 3: Update manager if provided
        if (req.getMgrId() != null) {
            User mgr = userRepo.findById(req.getMgrId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            user.setManager(mgr);  // Change the user's manager
        }
        
        
        // STEP 4: Save updated user to database
        // This executes: UPDATE users SET name=?, role=?, ... WHERE user_id=?
        User updated = userRepo.save(user);
        
        
        // STEP 5: Create audit log for the update action
        User admin = userRepo.findById(adminId).orElse(null);
        
        AuditLog log = new AuditLog();
        log.setUser(admin);  // Who performed the update
        log.setAction("USER_UPDATED");  // What action was performed
        log.setDetails("Updated user: " + updated.getName());
        log.setRelatedEntityType("User");  // Which type of entity was affected
        log.setRelatedEntityId(updated.getUserId());  // Which specific user was updated
        log.setStatus("SUCCESS");  // Was the action successful?
        log.setTimestamp(LocalDateTime.now());  // When did this happen?
        auditRepo.save(log);  // Save audit log to database
        
        
        // STEP 6: Return the updated user
        return updated;
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
 * AuthService: Handles user authentication and authorization
 * 
 * This service manages:
 * - User login (verifying credentials and generating JWT tokens)
 * - User logout (audit logging)
 * - Password changes
 * 
 * Security is critical here - we need to:
 * 1. Never expose why login failed (don't say "password wrong" vs "email wrong")
 * 2. Always encrypt passwords
 * 3. Generate secure JWT tokens for authenticated sessions
 * 4. Log all authentication events for security audits
 */
@Service  // Marks this as a Spring service component
public class AuthService {
    
    // ========== DEPENDENCIES (Auto-injected by Spring) ==========
    
    @Autowired
    private UserRepository userRepo;  // To fetch user data from database
    
    @Autowired
    private PasswordEncoder pwdEncoder;  // To verify encrypted passwords
    
    @Autowired
    private JwtUtil jwtUtil;  // To generate JWT tokens for authenticated sessions
    
    @Autowired
    private AuditLogRepository auditRepo;  // To log authentication events
    
    
    // ========== METHOD 1: USER LOGIN ==========
    
    /**
     * Authenticates a user and generates a JWT token for session management
     * 
     * Login Flow:
     * 1. Find user by email
     * 2. Verify password matches
     * 3. Check if user account is active
     * 4. Generate JWT token
     * 5. Log the successful login
     * 6. Return user details + token
     * 
     * @param req - Contains email and password from login form
     * @return LoginResponse with JWT token and user details
     * @throws UnauthorizedException if credentials are invalid or account is inactive
     */
    public LoginResponse login(LoginRequest req) {
        
        // STEP 1: Find user by email in database
        // SECURITY NOTE: We use generic "Invalid email or password" message
        // We don't say "email not found" vs "password wrong" to prevent
        // attackers from discovering valid emails in our system
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        
        // STEP 2: Verify password matches
        // pwdEncoder.matches() compares:
        // - Plain text password from login form: "password123"
        // - Encrypted password from database: "$2a$10$EixZaYVK..."
        // It returns true if they match, false otherwise
        if (!pwdEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            // Again, generic message for security
            throw new UnauthorizedException("Invalid email or password");
        }
        
        
        // STEP 3: Check if user account is active
        // Even if credentials are correct, inactive users can't log in
        // (e.g., suspended employees, accounts pending activation)
        if (user.getStatus().name().equals("INACTIVE")) {
            throw new UnauthorizedException("Account is inactive");
        }
        
        
        // STEP 4: Generate JWT token
        // JWT (JSON Web Token) is like a secure "session ticket"
        // It contains encrypted user info that the client sends with each request
        // Server can verify it without checking database every time
        String token = jwtUtil.generateToken(
            user.getEmail(),           // User's email
            user.getUserId(),          // User's ID
            user.getRole().name()      // User's role (ADMIN, MANAGER, EMPLOYEE)
        );
        // Token looks like: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        
        
        // STEP 5: Create audit log for security tracking
        // Important for compliance and detecting suspicious login patterns
        AuditLog log = new AuditLog();
        log.setUser(user);  // Who logged in
        log.setAction("LOGIN");  // What action occurred
        log.setDetails("User logged in successfully");  // Additional details
        log.setStatus("SUCCESS");  // Was login successful?
        log.setTimestamp(LocalDateTime.now());  // When did this happen?
        auditRepo.save(log);  // Save to database
        
        
        // STEP 6: Return login response with token and user info
        // Client will store this token and send it with every subsequent request
        return new LoginResponse(
            token,                 // JWT token for authentication
            user.getUserId(),      // User's ID
            user.getName(),        // User's name
            user.getEmail(),       // User's email
            user.getRole(),        // User's role
            user.getDepartment()   // User's department
        );
    }
    
    
    // ========== METHOD 2: USER LOGOUT ==========
    
    /**
     * Handles user logout
     * 
     * Note: With JWT tokens, logout is mostly client-side
     * (client deletes the token). On server-side, we just log the event.
     * 
     * In production systems, you might also:
     * - Add token to a blacklist
     * - Store active sessions in Redis
     * - Implement token refresh mechanism
     * 
     * @param userId - ID of the user logging out
     */
    public void logout(Integer userId) {
        
        // Find the user (if they exist)
        User user = userRepo.findById(userId).orElse(null);
        
        if (user != null) {
            // Create audit log for logout event
            // Helps track user session durations and activity patterns
            AuditLog log = new AuditLog();
            log.setUser(user);  // Who logged out
            log.setAction("LOGOUT");  // What action occurred
            log.setDetails("User logged out");  // Additional details
            log.setStatus("SUCCESS");  // Was logout successful?
            log.setTimestamp(LocalDateTime.now());  // When did this happen?
            auditRepo.save(log);  // Save to database
        }
    }
    
    
    // ========== METHOD 3: CHANGE PASSWORD ==========
    
    /**
     * Allows user to change their password
     * 
     * Security Flow:
     * 1. Verify user exists
     * 2. Verify current password is correct (prevent unauthorized password changes)
     * 3. Encrypt new password
     * 4. Save to database
     * 5. Log the password change event
     * 
     * @param userId - ID of the user changing password
     * @param oldPwd - Current password (for verification)
     * @param newPwd - New password to set
     * @throws UnauthorizedException if user not found or old password is wrong
     */
    public void changePassword(Integer userId, String oldPwd, String newPwd) {
        
        // STEP 1: Find the user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        
        // STEP 2: Verify old password is correct
        // SECURITY: This prevents someone who stole the JWT token from
        // changing the password without knowing the current password
        if (!pwdEncoder.matches(oldPwd, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        
        // STEP 3: Update password with encrypted new password
        // Never store plain text passwords!
        user.setPasswordHash(pwdEncoder.encode(newPwd));
        userRepo.save(user);  // Save to database
        
        
        // STEP 4: Create audit log for password change
        // Important for security monitoring and compliance
        AuditLog log = new AuditLog();
        log.setUser(user);  // Who changed their password
        log.setAction("PASSWORD_CHANGED");  // What action occurred
        log.setDetails("User changed password");  // Additional details
        log.setStatus("SUCCESS");  // Was the change successful?
        log.setTimestamp(LocalDateTime.now());  // When did this happen?
        auditRepo.save(log);  // Save to database
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
 * UserRepository: Data Access Layer for User entity
 * 
 * This interface extends JpaRepository, which provides:
 * - Basic CRUD operations (Create, Read, Update, Delete)
 * - Advanced querying capabilities
 * - Pagination and sorting
 * 
 * You don't need to write implementation code - Spring Data JPA
 * automatically generates the SQL queries based on method names!
 * 
 * For example:
 * - findByEmail() → SELECT * FROM users WHERE email = ?
 * - findByRole() → SELECT * FROM users WHERE role = ?
 * - findByManager_UserId() → SELECT * FROM users WHERE manager_id = ?
 */
@Repository  // Marks this as a Spring Data repository component
public interface UserRepository extends JpaRepository<User, Integer> {
    // JpaRepository<User, Integer> means:
    // - User: The entity this repository manages
    // - Integer: The type of the primary key (userId)
    
    
    // ========== CUSTOM QUERY METHOD 1: FIND BY EMAIL ==========
    
    /**
     * Finds a user by their email address
     * 
     * Spring Data JPA translates this method name into:
     * SELECT * FROM users WHERE email = ?
     * 
     * Use case: Login (checking if email exists and getting user details)
     * 
     * @param email - The email address to search for
     * @return Optional<User> - Contains User if found, empty if not found
     *                          Using Optional prevents NullPointerException
     */
    Optional<User> findByEmail(String email);
    // Method naming convention:
    // - "find" = SELECT query
    // - "By" = WHERE clause
    // - "Email" = field name in User entity
    
    
    // ========== CUSTOM QUERY METHOD 2: FIND BY ROLE ==========
    
    /**
     * Finds all users with a specific role
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE role = ?
     * 
     * Use case: 
     * - Admin viewing all managers
     * - Getting all employees for bulk operations
     * 
     * @param role - The role to filter by (ADMIN, MANAGER, or EMPLOYEE)
     * @return List of users with that role
     */
    List<User> findByRole(UserRole role);
    // Returns a List because multiple users can have the same role
    
    
    // ========== CUSTOM QUERY METHOD 3: FIND BY DEPARTMENT ==========
    
    /**
     * Finds all users in a specific department
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE department = ?
     * 
     * Use case:
     * - Viewing all employees in Engineering department
     * - Department-wise reports and analytics
     * 
     * @param department - The department name (e.g., "Engineering", "HR")
     * @return List of users in that department
     */
    List<User> findByDepartment(String department);
    
    
    // ========== CUSTOM QUERY METHOD 4: FIND BY STATUS ==========
    
    /**
     * Finds all users with a specific status
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE status = ?
     * 
     * Use case:
     * - Finding all inactive users for cleanup
     * - Getting active users for system reports
     * 
     * @param status - The status to filter by (ACTIVE or INACTIVE)
     * @return List of users with that status
     */
    List<User> findByStatus(UserStatus status);
    
    
    // ========== CUSTOM QUERY METHOD 5: FIND TEAM MEMBERS BY MANAGER ==========
    
    /**
     * Finds all users who report to a specific manager
     * 
     * This uses a special naming convention for navigating relationships:
     * - "Manager" = the field name in User entity
     * - "_" = navigate into the Manager object
     * - "UserId" = the field inside Manager object
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE manager_id = ?
     * 
     * Use case:
     * - Manager viewing their team members
     * - Calculating team size for a manager
     * 
     * @param managerId - The ID of the manager
     * @return List of users who report to this manager
     */
    List<User> findByManager_UserId(Integer managerId);
    // The underscore "_" is Spring Data JPA's way of navigating
    // through entity relationships in method names
    
    
    /* 
     * ========== INHERITED METHODS FROM JpaRepository ==========
     * 
     * You get these methods for FREE without writing any code:
     * 
     * 1. save(User user) - Insert or update a user
     * 2. findById(Integer id) - Find user by ID
     * 3. findAll() - Get all users
     * 4. deleteById(Integer id) - Delete user by ID
     * 5. count() - Count total users
     * 6. existsById(Integer id) - Check if user exists
     * 
     * And many more!
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
 * UserController: REST API endpoints for user management
 * 
 * This controller handles HTTP requests related to users:
 * - GET requests to retrieve user data
 * - POST requests to create new users
 * - PUT requests to update existing users
 * 
 * The controller is the ENTRY POINT for HTTP requests.
 * Flow: HTTP Request → Controller → Service → Repository → Database
 * 
 * Base URL: /api/v1/users
 * All endpoints in this controller start with this base path
 */
@RestController  // Marks this as a REST API controller (returns JSON, not HTML views)
@RequestMapping("/api/v1/users")  // Base path for all endpoints in this controller
public class UserController {
    
    // ========== DEPENDENCY INJECTION ==========
    
    @Autowired  // Spring automatically injects the UserService instance
    private UserService userSvc;  // Handles business logic for user operations
    
    
    // ========== ENDPOINT 1: GET ALL USERS (ADMIN/MANAGER ONLY) ==========
    
    /**
     * GET /api/v1/users
     * 
     * Retrieves a list of all users in the system
     * 
     * Access: ADMIN and MANAGER roles only
     * Use case: Admin dashboard showing all users, Manager viewing employee list
     * 
     * Example Request:
     * GET /api/v1/users
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * Example Response:
     * {
     *   "success": true,
     *   "message": "Users retrieved",
     *   "data": [
     *     {
     *       "userId": 1,
     *       "name": "John Doe",
     *       "email": "john@example.com",
     *       "role": "EMPLOYEE",
     *       ...
     *     },
     *     ...
     *   ]
     * }
     * 
     * @return ApiResponse containing list of all users
     */
    @GetMapping  // Maps to GET /api/v1/users
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")  // Only ADMIN and MANAGER can access
    public ApiResponse<List<User>> getAllUsers() {
        // Call service layer to fetch users from database
        List<User> users = userSvc.getAllUsers();
        
        // Wrap result in standardized ApiResponse format
        return ApiResponse.success("Users retrieved", users);
    }
    
    
    // ========== ENDPOINT 2: GET USER BY ID ==========
    
    /**
     * GET /api/v1/users/{userId}
     * 
     * Retrieves details of a specific user by their ID
     * 
     * Access: All authenticated users (but typically used for viewing own profile
     *         or manager viewing team member's profile)
     * 
     * Example Request:
     * GET /api/v1/users/5
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * Example Response:
     * {
     *   "success": true,
     *   "message": "User retrieved",
     *   "data": {
     *     "userId": 5,
     *     "name": "Jane Smith",
     *     "email": "jane@example.com",
     *     "role": "MANAGER",
     *     "department": "Engineering",
     *     ...
     *   }
     * }
     * 
     * @param userId - The ID from the URL path (e.g., 5 in /users/5)
     * @return ApiResponse containing the user details
     */
    @GetMapping("/{userId}")  // Maps to GET /api/v1/users/{userId}
    // @PathVariable extracts {userId} from URL and assigns it to the parameter
    public ApiResponse<User> getUserById(@PathVariable Integer userId) {
        // Call service layer to fetch user by ID
        User user = userSvc.getUserById(userId);
        
        // Wrap result in standardized ApiResponse format
        return ApiResponse.success("User retrieved", user);
    }
    
    
    // ========== ENDPOINT 3: CREATE NEW USER (ADMIN ONLY) ==========
    
    /**
     * POST /api/v1/users
     * 
     * Creates a new user account in the system
     * 
     * Access: ADMIN role only
     * Use case: Admin onboarding a new employee
     * 
     * Example Request:
     * POST /api/v1/users
     * Headers: 
     *   Authorization: Bearer <JWT_TOKEN>
     *   Content-Type: application/json
     * Body:
     * {
     *   "name": "Alice Johnson",
     *   "email": "alice@example.com",
     *   "password": "SecurePass123",
     *   "role": "EMPLOYEE",
     *   "dept": "Engineering",
     *   "status": "ACTIVE",
     *   "mgrId": 3
     * }
     * 
     * Example Response:
     * {
     *   "success": true,
     *   "message": "User created",
     *   "data": {
     *     "userId": 10,
     *     "name": "Alice Johnson",
     *     "email": "alice@example.com",
     *     ...
     *   }
     * }
     * 
     * @param req - Request body containing user details (validated by @Valid)
     * @param httpReq - The HTTP request object (contains JWT token info)
     * @return ApiResponse containing the newly created user
     */
    @PostMapping  // Maps to POST /api/v1/users
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can create users
    public ApiResponse<User> createUser(
            @Valid @RequestBody CreateUserRequest req,  // Parse and validate JSON request body
            HttpServletRequest httpReq  // To extract userId from JWT token
    ) {
        // Extract admin's userId from the HTTP request attributes
        // (This was set by our JWT filter after validating the token)
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        
        // Call service layer to create the user
        User user = userSvc.createUser(req, adminId);
        
        // Wrap result in standardized ApiResponse format
        return ApiResponse.success("User created", user);
    }
    
    
    // ========== ENDPOINT 4: UPDATE USER (ADMIN ONLY) ==========
    
    /**
     * PUT /api/v1/users/{userId}
     * 
     * Updates an existing user's information
     * 
     * Access: ADMIN role only
     * Use case: Admin updating user's role, department, or status
     * 
     * Example Request:
     * PUT /api/v1/users/10
     * Headers:
     *   Authorization: Bearer <JWT_TOKEN>
     *   Content-Type: application/json
     * Body:
     * {
     *   "name": "Alice Johnson",
     *   "role": "MANAGER",  // Promoted to manager
     *   "dept": "Engineering",
     *   "status": "ACTIVE",
     *   "mgrId": 3
     * }
     * 
     * Example Response:
     * {
     *   "success": true,
     *   "message": "User updated",
     *   "data": {
     *     "userId": 10,
     *     "name": "Alice Johnson",
     *     "role": "MANAGER",
     *     ...
     *   }
     * }
     * 
     * @param userId - ID of the user to update (from URL path)
     * @param req - Request body containing updated user details
     * @param httpReq - The HTTP request object (contains JWT token info)
     * @return ApiResponse containing the updated user
     */
    @PutMapping("/{userId}")  // Maps to PUT /api/v1/users/{userId}
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can update users
    public ApiResponse<User> updateUser(
            @PathVariable Integer userId,  // Extract userId from URL path
            @Valid @RequestBody CreateUserRequest req,  // Parse and validate JSON body
            HttpServletRequest httpReq  // To extract userId from JWT token
    ) {
        // Extract admin's userId from the HTTP request attributes
        Integer adminId = (Integer) httpReq.getAttribute("userId");
        
        // Call service layer to update the user
        User user = userSvc.updateUser(userId, req, adminId);
        
        // Wrap result in standardized ApiResponse format
        return ApiResponse.success("User updated", user);
    }
    
    
    // ========== ENDPOINT 5: GET TEAM MEMBERS (MANAGER ONLY) ==========
    
    /**
     * GET /api/v1/users/{userId}/team
     * 
     * Retrieves all team members who report to a specific manager
     * 
     * Access: MANAGER role only
     * Use case: Manager viewing their direct reports
     * 
     * Example Request:
     * GET /api/v1/users/3/team
     * Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * Example Response:
     * {
     *   "success": true,
     *   "message": "Team members retrieved",
     *   "data": [
     *     {
     *       "userId": 10,
     *       "name": "Alice Johnson",
     *       "email": "alice@example.com",
     *       "managerId": 3,
     *       ...
     *     },
     *     {
     *       "userId": 11,
     *       "name": "Bob Williams",
     *       ...
     *     }
     *   ]
     * }
     * 
     * @param userId - The manager's ID (from URL path)
     * @return ApiResponse containing list of team members
     */
    @GetMapping("/{userId}/team")  // Maps to GET /api/v1/users/{userId}/team
    @PreAuthorize("hasRole('MANAGER')")  // Only MANAGER can access
    public ApiResponse<List<User>> getTeam(@PathVariable Integer userId) {
        // Call service layer to fetch team members
        List<User> team = userSvc.getTeamMembers(userId);
        
        // Wrap result in standardized ApiResponse format
        return ApiResponse.success("Team members retrieved", team);
    }
}

/*
 * ========== EXPLANATION OF ANNOTATIONS ==========
 * 
 * @RestController:
 * - Combines @Controller + @ResponseBody
 * - Automatically converts return values to JSON
 * - Tells Spring this class handles HTTP requests
 * 
 * @RequestMapping("/api/v1/users"):
 * - Sets base URL path for all endpoints in this controller
 * - All methods inherit this path
 * 
 * @GetMapping, @PostMapping, @PutMapping:
 * - Shortcuts for @RequestMapping(method = RequestMethod.GET/POST/PUT)
 * - Maps HTTP methods to Java methods
 * 
 * @PathVariable:
 * - Extracts value from URL path
 * - Example: /users/5 → userId = 5
 * 
 * @RequestBody:
 * - Tells Spring to parse JSON request body into Java object
 * - Example: {"name": "John", ...} → CreateUserRequest object
 * 
 * @Valid:
 * - Triggers validation annotations in DTO class
 * - Example: @NotNull, @Email, @Size annotations
 * 
 * @PreAuthorize:
 * - Security check BEFORE method executes
 * - hasRole('ADMIN') → checks if logged-in user has ADMIN role
 * - hasAnyRole('ADMIN', 'MANAGER') → checks for ADMIN OR MANAGER
 * 
 * @Autowired:
 * - Tells Spring to inject dependencies automatically
 * - No need to manually create UserService instance
 */
```

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
 * AuthController: REST API endpoints for authentication operations
 * 
 * This controller handles:
 * - User login (POST /api/v1/auth/login)
 * - User logout (POST /api/v1/auth/logout)
 * - Password change (PUT /api/v1/auth/change-password)
 * 
 * These are PUBLIC endpoints (except logout and change-password)
 * - Login doesn't require authentication (obviously, since you're trying to get authenticated)
 * - Logout and change-password require valid JWT token
 * 
 * Base URL: /api/v1/auth
 */
@RestController  // Marks this as a REST API controller that returns JSON
@RequestMapping("/api/v1/auth")  // Base path for all authentication endpoints
public class AuthController {
    
    // ========== DEPENDENCY INJECTION ==========
    
    @Autowired  // Spring automatically injects the AuthService instance
    private AuthService authSvc;  // Handles authentication business logic
    
    
    // ========== ENDPOINT 1: LOGIN ==========
    
    /**
     * POST /api/v1/auth/login
     * 
     * Authenticates a user and returns a JWT token for accessing protected endpoints
     * 
     * Access: PUBLIC (no authentication required - this IS the authentication endpoint)
     * 
     * Login Flow:
     * 1. User submits email + password
     * 2. Server verifies credentials
     * 3. Server generates JWT token
     * 4. Client stores token (usually in localStorage or cookies)
     * 5. Client includes token in Authorization header for subsequent requests
     * 
     * Example Request:
     * POST /api/v1/auth/login
     * Headers:
     *   Content-Type: application/json
     * Body:
     * {
     *   "email": "john@example.com",
     *   "password": "SecurePass123"
     * }
     * 
     * Example Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "userId": 5,
     *     "name": "John Doe",
     *     "email": "john@example.com",
     *     "role": "EMPLOYEE",
     *     "department": "Engineering"
     *   }
     * }
     * 
     * Example Error Response (401 Unauthorized):
     * {
     *   "success": false,
     *   "message": "Invalid email or password"
     * }
     * 
     * @param req - Contains email and password (validated by @Valid)
     * @return ApiResponse with JWT token and user details
     */
    @PostMapping("/login")  // Maps to POST /api/v1/auth/login
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        // @Valid triggers validation in LoginRequest class
        // @RequestBody tells Spring to parse JSON body into LoginRequest object
        
        // Call service layer to authenticate user and generate token
        LoginResponse resp = authSvc.login(req);
        
        // Wrap result in standardized ApiResponse format
        return ApiResponse.success("Login successful", resp);
    }
    
    
    // ========== ENDPOINT 2: LOGOUT ==========
    
    /**
     * POST /api/v1/auth/logout
     * 
     * Logs out the current user (mainly for audit trail purposes)
     * 
     * Access: Requires valid JWT token
     * 
     * Important: With JWT tokens, logout is primarily CLIENT-SIDE
     * - Client deletes the token from storage
     * - Token becomes unusable once client removes it
     * - Server-side logout is mainly for logging the event
     * 
     * In more advanced systems, you might:
     * - Add token to a blacklist in Redis
     * - Implement token revocation
     * - Track active sessions
     * 
     * Example Request:
     * POST /api/v1/auth/logout
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * 
     * Example Response:
     * {
     *   "success": true,
     *   "message": "Logout successful"
     * }
     * 
     * @param req - HTTP request object containing JWT token info
     * @return ApiResponse confirming logout
     */
    @PostMapping("/logout")  // Maps to POST /api/v1/auth/logout
    public ApiResponse<Void> logout(HttpServletRequest req) {
        // Extract userId from request attributes
        // (Set by JWT filter after validating the token)
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Call service layer to log the logout event
        authSvc.logout(userId);
        
        // Return success response (no data needed, just confirmation)
        return ApiResponse.success("Logout successful");
    }
    
    
    // ========== ENDPOINT 3: CHANGE PASSWORD ==========
    
    /**
     * PUT /api/v1/auth/change-password
     * 
     * Allows authenticated user to change their password
     * 
     * Access: Requires valid JWT token
     * Security: Requires current password for verification
     * 
     * Use case: User wants to update their password for security reasons
     * 
     * Example Request:
     * PUT /api/v1/auth/change-password
     * Headers:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *   Content-Type: application/json
     * Body:
     * {
     *   "oldPassword": "OldPass123",
     *   "newPassword": "NewSecurePass456"
     * }
     * 
     * Example Success Response:
     * {
     *   "success": true,
     *   "message": "Password changed successfully"
     * }
     * 
     * Example Error Response (if old password is wrong):
     * {
     *   "success": false,
     *   "message": "Current password is incorrect"
     * }
     * 
     * @param body - Map containing oldPassword and newPassword
     * @param req - HTTP request object containing JWT token info
     * @return ApiResponse confirming password change
     */
    @PutMapping("/change-password")  // Maps to PUT /api/v1/auth/change-password
    public ApiResponse<Void> changePassword(
            @RequestBody Map<String, String> body,  // Parse JSON body as a Map
            HttpServletRequest req  // To extract userId from JWT token
    ) {
        // Extract userId from request attributes
        // (Set by JWT filter after validating the token)
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Extract old and new passwords from request body
        String oldPwd = body.get("oldPassword");  // Get "oldPassword" field from JSON
        String newPwd = body.get("newPassword");  // Get "newPassword" field from JSON
        
        // Call service layer to change password
        authSvc.changePassword(userId, oldPwd, newPwd);
        
        // Return success response (no data needed, just confirmation)
        return ApiResponse.success("Password changed successfully");
    }
}

/*
 * ========== NOTES FOR TRAINEES ==========
 * 
 * 1. Why use Map<String, String> for change-password?
 *    - For simple requests with just 2 fields, Map is quick and easy
 *    - For complex requests, create a dedicated DTO class (like LoginRequest)
 * 
 * 2. Why is login a POST request, not GET?
 *    - GET requests should be idempotent (same result every time)
 *    - GET requests appear in browser history and server logs
 *    - POST is more secure for sensitive data like passwords
 * 
 * 3. Why does logout need authentication?
 *    - To know WHICH user is logging out (for audit trail)
 *    - Prevents anonymous users from messing with logout logs
 * 
 * 4. HttpServletRequest vs Custom DTO?
 *    - HttpServletRequest gives access to raw HTTP request data
 *    - We use it to extract userId set by our JWT filter
 *    - Custom DTOs (@RequestBody) are for structured data in request body
 * 
 * 5. ApiResponse<Void> vs ApiResponse<LoginResponse>?
 *    - <Void> means no data in response (just success/error message)
 *    - <LoginResponse> means response includes data (token, user info, etc.)
 */
```

These commented versions should help you understand the code flow and purpose of each component!
