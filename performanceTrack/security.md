# Complete JWT Authentication Guide for PerformanceTrack

Let me break this down from the absolute basics to the complete implementation. I'll use real-world scenarios so you understand **why** every line exists in production code.

---

## 🔐 Part 1: Understanding JWT (The Foundation)

### What Problem Does JWT Solve?

**Scenario: Traditional Session-Based Authentication (The Old Way)**

Imagine you're building a restaurant app:

1. **Customer logs in** → Server creates a "session" and stores it in memory/database
2. **Customer orders food** → Server checks: "Do I have a session for this person?" (database lookup)
3. **Customer checks order status** → Server checks session again (another database call)
4. **Customer pays bill** → Server checks session again (another database call)

**Problems:**
- **Scalability Issue**: If you have 10,000 users logged in, you need to store 10,000 sessions in your database
- **Distributed Systems**: If you have 3 servers, Server A's session won't work on Server B
- **Database Load**: Every request requires a database lookup to verify the session

---

**JWT Solution (The Modern Way)**

Instead of storing sessions on the server, we give the user a **signed token** that proves who they are:

1. **Customer logs in** → Server creates a JWT token and gives it to the customer
2. **Customer orders food** → Sends JWT token → Server verifies signature (NO database call!)
3. **Customer checks order status** → Sends JWT token → Server verifies signature (NO database call!)
4. **Customer pays bill** → Sends JWT token → Server verifies signature (NO database call!)

**Advantages:**
- ✅ No session storage needed
- ✅ Works across multiple servers (stateless)
- ✅ No database lookups for every request
- ✅ Perfect for microservices and mobile apps

---

### What's Inside a JWT Token?

A JWT token looks like this:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEyMywicm9sZSI6Ik1BTkFHRVIiLCJlbWFpbCI6InJhaHVsQGNvZ25pemFudC5jb20ifQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

It has **3 parts** separated by dots (`.`):

#### **Part 1: Header** (Algorithm info)
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
*Says: "I'm a JWT token signed with HS256 algorithm"*

#### **Part 2: Payload** (Your data)
```json
{
  "userId": 123,
  "role": "MANAGER",
  "email": "rahul@cognizant.com",
  "iat": 1706400000,
  "exp": 1706486400
}
```
*Contains user information and expiration time*

#### **Part 3: Signature** (Security proof)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  SECRET_KEY
)
```
*Mathematical proof that the token wasn't tampered with*

---

### Real-World Analogy: JWT is Like a Passport

**Passport (JWT Token):**
- **Header** = Type of passport (biometric/regular)
- **Payload** = Your photo, name, nationality, expiry date
- **Signature** = Government's official seal/stamp

**How it works:**
1. **At airport security**: They scan your passport (verify signature)
2. **No need to call your government**: The seal proves it's authentic
3. **Cannot be forged**: If someone changes your photo, the seal becomes invalid
4. **Has expiration**: After expiry, you need a new passport

Same with JWT:
1. **User sends token**: Server verifies the signature
2. **No database call needed**: Signature proves authenticity
3. **Cannot be tampered**: Changing `role: "EMPLOYEE"` to `role: "ADMIN"` breaks the signature
4. **Has expiration**: After 24 hours, user must login again

---

## 💻 Part 2: JwtUtil.java - The Token Factory

Now let's understand your code **line by line** with real scenarios.

### **The Secret Key**

```java
private static final String SECRET = "MySecretKeyForPerformanceTrackApp2026VeryLongSecretKey";
```

**Real-World Scenario:**

This is like the **master key to your bank vault**. 

- **Why so long?** Security! A short key like `"abc123"` can be cracked in seconds
- **Industry Standard**: Minimum 256 bits (32 characters) for HS256 algorithm
- **Your key length**: 62 characters = 496 bits ✅ (Very secure!)

**Why NOT hardcode in production?**

❌ **Bad (Your current code):**
```java
private static final String SECRET = "MySecretKey...";
```

✅ **Good (Production):**
```java
@Value("${jwt.secret}")
private String SECRET; // Loaded from environment variable
```

**Why?**
If someone gets access to your source code on GitHub, they can:
1. Create fake tokens with any role
2. Pretend to be ADMIN
3. Access any user's data

**Production Setup (application.properties):**
```properties
# Local development
jwt.secret=${JWT_SECRET:dev-secret-key-only-for-local}

# Production (Set in AWS/Azure environment variables)
# JWT_SECRET=aksjdh87234hkjsdfh9234jkhsdf98234kjhsdf
```

---

### **Token Expiration**

```java
private static final long EXPIRATION = 86400000; // 24 hours
```

**Real-World Scenario:**

**Why expire tokens?**

Imagine an employee gets fired:

❌ **Without expiration:**
```
Day 1: Employee logs in → Gets JWT token
Day 30: Employee gets fired → You disable their account in database
Day 31: Employee can STILL access the system! (Token is valid for years)
```

✅ **With 24-hour expiration:**
```
Day 1: Employee logs in → Gets JWT token (valid 24 hours)
Day 30: Employee gets fired
Day 31: Token expired → Employee must login again → "Account disabled" error ✅
```

**Industry Standards:**

| Token Type | Typical Expiration | Use Case |
|------------|-------------------|----------|
| Access Token | 15 mins - 1 hour | API requests |
| Refresh Token | 7-30 days | Renewing access tokens |
| Remember Me | 30-90 days | Optional longer sessions |

**Your Choice (24 hours):**
- ✅ Good for internal enterprise apps (PerformanceTrack)
- ❌ Too long for banking apps (use 15 mins)
- ❌ Too short for mobile apps (users login too often)

---

### **Generating the Secret Key**

```java
private Key getSignKey() {
    return Keys.hmacShaKeyFor(SECRET.getBytes());
}
```

**Real-World Scenario:**

Think of this as **converting your password into a mathematical formula**.

**What's happening:**
1. Your secret string → Gets converted to bytes
2. `Keys.hmacShaKeyFor()` → Creates a cryptographic key object
3. This key is used for **signing** and **verifying** tokens

**Why a separate method?**

Instead of writing `Keys.hmacShaKeyFor(SECRET.getBytes())` everywhere:
```java
// ❌ Repetitive
Jwts.builder().signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), ...)
Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))

// ✅ Clean
Jwts.builder().signWith(getSignKey(), ...)
Jwts.parserBuilder().setSigningKey(getSignKey())
```

**Industry Practice**: Reusable helper methods = cleaner code

---

### **Generating JWT Token**

```java
public String generateToken(String email, Integer userId, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("role", role);
    
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();
}
```

**Real-World Scenario: Login Process**

**When user logs in successfully:**

```java
// In AuthController.java
User user = userRepository.findByEmail("rahul@cognizant.com");
if (passwordEncoder.matches(password, user.getPassword())) {
    String token = jwtUtil.generateToken(
        user.getEmail(),    // "rahul@cognizant.com"
        user.getUserId(),   // 123
        user.getRole()      // "MANAGER"
    );
    return ResponseEntity.ok(new AuthResponse(token));
}
```

**Let's break down each line:**

---

#### **1. Creating Claims (Custom Data)**

```java
Map<String, Object> claims = new HashMap<>();
claims.put("userId", userId);
claims.put("role", role);
```

**Claims** = Custom data you want to store in the token

**Why these specific claims?**

**userId:**
```java
// Without userId in token:
@GetMapping("/my-goals")
public List<Goal> getMyGoals(String email) {
    User user = userRepository.findByEmail(email); // 🔴 Database call!
    return goalRepository.findByUserId(user.getUserId());
}

// With userId in token:
@GetMapping("/my-goals")
public List<Goal> getMyGoals(@RequestAttribute Integer userId) {
    return goalRepository.findByUserId(userId); // ✅ No extra database call!
}
```

**role:**
```java
// Authorization becomes instant:
if (jwtUtil.extractRole(token).equals("ADMIN")) {
    // Allow access to admin panel
}
```

**What NOT to store in JWT:**
- ❌ Passwords (security risk!)
- ❌ Credit card numbers (JWT is not encrypted, just signed!)
- ❌ Large data like profile pictures (token becomes huge)

---

#### **2. Set Subject (Main Identifier)**

```java
.setSubject(email)
```

**Subject** = Primary identifier for the token (usually username/email)

**Why email as subject?**

**Industry Standard:**
```java
// JWT Standard Claims:
.setSubject(email)        // "sub" - Main user identifier
.setIssuer("PerformanceTrack")  // "iss" - Who created the token
.setAudience("web-app")   // "aud" - Who should use the token
```

**Your scenario:**
```java
String email = jwtUtil.extractEmail(token); // Gets the "subject"
if (email.equals(loggedInUser.getEmail())) {
    // Token belongs to this user ✅
}
```

---

#### **3. Set Issue Time**

```java
.setIssuedAt(new Date(System.currentTimeMillis()))
```

**Real-World Use Case: Token Revocation**

**Scenario:** Employee changes password

```java
// User's password_changed_at in database: 2026-01-28 10:00 AM

// Old token issued at: 2026-01-28 09:00 AM ❌ (Before password change)
// New token issued at: 2026-01-28 10:30 AM ✅ (After password change)

// In JwtAuthFilter:
Date tokenIssuedAt = jwtUtil.extractIssuedAt(token);
Date passwordChangedAt = user.getPasswordChangedAt();

if (tokenIssuedAt.before(passwordChangedAt)) {
    throw new InvalidTokenException("Password changed, please login again");
}
```

**Industry Use:**
- Debugging: "When was this token created?"
- Audit logs: "User logged in at 9:00 AM"
- Token refresh: "Only refresh tokens older than 23 hours"

---

#### **4. Set Expiration**

```java
.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
```

**Calculation:**
```java
Current time: 2026-01-28 10:00:00
+ 86400000 ms (24 hours)
= 2026-01-29 10:00:00 (Expiration time)
```

**What happens when token expires:**

```java
// In JwtUtil.validateToken():
if (isTokenExpired(token)) {
    return false; // ❌ Token invalid
}

// Frontend gets 401 Unauthorized
// → User redirected to login page
// → User logs in again
// → New token issued ✅
```

---

#### **5. Sign the Token**

```java
.signWith(getSignKey(), SignatureAlgorithm.HS256)
```

**Real-World Analogy: Signing a Cheque**

**Without signature:**
```
Anyone can write: "Pay $1000 to John"
```

**With signature:**
```
"Pay $1000 to John" + Your Signature
→ Bank verifies signature
→ If signature is fake, cheque is rejected
```

**JWT Signature:**
```java
// Token contents:
{
  "userId": 123,
  "role": "EMPLOYEE",
  "email": "john@cognizant.com"
}

// Hacker tries to change:
{
  "userId": 123,
  "role": "ADMIN", // ← Changed!
  "email": "john@cognizant.com"
}

// But signature verification fails:
jwtUtil.validateToken(hackedToken) // ❌ false - Signature mismatch!
```

**HS256 Algorithm:**
- **HS** = HMAC (Hash-based Message Authentication Code)
- **256** = Uses SHA-256 hashing (256-bit)
- **Symmetric** = Same key for signing and verification

**Industry Alternatives:**
```java
SignatureAlgorithm.HS256  // ✅ Your choice - Fast, simple
SignatureAlgorithm.HS512  // More secure, slightly slower
SignatureAlgorithm.RS256  // Asymmetric (public/private key) - For microservices
```

---

#### **6. Build the Token**

```java
.compact();
```

**What compact() does:**
1. Takes all the data you set
2. Converts to JSON
3. Base64-encodes header and payload
4. Generates signature
5. Combines all three parts with dots

**Result:**
```
eyJhbGci...  ← Header (Base64)
.
eyJ1c2Vy...  ← Payload (Base64)
.
SflKxwRJ...  ← Signature (HMAC-SHA256)
```

---

### **Extracting Data from Token**

```java
public String extractEmail(String token) {
    return extractClaims(token).getSubject();
}

public Integer extractUserId(String token) {
    return (Integer) extractClaims(token).get("userId");
}

public String extractRole(String token) {
    return (String) extractClaims(token).get("role");
}
```

**Real-World Usage in Controllers:**

```java
@GetMapping("/my-profile")
public ResponseEntity<UserProfile> getMyProfile(HttpServletRequest request) {
    // Get userId from token (set by JwtAuthFilter)
    Integer userId = (Integer) request.getAttribute("userId");
    String role = (String) request.getAttribute("userRole");
    
    UserProfile profile = userService.getUserProfile(userId);
    return ResponseEntity.ok(profile);
}
```

**Why separate methods instead of one?**

❌ **Bad:**
```java
Claims claims = jwtUtil.extractClaims(token);
Integer userId = (Integer) claims.get("userId");
String role = (String) claims.get("role");
String email = claims.getSubject();
```

✅ **Good:**
```java
Integer userId = jwtUtil.extractUserId(token);
String role = jwtUtil.extractRole(token);
String email = jwtUtil.extractEmail(token);
```

**Benefits:**
1. **Cleaner code**: Method names are self-explanatory
2. **Type safety**: Returns correct type (Integer, String)
3. **Reusability**: Use anywhere without boilerplate
4. **Error handling**: Can add validation in one place

---

### **The Core: extractClaims Method**

```java
private Claims extractClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
}
```

**Real-World Scenario: Reading the Passport**

Think of this as the **immigration officer scanning your passport**.

**Step-by-Step:**

**1. Create Parser:**
```java
Jwts.parserBuilder()
```
*"I'm going to read a JWT token"*

**2. Provide Signing Key:**
```java
.setSigningKey(getSignKey())
```
*"Use this key to verify the signature"*

**Like passport scanner:** Has government's public key to verify seals

**3. Build Parser:**
```java
.build()
```
*"Parser is ready to work"*

**4. Parse and Verify:**
```java
.parseClaimsJws(token)
```
*"Parse the token AND verify signature"*

**What happens internally:**
```java
// Step 1: Split token into 3 parts
String[] parts = token.split("\\.");
String header = parts[0];
String payload = parts[1];
String signature = parts[2];

// Step 2: Recalculate signature
String calculatedSignature = HMAC_SHA256(header + "." + payload, SECRET_KEY);

// Step 3: Compare
if (signature.equals(calculatedSignature)) {
    // ✅ Token is valid, return claims
} else {
    // ❌ Token tampered, throw SignatureException
}
```

**5. Get Body (Claims):**
```java
.getBody()
```
*"Give me the payload data"*

---

### **Validating Token**

```java
public Boolean validateToken(String token, String email) {
    final String tokenEmail = extractEmail(token);
    return (tokenEmail.equals(email) && !isTokenExpired(token));
}
```

**Real-World Scenario: Two-Level Security Check**

**Check 1: Email Match**
```java
tokenEmail.equals(email)
```

**Why needed?**

Imagine:
```java
// User A logs in, gets token
String tokenA = jwtUtil.generateToken("alice@cognizant.com", 1, "EMPLOYEE");

// User B tries to use User A's token
// But User B's email is "bob@cognizant.com"

jwtUtil.validateToken(tokenA, "bob@cognizant.com"); 
// ❌ false - Email mismatch!
```

**Check 2: Not Expired**
```java
!isTokenExpired(token)
```

```java
private Boolean isTokenExpired(String token) {
    return extractClaims(token).getExpiration().before(new Date());
}
```

**Scenario:**
```java
Token expiration: 2026-01-28 10:00:00
Current time:     2026-01-28 10:30:00

extractClaims(token).getExpiration() // 10:00:00
.before(new Date())                  // 10:30:00

// 10:00:00 is before 10:30:00? Yes ✅
// Token expired!
```

---

## 🛡️ Part 3: JwtAuthFilter.java - The Security Guard

This filter **runs on EVERY request** to check if the user is authenticated.

### **Filter Concept in Spring**

**Real-World Analogy: Airport Security Checkpoints**

```
Passenger → Security Check 1 → Security Check 2 → Security Check 3 → Boarding Gate
   ↓              ↓                  ↓                  ↓                  ↓
Request → Filter 1 (CORS) → Filter 2 (JWT) → Filter 3 (Logging) → Controller
```

**Spring Filter Chain:**
```
Client Request
    ↓
[JwtAuthFilter] ← Your custom filter
    ↓
[UsernamePasswordAuthenticationFilter]
    ↓
[FilterSecurityInterceptor]
    ↓
Controller Method
```

---

### **OncePerRequestFilter**

```java
public class JwtAuthFilter extends OncePerRequestFilter {
```

**Why "Once Per Request"?**

**Problem with normal filters:**
```java
// Request to /api/v1/users/123

Normal Filter:
1. Runs on /api/v1/users/123 ✅
2. Request forwarded internally to /error (if error occurs)
3. Filter runs AGAIN on /error ❌ (Unnecessary!)
```

**OncePerRequestFilter ensures:**
```java
// Runs only ONCE per request, no matter what
OncePerRequestFilter:
1. Runs on /api/v1/users/123 ✅
2. Request forwarded to /error
3. Filter does NOT run again ✅
```

---

### **The doFilterInternal Method**

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
```

**Parameters:**
- **request**: Incoming HTTP request (contains headers, body, URL)
- **response**: Outgoing HTTP response
- **filterChain**: Reference to next filter in the chain

---

### **Step 1: Extract Token from Header**

```java
String authHeader = request.getHeader("Authorization");
String token = null;
String email = null;

if (authHeader != null && authHeader.startsWith("Bearer ")) {
    token = authHeader.substring(7);
    try {
        email = jwtUtil.extractEmail(token);
    } catch (Exception e) {
        // Invalid token, continue without authentication
    }
}
```

**Real-World Scenario: Frontend sends request**

**Frontend (React/Angular):**
```javascript
// After login, store token
localStorage.setItem('token', 'eyJhbGci...');

// On every API request:
fetch('http://localhost:8080/api/v1/goals', {
    headers: {
        'Authorization': 'Bearer eyJhbGci...'
    }
});
```

**Backend receives:**
```
GET /api/v1/goals
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Code breakdown:**

**1. Get Authorization Header:**
```java
String authHeader = request.getHeader("Authorization");
// authHeader = "Bearer eyJhbGci..."
```

**2. Check if it exists and starts with "Bearer ":**
```java
if (authHeader != null && authHeader.startsWith("Bearer "))
```

**Why "Bearer"?**

**Industry Standard (RFC 6750):**
```
Authorization: Bearer <token>
Authorization: Basic <credentials>
Authorization: Digest <credentials>
```

**"Bearer"** means: *"The bearer (holder) of this token has access"*

**3. Extract token (remove "Bearer " prefix):**
```java
token = authHeader.substring(7);
// "Bearer " has 7 characters (including space)
// "Bearer eyJhbGci..." → "eyJhbGci..."
```

**4. Extract email from token:**
```java
try {
    email = jwtUtil.extractEmail(token);
} catch (Exception e) {
    // Token is malformed/invalid/expired
    // Don't throw error, just continue
    // User will get 401 Unauthorized later
}
```

**Why try-catch?**

Possible exceptions:
- `MalformedJwtException`: Token format is wrong
- `ExpiredJwtException`: Token expired
- `SignatureException`: Signature verification failed
- `IllegalArgumentException`: Token is null or empty

**Industry Practice:** Don't expose error details to client (security risk)

---

### **Step 2: Validate Token and Set Authentication**

```java
if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    if (jwtUtil.validateToken(token, email)) {
        String role = jwtUtil.extractRole(token);
        Integer userId = jwtUtil.extractUserId(token);
        
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(
                email, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        SecurityContextHolder.getContext().setAuthentication(authToken);
        request.setAttribute("userId", userId);
        request.setAttribute("userRole", role);
    }
}
```

**Let's break this down:**

---

#### **Condition 1: Email Not Null**

```java
if (email != null && ...)
```

**Means:** Token was successfully parsed and email was extracted

---

#### **Condition 2: Not Already Authenticated**

```java
SecurityContextHolder.getContext().getAuthentication() == null
```

**What is SecurityContextHolder?**

**Real-World Analogy: Security Checkpoint Stamp**

```
Passenger → Shows passport → Gets "CLEARED" stamp → Boards plane
Request → Provides JWT → Gets authentication object → Accesses API
```

**SecurityContextHolder** = Thread-local storage that holds authentication info

**Why check if already authenticated?**

Prevent double authentication:
```java
// First request: /api/v1/users/123
→ JwtAuthFilter sets authentication ✅

// If internally forwarded to /api/v1/users/123/details
→ JwtAuthFilter checks: Already authenticated? Yes, skip ✅
```

---

#### **Token Validation**

```java
if (jwtUtil.validateToken(token, email)) {
```

**This checks:**
1. Email in token matches extracted email ✅
2. Token is not expired ✅
3. Signature is valid (checked during `extractEmail()`) ✅

---

#### **Extract User Info**

```java
String role = jwtUtil.extractRole(token);
Integer userId = jwtUtil.extractUserId(token);
```

**Example values:**
```java
role = "MANAGER"
userId = 123
```

---

#### **Create Authentication Token**

```java
UsernamePasswordAuthenticationToken authToken = 
    new UsernamePasswordAuthenticationToken(
        email,                    // Principal (who is this user?)
        null,                     // Credentials (password - not needed for JWT)
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
    );
```

**What is UsernamePasswordAuthenticationToken?**

**Real-World Analogy: Security Badge**

```
Employee Badge:
- Name: Rahul Kumar
- ID: 123
- Role: Manager
- Access Level: Floor 1-5, Server Room
```

```java
UsernamePasswordAuthenticationToken:
- Principal: rahul@cognizant.com (who)
- Credentials: null (no password needed, JWT already verified)
- Authorities: [ROLE_MANAGER] (what they can do)
```

---

#### **Understanding the 3 Parameters:**

**1. Principal (email):**
```java
email // "rahul@cognizant.com"
```

**Used in controllers:**
```java
@GetMapping("/my-profile")
public ResponseEntity<User> getProfile(Principal principal) {
    String email = principal.getName(); // "rahul@cognizant.com"
    return userService.getByEmail(email);
}
```

**2. Credentials (null):**
```java
null
```

**Why null?**

In traditional authentication:
```java
// Username/Password Login:
new UsernamePasswordAuthenticationToken(
    "rahul@cognizant.com",  // username
    "SecurePass123",        // password ← needs to be checked
    authorities
)
```

In JWT:
```java
// JWT is already verified! No need to store password
new UsernamePasswordAuthenticationToken(
    "rahul@cognizant.com",
    null,  // ← No password needed
    authorities
)
```

**3. Authorities (roles):**
```java
Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
```

**Why "ROLE_" prefix?**

**Spring Security Convention:**
```java
// Without prefix:
new SimpleGrantedAuthority("MANAGER")  // ❌ Won't work with @PreAuthorize

// With prefix:
new SimpleGrantedAuthority("ROLE_MANAGER")  // ✅ Works correctly
```

**Usage in controller:**
```java
@PreAuthorize("hasRole('MANAGER')")  // Automatically looks for "ROLE_MANAGER"
@GetMapping("/team-performance")
public ResponseEntity<Report> getTeamReport() {
    // Only accessible by ROLE_MANAGER
}
```

---

#### **Set Request Details**

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```

**What are "details"?**

**Stores request metadata:**
```java
WebAuthenticationDetails {
    remoteAddress: "192.168.1.100"  // Client IP
    sessionId: "ABC123XYZ"          // Session ID (if any)
}
```

**Industry Use Cases:**

**1. Audit Logging:**
```java
@PostMapping("/salary-update")
public void updateSalary() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    WebAuthenticationDetails details = (WebAuthenticationDetails) auth.getDetails();
    
    auditLog.log("Salary updated by " + auth.getName() + 
                 " from IP: " + details.getRemoteAddress());
}
```

**2. Security Monitoring:**
```java
// Detect suspicious activity:
if (!isAllowedIP(details.getRemoteAddress())) {
    throw new SecurityException("Access from unauthorized IP");
}
```

---

#### **Store Authentication in SecurityContext**

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

**What this does:**

**Before:**
```java
SecurityContextHolder.getContext().getAuthentication() // null
```

**After:**
```java
SecurityContextHolder.getContext().getAuthentication() 
// → UsernamePasswordAuthenticationToken{
//     principal: "rahul@cognizant.com",
//     authorities: [ROLE_MANAGER],
//     authenticated: true
// }
```

**Now Spring Security knows:**
- ✅ User is authenticated
- ✅ User's email is "rahul@cognizant.com"
- ✅ User has ROLE_MANAGER
- ✅ User can access endpoints requiring authentication

---

#### **Store Additional Attributes in Request**

```java
request.setAttribute("userId", userId);
request.setAttribute("userRole", role);
```

**Why store separately?**

**SecurityContext vs Request Attributes:**

```java
// SecurityContext:
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String email = auth.getName(); // ✅ Easy
List<GrantedAuthority> roles = auth.getAuthorities(); // ✅ Easy

// But to get userId:
// ❌ Not stored in SecurityContext!
// Need to query database:
User user = userRepository.findByEmail(email);
Integer userId = user.getUserId(); // 🔴 Extra database call!

// Request Attributes (Your Solution):
Integer userId = (Integer) request.getAttribute("userId"); // ✅ Instant!
String role = (String) request.getAttribute("userRole"); // ✅ Instant!
```

**Usage in Controllers:**

```java
@GetMapping("/my-goals")
public ResponseEntity<List<Goal>> getMyGoals(
    @RequestAttribute Integer userId,
    @RequestAttribute String userRole
) {
    // No database lookup needed! ✅
    List<Goal> goals = goalService.findByUserId(userId);
    return ResponseEntity.ok(goals);
}
```

**Performance Benefit:**

❌ **Without request attributes:**
```java
// Every request:
1. Extract email from token
2. Query database for user by email
3. Get userId from database result
Total: 1 database call per request
```

✅ **With request attributes:**
```java
// Every request:
1. Extract userId from token
2. Store in request
3. Use directly in controller
Total: 0 database calls
```

**For 1000 requests/minute:** Saves 1000 database queries/minute!

---

### **Step 3: Continue Filter Chain**

```java
filterChain.doFilter(request, response);
```

**Real-World Analogy: Passing to Next Security Checkpoint**

```
Airport Security:
Checkpoint 1 (Document Check) → Pass to Checkpoint 2 (Baggage Scan) → Pass to Gate
      ↓                              ↓                                   ↓
   JwtAuthFilter → CSRF Filter → Logging Filter → Controller Method
```

**What happens:**
1. **Token valid?** → Sets authentication → Calls `filterChain.doFilter()` → Request proceeds
2. **Token invalid?** → No authentication set → Calls `filterChain.doFilter()` → Request proceeds but will get 401 later
3. **No token?** → No authentication set → Calls `filterChain.doFilter()` → Request proceeds but will get 401 for protected endpoints

**Why not throw exception here?**

Because some endpoints are public:
```java
.requestMatchers("/api/v1/auth/**").permitAll()  // Login, signup
```

If user accesses `/api/v1/auth/login` without a token:
- ✅ No error thrown in JwtAuthFilter
- ✅ Request reaches login controller
- ✅ User can login and get a token

---

## 🔐 Part 4: SecurityConfig.java - The Rulebook

This class defines **WHO can access WHAT**.

### **Basic Configuration**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
```

**Annotations Explained:**

**1. @Configuration:**
```java
@Configuration
```
*"This class contains Spring configuration"*

**Like:**
```java
// Old XML configuration (Spring 2.x):
<beans>
    <bean id="securityConfig" class="SecurityConfig"/>
</beans>

// Modern Java config:
@Configuration
public class SecurityConfig { }
```

---

**2. @EnableWebSecurity:**
```java
@EnableWebSecurity
```
*"Enable Spring Security's web security features"*

**What it enables:**
- HTTP security configuration
- Filter chain registration
- Authentication/authorization
- CSRF protection (which we disable)
- Session management

**Without this annotation:**
```java
// Your security config won't work!
// Spring won't know this class configures security
```

---

**3. @EnableMethodSecurity:**
```java
@EnableMethodSecurity
```
*"Enable method-level security annotations"*

**Allows you to use:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/delete-user")
public void deleteUser() { }

@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@GetMapping("/team-reports")
public List<Report> getReports() { }

@PreAuthorize("#userId == authentication.principal.userId")
@GetMapping("/users/{userId}/profile")
public Profile getProfile(@PathVariable Integer userId) { }
```

**Industry Use:**
- ✅ Fine-grained access control
- ✅ Business logic security
- ✅ Dynamic role checking

---

### **Password Encoder Bean**

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Real-World Scenario: Secure Password Storage**

**❌ Never store plain passwords:**
```java
// If hacker gets database access:
Users Table:
| email                  | password      |
|------------------------|---------------|
| rahul@cognizant.com    | Pass123       |  ← Hacker sees password!
| priya@cognizant.com    | MySecret456   |  ← Hacker sees password!
```

**✅ Always hash passwords:**
```java
// BCrypt hashed:
Users Table:
| email                  | password                                                       |
|------------------------|---------------------------------------------------------------|
| rahul@cognizant.com    | $2a$10$N9qo8uLOickgx2ZMRZoMye0LjESw9uf5J5.6Y9vC3eT5bQ7eC7NM6 |
| priya@cognizant.com    | $2a$10$xBUy3N9R2kV6N8I8YbX6LuO5p8RxXy2J3Gx5M7Qx8K4Yx2Qx9N8 |
```

**BCrypt Features:**

**1. One-way hashing:**
```java
String hashed = passwordEncoder.encode("Pass123");
// hashed = "$2a$10$N9qo8u..."

// Cannot reverse:
String original = passwordEncoder.decode(hashed); // ❌ Method doesn't exist!
```

**2. Same password → Different hashes (salt):**
```java
passwordEncoder.encode("Pass123") 
// → "$2a$10$ABC..."

passwordEncoder.encode("Pass123") 
// → "$2a$10$XYZ..." ← Different hash!
```

**Why?** BCrypt adds random "salt" to each hash

**3. Slow by design (prevents brute force):**
```java
// MD5: Can test 1 billion passwords/second
// BCrypt: Can test ~10,000 passwords/second ✅
```

**Usage in your code:**

**Registration:**
```java
@PostMapping("/signup")
public void signup(UserRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash
    userRepository.save(user);
}
```

**Login:**
```java
@PostMapping("/login")
public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail());
    
    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        // ✅ Password correct
        String token = jwtUtil.generateToken(...);
        return new AuthResponse(token);
    } else {
        // ❌ Password wrong
        throw new BadCredentialsException("Invalid credentials");
    }
}
```

---

### **Security Filter Chain - The Core Configuration**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
```

**What is SecurityFilterChain?**

**Real-World Analogy: Office Building Security Rules**

```
Building Rules Document:
1. Visitors can access lobby without ID
2. Employees need badge for floors 1-5
3. Managers can access server room
4. Only ADMIN can access CEO floor
```

```java
SecurityFilterChain:
1. /api/v1/auth/** → Anyone can access (login/signup)
2. /api/v1/goals/** → Authenticated users only
3. /api/v1/users/** → ADMIN or MANAGER only
4. /api/v1/audit-logs/** → ADMIN only
```

---

### **Disable CSRF**

```java
.csrf(csrf -> csrf.disable())
```

**What is CSRF?**

**Cross-Site Request Forgery Attack:**

**Scenario: Malicious Website Attack**

**Step 1:** User logs into `PerformanceTrack.com`
**Step 2:** User visits malicious site `evil-hacker.com`
**Step 3:** Malicious site has hidden form:

```html
<form action="https://performancetrack.com/api/v1/users/delete" method="POST">
    <input type="hidden" name="userId" value="123">
</form>
<script>
    // Auto-submit when page loads
    document.forms[0].submit();
</script>
```

**Step 4:** Since user is logged in, browser sends cookies → User 123 gets deleted!

---

**CSRF Protection (Traditional Web Apps):**

```java
// Spring generates random token for each form
<form action="/delete-user" method="POST">
    <input type="hidden" name="_csrf" value="a8f3b7c2..."/>
    <button>Delete</button>
</form>

// Server checks:
if (request.getParameter("_csrf").equals(session.getCsrfToken())) {
    // ✅ Valid request
} else {
    // ❌ CSRF attack!
}
```

---

**Why Disable for JWT?**

**JWT is immune to CSRF:**

**JWT Approach:**
```javascript
// Frontend stores token in localStorage (NOT cookies)
localStorage.setItem('token', 'eyJhbGci...');

// Every request sends token in header
fetch('/api/users/delete', {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
});
```

**Malicious site CANNOT access localStorage:**
```javascript
// On evil-hacker.com:
localStorage.getItem('token'); // ❌ null (different domain)

// Cannot send authenticated request ✅
```

**Key Difference:**

| Cookies | LocalStorage |
|---------|--------------|
| Sent automatically by browser | Must be manually added to requests |
| Vulnerable to CSRF | Not vulnerable to CSRF |
| Works across domains (if configured) | Cannot be accessed across domains |

**When to enable CSRF:**
- ✅ Traditional web apps with session cookies
- ✅ Server-side rendered forms

**When to disable CSRF:**
- ✅ REST APIs with JWT
- ✅ Mobile apps
- ✅ Single Page Applications (React, Angular, Vue)

---

### **Authorization Rules**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/**").permitAll()
    .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
    .requestMatchers("/api/v1/review-cycles/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/audit-logs/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

**Let's break down each rule:**

---

#### **Rule 1: Public Endpoints**

```java
.requestMatchers("/api/v1/auth/**").permitAll()
```

**What this means:**
```
/api/v1/auth/login    ✅ No authentication required
/api/v1/auth/signup   ✅ No authentication required
/api/v1/auth/refresh  ✅ No authentication required
```

**Real-World Scenario:**

**Without this rule:**
```
User → Tries to login → Needs JWT token to access login endpoint → Catch-22!
```

**With this rule:**
```
User → Accesses /auth/login without token ✅
     → Provides email/password
     → Gets JWT token
     → Uses token for other APIs ✅
```

**The `/**` wildcard:**
```java
/api/v1/auth/**  matches:
    /api/v1/auth/login
    /api/v1/auth/signup
    /api/v1/auth/password-reset
    /api/v1/auth/verify-email
    /api/v1/auth/anything/here
```

---

#### **Rule 2: Admin or Manager Only**

```java
.requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
```

**Real-World Scenario: User Management**

```
ADMIN tries:
    GET /api/v1/users → ✅ Allowed (List all users)
    POST /api/v1/users → ✅ Allowed (Create user)
    DELETE /api/v1/users/123 → ✅ Allowed (Delete user)

MANAGER tries:
    GET /api/v1/users → ✅ Allowed (View team members)
    POST /api/v1/users → ✅ Allowed (Add team member)
    DELETE /api/v1/users/123 → ✅ Allowed (Remove team member)

EMPLOYEE tries:
    GET /api/v1/users → ❌ 403 Forbidden
    POST /api/v1/users → ❌ 403 Forbidden
```

**How it works:**
```java
// JWT token contains:
{
  "role": "MANAGER"
}

// JwtAuthFilter sets:
new SimpleGrantedAuthority("ROLE_MANAGER")

// SecurityConfig checks:
hasAnyRole("ADMIN", "MANAGER")
// → Looks for "ROLE_ADMIN" or "ROLE_MANAGER"
// → User has "ROLE_MANAGER" ✅
// → Access granted!
```

---

#### **Rule 3: Admin Only (Review Cycles)**

```java
.requestMatchers("/api/v1/review-cycles/**").hasRole("ADMIN")
```

**Real-World Scenario: Performance Review Management**

```
ADMIN tries:
    POST /api/v1/review-cycles → ✅ (Create annual review cycle)
    PUT /api/v1/review-cycles/1 → ✅ (Update cycle dates)
    DELETE /api/v1/review-cycles/1 → ✅ (Cancel cycle)

MANAGER tries:
    POST /api/v1/review-cycles → ❌ 403 Forbidden
    (Managers shouldn't create company-wide review cycles)

EMPLOYEE tries:
    POST /api/v1/review-cycles → ❌ 403 Forbidden
```

**Business Justification:**

Only ADMIN should:
- Create annual review cycles
- Set review deadlines
- Configure review forms
- Modify cycle parameters

Managers and Employees:
- Can view their assigned reviews
- Can submit reviews
- But cannot manage cycles

---

#### **Rule 4: Admin Only (Audit Logs)**

```java
.requestMatchers("/api/v1/audit-logs/**").hasRole("ADMIN")
```

**Real-World Scenario: Security Compliance**

```
Audit Logs contain sensitive information:
- Who accessed what data
- When passwords were changed
- Failed login attempts
- Data modifications

ADMIN can:
    GET /api/v1/audit-logs → ✅ (View all logs)
    GET /api/v1/audit-logs/user/123 → ✅ (View specific user's logs)

MANAGER cannot:
    GET /api/v1/audit-logs → ❌ (Cannot see logs)
    (Prevents managers from tracking employees' every action)

EMPLOYEE cannot:
    GET /api/v1/audit-logs → ❌ (Cannot see logs)
```

**Compliance Requirements:**

Many regulations require:
- **SOC 2**: Only admins can access audit logs
- **GDPR**: Log access must be restricted
- **HIPAA**: Audit logs must be protected

---

#### **Rule 5: Default - Authenticated Users**

```java
.anyRequest().authenticated()
```

**What this means:**

```
All OTHER endpoints require authentication:
    /api/v1/goals/** → Must be logged in
    /api/v1/feedback/** → Must be logged in
    /api/v1/profile/** → Must be logged in
    /api/v1/anything-else/** → Must be logged in
```

**Order Matters!**

```java
// ✅ Correct order:
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
.anyRequest().authenticated()

// ❌ Wrong order:
.anyRequest().authenticated()  // ← This catches everything first!
.requestMatchers("/api/v1/auth/**").permitAll()  // ← Never reached!
```

**Think of it as if-else:**
```java
if (url.matches("/api/v1/auth/**")) {
    permitAll();
} else if (url.matches("/api/v1/users/**")) {
    requireRole("ADMIN", "MANAGER");
} else {
    requireAuthenticated();
}
```

---

### **Session Management**

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**What is SessionCreationPolicy?**

**Traditional Session-Based Auth:**
```java
// User logs in
HttpSession session = request.getSession();
session.setAttribute("userId", 123);
session.setAttribute("role", "MANAGER");

// Server stores session in memory/database:
Sessions Table:
| session_id  | user_id | role    | last_access |
|-------------|---------|---------|-------------|
| ABC123XYZ   | 123     | MANAGER | 10:30 AM    |
```

**Problems:**
- **Memory usage**: Storing millions of sessions
- **Scalability**: Sessions tied to specific server
- **Cleanup**: Expired sessions need to be deleted

---

**JWT Stateless Auth:**
```java
SessionCreationPolicy.STATELESS

// Server does NOT create sessions
// All user info is in JWT token
// No memory/database storage needed ✅
```

**Benefits:**

**1. Horizontal Scaling:**
```
Load Balancer
    ↓
Server 1 ← Request with JWT → Verifies token ✅
Server 2 ← Request with JWT → Verifies token ✅
Server 3 ← Request with JWT → Verifies token ✅

No session sharing needed!
```

**2. Microservices:**
```
Gateway → Auth Service (verifies JWT)
       → User Service (uses JWT)
       → Goal Service (uses JWT)
       → Feedback Service (uses JWT)

All services can verify JWT independently!
```

**3. Mobile Apps:**
```
Mobile App:
- Stores JWT token locally
- Sends token with each request
- No cookies needed
- Works offline (token cached)
```

---

### **Add JWT Filter**

```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

**What this does:**

**Spring Security's Default Filter Chain:**
```
1. SecurityContextPersistenceFilter
2. UsernamePasswordAuthenticationFilter  ← Default login filter
3. AnonymousAuthenticationFilter
4. FilterSecurityInterceptor
```

**Your Modified Chain:**
```
1. SecurityContextPersistenceFilter
2. JwtAuthFilter  ← Your custom filter (ADDED BEFORE)
3. UsernamePasswordAuthenticationFilter
4. AnonymousAuthenticationFilter
5. FilterSecurityInterceptor
```

**Why "Before"?**

```java
// Request Flow:
Request arrives
    ↓
JwtAuthFilter runs FIRST
    ↓ (Extracts JWT, sets authentication)
UsernamePasswordAuthenticationFilter
    ↓ (Skipped - already authenticated)
Controller
```

**If added AFTER:**
```java
Request arrives
    ↓
UsernamePasswordAuthenticationFilter runs FIRST
    ↓ (Looks for username/password - not found)
JwtAuthFilter
    ↓ (Too late, already rejected)
❌ 401 Unauthorized
```

---

## 🔄 Complete Request Flow

Let me show you the **entire journey of a request** through your security system:

### **Scenario 1: Login (Public Endpoint)**

```
1. Frontend sends:
   POST /api/v1/auth/login
   Body: { "email": "rahul@cognizant.com", "password": "Pass123" }

2. JwtAuthFilter runs:
   - No Authorization header → Skips authentication
   - Calls filterChain.doFilter()

3. SecurityConfig checks:
   - URL matches "/api/v1/auth/**" → permitAll() ✅
   - Allows request to proceed

4. AuthController receives request:
   @PostMapping("/login")
   public AuthResponse login(LoginRequest request) {
       User user = userRepository.findByEmail(request.getEmail());
       if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
           String token = jwtUtil.generateToken(
               user.getEmail(),
               user.getUserId(),
               user.getRole()
           );
           return new AuthResponse(token);
       }
       throw new BadCredentialsException("Invalid credentials");
   }

5. Response:
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "expiresIn": 86400000
   }

6. Frontend stores token:
   localStorage.setItem('token', response.token);
```

---

### **Scenario 2: Accessing Protected Endpoint**

```
1. Frontend sends:
   GET /api/v1/goals/my-goals
   Headers:
     Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthFilter runs:
   a) Extracts token from header:
      token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   
   b) Validates token:
      email = jwtUtil.extractEmail(token) // "rahul@cognizant.com"
      isValid = jwtUtil.validateToken(token, email) // true
   
   c) Extracts user info:
      userId = jwtUtil.extractUserId(token) // 123
      role = jwtUtil.extractRole(token) // "MANAGER"
   
   d) Creates authentication:
      authToken = new UsernamePasswordAuthenticationToken(
          "rahul@cognizant.com",
          null,
          [ROLE_MANAGER]
      )
   
   e) Sets authentication:
      SecurityContextHolder.getContext().setAuthentication(authToken)
      request.setAttribute("userId", 123)
      request.setAttribute("userRole", "MANAGER")
   
   f) Continues:
      filterChain.doFilter(request, response)

3. SecurityConfig checks:
   - User is authenticated ✅
   - URL doesn't match specific rules
   - anyRequest().authenticated() → Allows ✅

4. GoalController receives request:
   @GetMapping("/my-goals")
   public List<Goal> getMyGoals(@RequestAttribute Integer userId) {
       return goalService.findByUserId(userId); // Uses userId from token!
   }

5. Response:
   [
     { "goalId": 1, "title": "Complete Q1 targets", "status": "IN_PROGRESS" },
     { "goalId": 2, "title": "Improve team efficiency", "status": "COMPLETED" }
   ]
```

---

### **Scenario 3: Accessing Admin-Only Endpoint (As Manager)**

```
1. Frontend sends:
   GET /api/v1/audit-logs
   Headers:
     Authorization: Bearer eyJhbGci... (MANAGER token)

2. JwtAuthFilter runs:
   - Validates token ✅
   - Sets authentication with ROLE_MANAGER

3. SecurityConfig checks:
   - URL matches "/api/v1/audit-logs/**"
   - Rule: hasRole("ADMIN")
   - User has: ROLE_MANAGER
   - ❌ Does not match!

4. Spring Security blocks request:
   Response: 403 Forbidden
   {
     "error": "Access Denied",
     "message": "Insufficient permissions"
   }

5. Controller never reached
```

---

### **Scenario 4: Token Expired**

```
1. Frontend sends:
   GET /api/v1/goals/my-goals
   Headers:
     Authorization: Bearer <expired_token>

2. JwtAuthFilter runs:
   a) Extracts token
   b) Tries to extract email:
      try {
          email = jwtUtil.extractEmail(token);
      } catch (ExpiredJwtException e) {
          // Token expired!
          email = null;
      }
   c) email is null → Skips authentication
   d) Continues without setting authentication

3. SecurityConfig checks:
   - User is NOT authenticated
   - anyRequest().authenticated() → Blocks ❌

4. Spring Security responds:
   401 Unauthorized

5. Frontend handles:
   if (response.status === 401) {
       localStorage.removeItem('token');
       redirectToLogin();
   }
```

---

## 📊 Complete Security Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                     CLIENT (React/Angular)                    │
│  localStorage.setItem('token', 'eyJhbGci...')                │
└────────────────────────────┬─────────────────────────────────┘
                             │
                             │ HTTP Request
                             │ Authorization: Bearer eyJhbGci...
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                    │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              1. JwtAuthFilter (Security Guard)           │ │
│  │                                                          │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │ Extract token from Authorization header           │  │ │
│  │  │ token = header.substring(7)                      │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  │                      ↓                                  │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │ Validate token signature & expiration            │  │ │
│  │  │ jwtUtil.validateToken(token, email)              │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  │                      ↓                                  │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │ Extract user info from token                     │  │ │
│  │  │ userId = 123, role = "MANAGER"                   │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  │                      ↓                                  │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │ Set authentication in SecurityContext            │  │ │
│  │  │ + Store userId and role in request attributes   │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                ↓                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │            2. SecurityConfig (Rulebook)                  │ │
│  │                                                          │ │
│  │  /api/v1/auth/** → permitAll() ✅                       │ │
│  │  /api/v1/users/** → hasAnyRole("ADMIN", "MANAGER") ✅   │ │
│  │  /api/v1/audit-logs/** → hasRole("ADMIN") ❌            │ │
│  │  anyRequest() → authenticated() ✅                       │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                ↓                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              3. Controller (Business Logic)              │ │
│  │                                                          │ │
│  │  @GetMapping("/my-goals")                               │ │
│  │  public List<Goal> getMyGoals(                          │ │
│  │      @RequestAttribute Integer userId                   │ │
│  │  ) {                                                    │ │
│  │      return goalService.findByUserId(userId);           │ │
│  │  }                                                      │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                             │
                             │ HTTP Response
                             │ 200 OK + Data
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                          CLIENT                               │
│  Displays data to user                                        │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Takeaways for PerformanceTrack

**1. JWT = Stateless Authentication**
- No session storage needed
- Scales horizontally
- Perfect for microservices

**2. Security Layers:**
```
Layer 1: JwtAuthFilter → Validates token
Layer 2: SecurityConfig → Checks permissions
Layer 3: @PreAuthorize → Method-level security
```

**3. Performance Benefits:**
```
Traditional: Every request → Database lookup for session
JWT: Every request → Signature verification (no database) ✅
```

**4. Security Best Practices You're Following:**
- ✅ BCrypt password hashing
- ✅ JWT expiration (24 hours)
- ✅ Role-based access control
- ✅ CSRF protection disabled (REST API)
- ✅ Stateless sessions

**5. Production Improvements Needed:**
```java
// 1. Move secret to environment variable
@Value("${jwt.secret}")
private String SECRET;

// 2. Add refresh tokens (30-day expiration)
// 3. Implement token blacklist for logout
// 4. Add rate limiting
// 5. Log all authentication failures
```

This implementation is **production-ready at a trainee level** and shows you understand enterprise security patterns. You can confidently explain every line during interviews or code reviews! 🚀
