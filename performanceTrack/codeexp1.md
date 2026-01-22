# 🚀 Complete Beginner's Guide to PerformanceTrack Backend Code

I'll explain **every single line** of code in extreme detail. Let's break this down piece by piece!

---

## 📁 PROJECT STRUCTURE OVERVIEW

```
PerformanceTrack/
├── config/          → Security & app configuration
├── controllers/     → Handle HTTP requests (API endpoints)
├── dto/            → Data Transfer Objects (request/response formats)
├── entity/         → Database tables (already explained)
├── exception/      → Error handling
├── repository/     → Database queries
├── security/       → Authentication filter
├── service/        → Business logic
├── util/           → Helper utilities (JWT)
└── application/    → Main app starter
```

---

## 🔐 1. SECURITY CONFIGURATION

### **SecurityConfig.java**

```java
package com.project.performanceTrack.config;
```
- **What it does**: Declares this file belongs to the `config` package
- **Simple terms**: Like putting a file in a folder called "config"

```java
import com.project.performanceTrack.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
```
- **What it does**: Imports (brings in) classes we need from other packages
- **Simple terms**: Like saying "I need these tools from other toolboxes"

```java
@Configuration
```
- **What it does**: Tells Spring "this class contains configuration settings"
- **Simple terms**: This class sets up rules for the entire app

```java
@EnableWebSecurity
```
- **What it does**: Activates Spring Security features
- **Simple terms**: Turns on the security guard system

```java
@EnableMethodSecurity
```
- **What it does**: Allows method-level security annotations like `@PreAuthorize`
- **Simple terms**: Lets you add security checks directly on individual functions

```java
public class SecurityConfig {
```
- **What it does**: Declares a public class named SecurityConfig
- **Simple terms**: Creates a blueprint for security settings

```java
@Autowired
private JwtAuthFilter jwtAuthFilter;
```
- **@Autowired**: Tells Spring to automatically inject (provide) an instance of JwtAuthFilter
- **private**: Only this class can access this variable
- **JwtAuthFilter**: Custom filter that checks JWT tokens
- **Simple terms**: "Spring, please give me the JWT authentication filter"

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- **@Bean**: Tells Spring to create and manage this object
- **public**: Anyone can call this method
- **PasswordEncoder**: Return type - interface for password encoding
- **passwordEncoder()**: Method name
- **new BCryptPasswordEncoder()**: Creates a BCrypt password hasher
- **Simple terms**: "Create a password hasher that uses BCrypt algorithm (very secure)"
- **Why BCrypt?**: Industry-standard, slow (good for passwords), salted automatically

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
```
- **@Bean**: Spring manages this
- **SecurityFilterChain**: Return type - chain of security filters
- **securityFilterChain**: Method name
- **HttpSecurity http**: Parameter - object to configure HTTP security
- **throws Exception**: This method can throw errors
- **Simple terms**: "Here's how to check every incoming HTTP request"

```java
http
    .csrf(csrf -> csrf.disable())
```
- **http**: The HttpSecurity object
- **csrf()**: Configure CSRF (Cross-Site Request Forgery) protection
- **csrf -> csrf.disable()**: Lambda expression - disable CSRF
- **Why disable?**: We're using JWT tokens (not cookies), so CSRF isn't needed
- **Simple terms**: "Turn off CSRF protection because we're using token-based auth"

```java
.authorizeHttpRequests(auth -> auth
```
- **authorizeHttpRequests()**: Configure URL access rules
- **auth ->**: Lambda parameter representing the authorization config
- **Simple terms**: "Here are the rules for who can access what URLs"

```java
.requestMatchers("/api/v1/auth/**").permitAll()
```
- **requestMatchers()**: Match URLs by pattern
- **"/api/v1/auth/**"**: Pattern - any URL starting with /api/v1/auth/
- **`**`**: Wildcard - matches any path after /auth/
- **permitAll()**: Allow everyone (no authentication required)
- **Simple terms**: "Anyone can access login/logout endpoints without logging in"

```java
.requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
```
- **hasAnyRole()**: Check if user has at least one of these roles
- **"ADMIN", "MANAGER"**: Allowed roles
- **Simple terms**: "Only ADMINs and MANAGERs can access user management endpoints"

```java
.requestMatchers("/api/v1/review-cycles/**").hasRole("ADMIN")
```
- **hasRole()**: Check for a specific role
- **"ADMIN"**: Only admins allowed
- **Simple terms**: "Only ADMINs can manage review cycles"

```java
.requestMatchers("/api/v1/audit-logs/**").hasRole("ADMIN")
```
- **Simple terms**: "Only ADMINs can view audit logs"

```java
.anyRequest().authenticated()
```
- **anyRequest()**: All other requests (not matched above)
- **authenticated()**: User must be logged in
- **Simple terms**: "Everything else requires login"

```java
)
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```
- **sessionManagement()**: Configure session handling
- **SessionCreationPolicy.STATELESS**: Don't create HTTP sessions
- **Why stateless?**: JWT tokens are self-contained; server doesn't need to remember users
- **Simple terms**: "Don't use server-side sessions because JWT tokens handle authentication"

```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```
- **addFilterBefore()**: Insert our custom filter before another filter
- **jwtAuthFilter**: Our custom JWT validation filter
- **UsernamePasswordAuthenticationFilter.class**: Spring's default authentication filter
- **Simple terms**: "Before checking username/password, check the JWT token first"

```java
return http.build();
```
- **build()**: Finalize and create the SecurityFilterChain
- **return**: Send this chain back to Spring
- **Simple terms**: "Give Spring the complete security configuration"

---

## 🎮 2. CONTROLLERS (API Endpoints)

### **AuthController.java** - Login/Logout

```java
@RestController
```
- **What it does**: Marks this class as a REST API controller
- **Simple terms**: "This class handles HTTP requests and returns JSON responses"

```java
@RequestMapping("/api/v1/auth")
```
- **What it does**: All endpoints in this class start with /api/v1/auth
- **Simple terms**: "This controller handles /api/v1/auth/... URLs"

```java
public class AuthController {
```
- **Simple terms**: Controller for authentication (login/logout/password change)

```java
@Autowired
private AuthService authSvc;
```
- **Simple terms**: "Spring, inject the AuthService so I can use it"

```java
@PostMapping("/login")
```
- **@PostMapping**: This method handles POST requests
- **"/login"**: URL path (combined with @RequestMapping = /api/v1/auth/login)
- **Simple terms**: "When someone POSTs to /api/v1/auth/login, run this method"

```java
public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
```
- **public**: Anyone can call this
- **ApiResponse<LoginResponse>**: Return type - wrapper containing LoginResponse
- **login**: Method name
- **@Valid**: Validate the request body using validation annotations
- **@RequestBody**: Take data from HTTP request body (JSON) and convert to LoginRequest object
- **LoginRequest req**: Parameter - contains email and password
- **Simple terms**: "Take login data from request, validate it, process login"

```java
LoginResponse resp = authSvc.login(req);
```
- **LoginResponse resp**: Variable to store response
- **authSvc.login(req)**: Call the login method in AuthService
- **Simple terms**: "Ask AuthService to handle the actual login logic"

```java
return ApiResponse.success("Login successful", resp);
```
- **ApiResponse.success()**: Static method to create success response
- **"Login successful"**: Message
- **resp**: Data to include
- **Simple terms**: "Return a success response with the login data (token, user info)"

```java
@PostMapping("/logout")
public ApiResponse<Void> logout(HttpServletRequest req) {
```
- **ApiResponse<Void>**: Return type - no data, just success/error message
- **HttpServletRequest req**: Contains HTTP request info
- **Simple terms**: "Handle POST requests to /api/v1/auth/logout"

```java
Integer userId = (Integer) req.getAttribute("userId");
```
- **Integer userId**: Variable to store user ID
- **req.getAttribute("userId")**: Get userId from request attributes
- **Where did userId come from?**: JwtAuthFilter put it there after validating the token
- **(Integer)**: Cast (convert) Object to Integer
- **Simple terms**: "Get the logged-in user's ID from the request"

```java
authSvc.logout(userId);
```
- **Simple terms**: "Tell AuthService to log this user out"

```java
return ApiResponse.success("Logout successful");
```
- **Simple terms**: "Return success message (no data needed)"

```java
@PutMapping("/change-password")
```
- **@PutMapping**: Handle PUT requests (used for updates)
- **Simple terms**: "When someone PUTs to /api/v1/auth/change-password, run this"

```java
public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body,
                                        HttpServletRequest req) {
```
- **Map<String, String> body**: Key-value pairs from request body
- **Example body**: `{"oldPassword": "abc123", "newPassword": "xyz789"}`
- **Simple terms**: "Take password change data from request"

```java
Integer userId = (Integer) req.getAttribute("userId");
String oldPwd = body.get("oldPassword");
String newPwd = body.get("newPassword");
```
- **body.get("oldPassword")**: Extract old password from JSON
- **body.get("newPassword")**: Extract new password from JSON
- **Simple terms**: "Get user ID and extract old/new passwords"

```java
authSvc.changePassword(userId, oldPwd, newPwd);
```
- **Simple terms**: "Ask AuthService to change the password"

```java
return ApiResponse.success("Password changed successfully");
```

---

### **GoalController.java** - Goal Management

```java
@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {
```
- **Simple terms**: "Handles all /api/v1/goals/... endpoints"

```java
@Autowired
private GoalService goalSvc;
```
- **Simple terms**: "Inject GoalService to handle business logic"

```java
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<Goal> createGoal(@Valid @RequestBody CreateGoalRequest req,
                                    HttpServletRequest httpReq) {
```
- **@PostMapping**: POST to /api/v1/goals
- **@PreAuthorize("hasRole('EMPLOYEE')")**: Only employees can create goals
- **CreateGoalRequest req**: Request body with goal data
- **HttpServletRequest httpReq**: HTTP request (to get userId)
- **Simple terms**: "Employees can POST to /api/v1/goals to create a new goal"

```java
Integer empId = (Integer) httpReq.getAttribute("userId");
```
- **Simple terms**: "Get the employee's ID from the request (set by JWT filter)"

```java
Goal goal = goalSvc.createGoal(req, empId);
```
- **Simple terms**: "Ask GoalService to create the goal"

```java
return ApiResponse.success("Goal created", goal);
```
- **Simple terms**: "Return success with the created goal data"

```java
@GetMapping
public ApiResponse<List<Goal>> getGoals(HttpServletRequest httpReq,
                                        @RequestParam(required = false) Integer userId,
                                        @RequestParam(required = false) Integer mgrId) {
```
- **@GetMapping**: GET request to /api/v1/goals
- **@RequestParam**: Extract query parameters from URL
- **required = false**: These parameters are optional
- **Example URL**: /api/v1/goals?userId=5&mgrId=2
- **Simple terms**: "Get goals with optional filters (by user or manager)"

```java
String role = (String) httpReq.getAttribute("userRole");
Integer currentUserId = (Integer) httpReq.getAttribute("userId");
```
- **Simple terms**: "Get the current user's role and ID from request"

```java
List<Goal> goals;
if (role.equals("EMPLOYEE")) {
    goals = goalSvc.getGoalsByUser(currentUserId);
```
- **if**: Check condition
- **role.equals("EMPLOYEE")**: Is user an employee?
- **.equals()**: Compare strings (don't use == for strings!)
- **Simple terms**: "If employee, get only their own goals"

```java
} else if (role.equals("MANAGER")) {
    if (userId != null) {
        goals = goalSvc.getGoalsByUser(userId);
    } else {
        goals = goalSvc.getGoalsByManager(currentUserId);
    }
```
- **else if**: Check another condition
- **userId != null**: Is userId parameter provided?
- **Simple terms**: "If manager, get specific user's goals OR all team goals"

```java
} else {
    goals = userId != null ? goalSvc.getGoalsByUser(userId) : 
            mgrId != null ? goalSvc.getGoalsByManager(mgrId) : 
            goalSvc.getGoalsByUser(currentUserId);
}
```
- **Ternary operator**: condition ? value_if_true : value_if_false
- **Nested ternary**: Checking multiple conditions
- **Simple terms**: "Admin can filter by userId OR mgrId OR get their own"

```java
return ApiResponse.success("Goals retrieved", goals);
```

```java
@GetMapping("/{goalId}")
public ApiResponse<Goal> getGoalById(@PathVariable Integer goalId) {
```
- **@GetMapping("/{goalId}")**: URL path variable
- **Example**: /api/v1/goals/42 → goalId = 42
- **@PathVariable**: Extract {goalId} from URL
- **Simple terms**: "Get one specific goal by its ID from the URL"

```java
Goal goal = goalSvc.getGoalById(goalId);
return ApiResponse.success("Goal retrieved", goal);
```

```java
@PutMapping("/{goalId}/approve")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<Goal> approveGoal(@PathVariable Integer goalId,
                                     HttpServletRequest httpReq) {
```
- **@PutMapping**: PUT request (update operation)
- **"/{goalId}/approve"**: Example: /api/v1/goals/42/approve
- **@PreAuthorize("hasRole('MANAGER')")**: Only managers can approve
- **Simple terms**: "Manager approves a goal by its ID"

```java
Integer mgrId = (Integer) httpReq.getAttribute("userId");
Goal goal = goalSvc.approveGoal(goalId, mgrId);
return ApiResponse.success("Goal approved", goal);
```

```java
@PutMapping("/{goalId}/request-changes")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<Goal> requestChanges(@PathVariable Integer goalId,
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest httpReq) {
```
- **@RequestBody Map<String, String> body**: JSON with comments
- **Example body**: `{"comments": "Please add more details"}`
- **Simple terms**: "Manager requests changes to a goal with comments"

```java
Integer mgrId = (Integer) httpReq.getAttribute("userId");
String comments = body.get("comments");
Goal goal = goalSvc.requestChanges(goalId, mgrId, comments);
return ApiResponse.success("Change request sent", goal);
```
- **body.get("comments")**: Extract comments from JSON
- **Simple terms**: "Get manager ID and comments, ask service to request changes"

```java
@PostMapping("/{goalId}/submit-completion")
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<Goal> submitCompletion(@PathVariable Integer goalId,
                                          @Valid @RequestBody SubmitCompletionRequest req,
                                          HttpServletRequest httpReq) {
```
- **Simple terms**: "Employee submits goal completion with evidence"

```java
Integer empId = (Integer) httpReq.getAttribute("userId");
Goal goal = goalSvc.submitCompletion(goalId, req, empId);
return ApiResponse.success("Completion submitted", goal);
```

```java
@PostMapping("/{goalId}/approve-completion")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<Goal> approveCompletion(@PathVariable Integer goalId,
                                           @RequestBody ApproveCompletionRequest req,
                                           HttpServletRequest httpReq) {
```
- **Simple terms**: "Manager approves goal completion"

```java
@PostMapping("/{goalId}/request-additional-evidence")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<Goal> requestEvidence(@PathVariable Integer goalId,
                                         @RequestBody Map<String, String> body,
                                         HttpServletRequest httpReq) {
```
- **Simple terms**: "Manager requests more evidence for goal completion"

```java
Integer mgrId = (Integer) httpReq.getAttribute("userId");
String reason = body.get("reason");
Goal goal = goalSvc.requestAdditionalEvidence(goalId, mgrId, reason);
return ApiResponse.success("Additional evidence requested", goal);
```

```java
@PutMapping("/{goalId}")
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<Goal> updateGoal(@PathVariable Integer goalId,
                                    @Valid @RequestBody CreateGoalRequest req,
                                    HttpServletRequest httpReq) {
```
- **Simple terms**: "Employee updates goal (when changes were requested)"

```java
@DeleteMapping("/{goalId}")
public ApiResponse<Void> deleteGoal(@PathVariable Integer goalId,
                                    HttpServletRequest httpReq) {
```
- **@DeleteMapping**: DELETE HTTP method
- **Simple terms**: "Delete (soft delete) a goal"

```java
Integer userId = (Integer) httpReq.getAttribute("userId");
String role = (String) httpReq.getAttribute("userRole");
goalSvc.deleteGoal(goalId, userId, role);
return ApiResponse.success("Goal deleted");
```

```java
@PutMapping("/{goalId}/evidence/verify")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<Goal> verifyEvidence(@PathVariable Integer goalId,
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest httpReq) {
```
- **Simple terms**: "Manager verifies evidence link"

```java
Integer mgrId = (Integer) httpReq.getAttribute("userId");
String status = body.get("status");
String notes = body.get("notes");
Goal goal = goalSvc.verifyEvidence(goalId, mgrId, status, notes);
return ApiResponse.success("Evidence verified", goal);
```

```java
@PostMapping("/{goalId}/reject-completion")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<Goal> rejectCompletion(@PathVariable Integer goalId,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest httpReq) {
```
- **Simple terms**: "Manager rejects goal completion"

```java
@PostMapping("/{goalId}/progress")
@PreAuthorize("hasRole('EMPLOYEE')")
public ApiResponse<Void> addProgress(@PathVariable Integer goalId,
                                     @RequestBody Map<String, String> body,
                                     HttpServletRequest httpReq) {
```
- **Simple terms**: "Employee adds progress note to goal"

```java
Integer empId = (Integer) httpReq.getAttribute("userId");
String progressNote = body.get("note");
goalSvc.addProgressUpdate(goalId, empId, progressNote);
return ApiResponse.success("Progress added");
```

```java
@GetMapping("/{goalId}/progress")
public ApiResponse<String> getProgress(@PathVariable Integer goalId) {
```
- **ApiResponse<String>**: Returns progress notes as string
- **Simple terms**: "Get all progress updates for a goal"

```java
String progress = goalSvc.getProgressUpdates(goalId);
return ApiResponse.success("Progress retrieved", progress);
```

---

### **UserController.java** - User Management

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userSvc;
```

```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public ApiResponse<List<User>> getAllUsers() {
```
- **hasAnyRole('ADMIN', 'MANAGER')**: Either admin or manager can access
- **Simple terms**: "Get all users (admin/manager only)"

```java
List<User> users = userSvc.getAllUsers();
return ApiResponse.success("Users retrieved", users);
```

```java
@GetMapping("/{userId}")
public ApiResponse<User> getUserById(@PathVariable Integer userId) {
```
- **Simple terms**: "Get one user by ID"

```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<User> createUser(@Valid @RequestBody CreateUserRequest req, 
                                    HttpServletRequest httpReq) {
```
- **Simple terms**: "Admin creates a new user"

```java
Integer adminId = (Integer) httpReq.getAttribute("userId");
User user = userSvc.createUser(req, adminId);
return ApiResponse.success("User created", user);
```

```java
@PutMapping("/{userId}")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<User> updateUser(@PathVariable Integer userId,
                                    @Valid @RequestBody CreateUserRequest req,
                                    HttpServletRequest httpReq) {
```
- **Simple terms**: "Admin updates a user"

```java
@GetMapping("/{userId}/team")
@PreAuthorize("hasRole('MANAGER')")
public ApiResponse<List<User>> getTeam(@PathVariable Integer userId) {
```
- **Simple terms**: "Manager gets their team members"

```java
List<User> team = userSvc.getTeamMembers(userId);
return ApiResponse.success("Team members retrieved", team);
```

---

I'll continue with the remaining controllers, services, repositories, DTOs, and other components in the same extreme detail. Would you like me to continue with:

1. **Remaining Controllers** (ReviewCycle, PerformanceReview, Notification, etc.)
2. **DTOs** (Data Transfer Objects)
3. **Services** (Business Logic)
4. **Repositories** (Database Queries)
5. **Security Filter** (JWT Authentication)
6. **Utility Classes** (JWT generation/validation)
7. **Exception Handling**

Which section would you like me to explain next in the same extreme detail?


