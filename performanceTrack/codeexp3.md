# Complete Beginner's Guide to PerformanceTrack Entities

I'll explain everything in simple terms, starting from the basics!

## 📚 What Are These Files?

These are **Entity classes** - they represent **tables in your database**. Think of them as blueprints that tell your database what kind of data to store and how different pieces of data relate to each other.

---

## 🔧 Understanding the Annotations

### **Class-Level Annotations**

#### `@Entity`
- **What it does**: Tells Spring Boot "this class represents a database table"
- **Simple terms**: This class will become a table in your database
- **Example**: When you write `@Entity` on `User` class, Spring creates a `users` table

#### `@Table(name = "users")`
- **What it does**: Specifies the exact table name in the database
- **Simple terms**: Without this, table name would be "user" (class name). With this, it's "users"
- **Other options**:
  ```java
  @Table(
      name = "users",
      schema = "public",  // database schema
      uniqueConstraints = @UniqueConstraint(columnNames = {"email"})
  )
  ```

#### `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok)
- **What they do**: Automatically generate code you'd normally write manually
- `@Data`: Creates getters, setters, toString(), equals(), hashCode()
- `@NoArgsConstructor`: Creates a constructor with no parameters: `new User()`
- `@AllArgsConstructor`: Creates a constructor with all parameters: `new User(id, name, email, ...)`
- **Simple terms**: Saves you from writing 100+ lines of repetitive code!

---

### **Field-Level Annotations**

#### `@Id`
- **What it does**: Marks this field as the **primary key** (unique identifier)
- **Simple terms**: Like a student ID number - each row gets a unique one
- **Example**: `userId` is the primary key for User table

#### `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **What it does**: Tells database to automatically generate ID values
- **Simple terms**: When you create a new user, you don't provide the ID - database does it automatically (1, 2, 3, 4...)
- **Other strategies**:
  ```java
  GenerationType.AUTO      // Let JPA choose
  GenerationType.IDENTITY  // Database auto-increment (MySQL, PostgreSQL)
  GenerationType.SEQUENCE  // Use database sequence (Oracle)
  GenerationType.TABLE     // Use separate table for IDs
  ```

#### `@Column`
- **What it does**: Customizes how the field maps to a database column
- **Common options**:
  ```java
  @Column(
      name = "user_id",           // Column name in database
      nullable = false,           // Cannot be NULL (required)
      unique = true,              // Must be unique across all rows
      length = 100,               // Max length for strings
      columnDefinition = "TEXT",  // Specific database type
      updatable = false           // Cannot be changed after creation
  )
  ```

#### `@Enumerated(EnumType.STRING)`
- **What it does**: Stores enum values in the database
- **Two options**:
  - `EnumType.STRING`: Stores "ACTIVE", "INACTIVE" as text (better!)
  - `EnumType.ORDINAL`: Stores 0, 1, 2 as numbers (dangerous if you reorder enum!)
- **Example**:
  ```java
  public enum UserStatus { ACTIVE, INACTIVE }
  
  @Enumerated(EnumType.STRING)
  private UserStatus status;
  // Database stores: "ACTIVE" or "INACTIVE"
  ```

---

### **Relationship Annotations**

#### `@ManyToOne`
- **What it means**: Many records of this entity can point to ONE record of another entity
- **Real-world example**: Many employees can have ONE manager
- **Visual**:
  ```
  Employee 1 ----→ Manager A
  Employee 2 ----→ Manager A
  Employee 3 ----→ Manager B
  ```

#### `@OneToMany`
- **What it means**: One record can have MANY related records
- **Real-world example**: One manager has MANY employees
- **Not explicitly used in your code, but it's the reverse of @ManyToOne**

#### `@OneToOne`
- **What it means**: One record relates to exactly ONE other record
- **Real-world example**: One employee has ONE employee profile

#### `@ManyToMany`
- **What it means**: Many records relate to MANY other records
- **Real-world example**: Many students enroll in MANY courses
- **Your code uses this**: PerformanceReview ↔ Goal (through PerformanceReviewGoals table)

#### `@JoinColumn(name = "manager_id")`
- **What it does**: Specifies the foreign key column name
- **Simple terms**: Creates a column that stores the ID of the related record
- **Example**:
  ```java
  @ManyToOne
  @JoinColumn(name = "manager_id")
  private User manager;
  // Creates a "manager_id" column that stores the manager's userId
  ```

---

### **Timestamp Annotations**

#### `@CreationTimestamp`
- **What it does**: Automatically sets timestamp when record is created
- **Simple terms**: Saves "when was this created" automatically
- **Example**: When you create a user, `createdDate` is set to current time

#### `@UpdateTimestamp`
- **What it does**: Automatically updates timestamp whenever record changes
- **Simple terms**: Saves "when was this last modified" every time you update

---

## 🗂️ Entity-by-Entity Breakdown

### **1. User Entity** 👤

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;              // Primary key (1, 2, 3...)
    
    private String name;                 // User's full name
    private String email;                // Login email (unique)
    private String passwordHash;         // Encrypted password
    
    @Enumerated(EnumType.STRING)
    private UserRole role;               // ADMIN, MANAGER, or EMPLOYEE
    
    private String department;           // Which department (Sales, IT, etc.)
    
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;                // Who is this user's manager?
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;           // ACTIVE or INACTIVE
    
    @CreationTimestamp
    private LocalDateTime createdDate;   // When account was created
    
    @UpdateTimestamp
    private LocalDateTime lastModifiedDate; // When account was last updated
}
```

**Relationships:**
- **Self-referencing**: A User can have a manager who is also a User
  ```
  Employee John → manager → Manager Sarah
  Manager Sarah → manager → null (she's the boss)
  ```

---

### **2. Goal Entity** 🎯

```java
@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer goalId;
    
    private String title;                    // "Complete Spring Boot Training"
    private String description;              // Detailed explanation
    
    @Enumerated(EnumType.STRING)
    private GoalCategory category;           // LEARNING, PERFORMANCE, etc.
    
    @Enumerated(EnumType.STRING)
    private GoalPriority priority;           // HIGH, MEDIUM, LOW
    
    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;             // Employee working on goal
    
    @ManyToOne
    @JoinColumn(name = "assigned_manager_id")
    private User assignedManager;            // Manager overseeing goal
    
    private LocalDate startDate;             // When goal starts
    private LocalDate endDate;               // When goal should be completed
    
    @Enumerated(EnumType.STRING)
    private GoalStatus status;               // PENDING, IN_PROGRESS, COMPLETED
    
    private String evidenceLink;             // Link to proof of completion
    private String evidenceLinkDescription;  // What the link contains
    
    @Enumerated(EnumType.STRING)
    private EvidenceVerificationStatus evidenceLinkVerificationStatus;
    // Has manager verified the evidence?
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;                 // Manager who approved goal
    
    private LocalDateTime approvedDate;      // When it was approved
}
```

**Relationships:**
- **Goal → User (assignedToUser)**: Many goals belong to one employee
- **Goal → User (assignedManager)**: Many goals are overseen by one manager
- **Goal → User (approvedBy)**: Many goals are approved by one manager

**Workflow:**
```
1. Employee creates goal → status: PENDING
2. Manager approves → status: IN_PROGRESS
3. Employee completes, submits evidence → status: PENDING_COMPLETION_APPROVAL
4. Manager verifies evidence → status: COMPLETED
```

---

### **3. ReviewCycle Entity** 🔄

```java
@Entity
@Table(name = "review_cycles")
public class ReviewCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cycleId;
    
    private String title;                    // "Q1 2025 Review"
    private LocalDate startDate;             // Jan 1, 2025
    private LocalDate endDate;               // Mar 31, 2025
    
    @Enumerated(EnumType.STRING)
    private ReviewCycleStatus status;        // ACTIVE or CLOSED
    
    private Boolean requiresCompletionApproval; // Do managers approve completions?
    private Boolean evidenceRequired;           // Must employees provide evidence?
}
```

**Purpose:** Defines time periods for performance reviews (quarterly, annually, etc.)

---

### **4. PerformanceReview Entity** 📝

```java
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;
    
    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private ReviewCycle cycle;               // Which review period?
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;                       // Employee being reviewed
    
    private String selfAssessment;           // Employee's self-evaluation (JSON)
    private Integer employeeSelfRating;      // Employee rates themselves (1-5)
    
    private String managerFeedback;          // Manager's comments (JSON)
    private Integer managerRating;           // Manager's rating (1-5)
    
    private String ratingJustification;      // Why this rating?
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;                 // Which manager did the review?
    
    @Enumerated(EnumType.STRING)
    private PerformanceReviewStatus status;  // PENDING, COMPLETED, etc.
    
    @ManyToOne
    @JoinColumn(name = "acknowledged_by")
    private User acknowledgedBy;             // Employee confirms they've seen it
    
    private LocalDateTime acknowledgedDate;
}
```

**Relationships:**
- **PerformanceReview → ReviewCycle**: Many reviews happen in one cycle
- **PerformanceReview → User (user)**: Many reviews about one employee
- **PerformanceReview → User (reviewedBy)**: Many reviews done by one manager

**Workflow:**
```
1. Employee writes self-assessment → SELF_ASSESSMENT_COMPLETED
2. Manager writes review → MANAGER_REVIEW_COMPLETED
3. System finalizes → COMPLETED
4. Employee acknowledges → COMPLETED_AND_ACKNOWLEDGED
```

---

### **5. PerformanceReviewGoals Entity** 🔗

```java
@Entity
@Table(name = "performance_review_goals")
public class PerformanceReviewGoals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer linkId;
    
    @ManyToOne
    @JoinColumn(name = "review_id")
    private PerformanceReview review;
    
    @ManyToOne
    @JoinColumn(name = "goal_id")
    private Goal goal;
    
    private LocalDateTime linkedDate;
}
```

**Purpose:** **Junction/Bridge table** for Many-to-Many relationship
- One review can reference MANY goals
- One goal can appear in MANY reviews

**Example:**
```
Q1 Review for John:
  - Goal 1: Complete training
  - Goal 2: Improve sales
  - Goal 3: Mentor junior staff

Q2 Review for John:
  - Goal 2: Improve sales (carried over)
  - Goal 4: Launch new product
```

---

### **6. Notification Entity** 🔔

```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;                       // Who gets this notification?
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;           // GOAL_APPROVED, REVIEW_REMINDER, etc.
    
    private String message;                  // "Your goal was approved!"
    
    private String relatedEntityType;        // "Goal", "Review", etc.
    private Integer relatedEntityId;         // ID of the related goal/review
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;       // UNREAD or READ
    
    private Boolean actionRequired;          // Does user need to do something?
    
    @CreationTimestamp
    private LocalDateTime createdDate;
    
    private LocalDateTime readDate;          // When user read it
}
```

**Example Notification:**
```
User: John (Employee)
Type: GOAL_APPROVED
Message: "Your goal 'Complete Java Training' has been approved by Sarah"
RelatedEntityType: "Goal"
RelatedEntityId: 42
Status: UNREAD
ActionRequired: false
```

---

### **7. GoalCompletionApproval Entity** ✅

```java
@Entity
@Table(name = "goal_completion_approvals")
public class GoalCompletionApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer approvalId;
    
    @ManyToOne
    @JoinColumn(name = "goal_id")
    private Goal goal;
    
    private String approvalDecision;         // APPROVED, REJECTED, ADDITIONAL_EVIDENCE_REQUIRED
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;                 // Which manager made decision?
    
    private LocalDateTime approvalDate;
    
    private String managerComments;          // Manager's feedback
    
    private Boolean evidenceLinkVerified;    // Is evidence link valid?
}
```

**Purpose:** Tracks manager decisions when employees submit completed goals

---

### **8. Feedback Entity** 💬

```java
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feedbackId;
    
    @ManyToOne
    @JoinColumn(name = "review_id")
    private PerformanceReview review;        // Feedback on a review
    
    @ManyToOne
    @JoinColumn(name = "goal_id")
    private Goal goal;                       // Feedback on a goal
    
    @ManyToOne
    @JoinColumn(name = "given_by_user_id")
    private User givenByUser;                // Who gave this feedback?
    
    private String comments;                 // The actual feedback text
    
    private String feedbackType;             // "Positive", "Constructive", etc.
    
    private LocalDateTime date;
}
```

**Note:** Either `review` OR `goal` will be set, not both

---

### **9. AuditLog Entity** 📋

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auditId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;                       // Who did the action?
    
    private String action;                   // "CREATED_GOAL", "APPROVED_REVIEW"
    
    private String details;                  // Additional info (JSON)
    
    private String relatedEntityType;        // "Goal", "User", "Review"
    private Integer relatedEntityId;         // ID of affected entity
    
    private String ipAddress;                // Where action came from
    
    private String status;                   // "SUCCESS", "FAILED"
    
    private LocalDateTime timestamp;
}
```

**Purpose:** Security and compliance - tracks every action in the system

---

### **10. Report Entity** 📊

```java
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;
    
    private String scope;                    // "Department", "User", "Period"
    
    private String metrics;                  // Report data (JSON)
    
    private String format;                   // "PDF", "Excel", "CSV"
    
    @ManyToOne
    @JoinColumn(name = "generated_by")
    private User generatedBy;                // Who created report?
    
    private LocalDateTime generatedDate;
    
    private String filePath;                 // Where file is stored
}
```

---

## 🔗 Complete Relationship Map

```
User (Employee)
  ├── has many → Goals (as assignedToUser)
  ├── has many → PerformanceReviews (as user)
  ├── has many → Notifications
  ├── manages (as Manager) → Users
  ├── manages (as Manager) → Goals
  └── reviews (as Manager) → PerformanceReviews

Goal
  ├── belongs to → User (employee)
  ├── overseen by → User (manager)
  ├── has many → GoalCompletionApprovals
  ├── linked to many → PerformanceReviews (through PerformanceReviewGoals)
  └── has many → Feedback

PerformanceReview
  ├── belongs to → ReviewCycle
  ├── about → User (employee)
  ├── done by → User (manager)
  ├── linked to many → Goals (through PerformanceReviewGoals)
  └── has many → Feedback

ReviewCycle
  └── has many → PerformanceReviews
```

---

## 📝 Key JPA Concepts Explained

### **Cascade Types** (Not used in your code, but important!)

```java
@ManyToOne(cascade = CascadeType.ALL)
```

Options:
- `PERSIST`: When you save parent, save children too
- `MERGE`: When you update parent, update children too
- `REMOVE`: When you delete parent, delete children too
- `REFRESH`: When you refresh parent, refresh children too
- `DETACH`: When you detach parent, detach children too
- `ALL`: All of the above

### **Fetch Types**

```java
@ManyToOne(fetch = FetchType.LAZY)   // Load only when accessed
@ManyToOne(fetch = FetchType.EAGER)  // Load immediately with parent
```

- `LAZY`: Default for @OneToMany, @ManyToMany (better performance)
- `EAGER`: Default for @ManyToOne, @OneToOne (convenient but slower)

### **Orphan Removal** (Not used in your code)

```java
@OneToMany(orphanRemoval = true)
```
- Deletes child records when removed from parent's collection

---

## 🎯 Common Data Types Used

```java
Integer          // Whole numbers: 1, 2, 3, -5
String           // Text: "Hello", "john@email.com"
Boolean          // true or false
LocalDate        // Date only: 2025-01-22
LocalDateTime    // Date + time: 2025-01-22 14:30:00
Enum             // Fixed set of values: ACTIVE, INACTIVE
```

---

## 💡 Real-World Example Flow

**1. New Employee Joins**
```
1. Admin creates User: role=EMPLOYEE, manager=Sarah
2. AuditLog records: "USER_CREATED"
3. Notification sent to Employee: "ACCOUNT_CREATED"
```

**2. Employee Creates Goal**
```
1. Employee creates Goal: status=PENDING
2. Notification sent to Manager: "GOAL_SUBMITTED"
3. AuditLog records: "GOAL_CREATED"
```

**3. Manager Approves Goal**
```
1. Manager updates Goal: status=IN_PROGRESS, approvedBy=Manager
2. Notification sent to Employee: "GOAL_APPROVED"
3. AuditLog records: "GOAL_APPROVED"
```

**4. Employee Completes Goal**
```
1. Employee updates Goal: status=PENDING_COMPLETION_APPROVAL, evidenceLink=...
2. Notification sent to Manager: "GOAL_COMPLETION_SUBMITTED"
```

**5. Manager Verifies Completion**
```
1. Manager creates GoalCompletionApproval: decision=APPROVED
2. Manager updates Goal: status=COMPLETED
3. Notification sent to Employee: "GOAL_COMPLETION_APPROVED"
```

**6. Review Time**
```
1. Admin creates ReviewCycle: "Q1 2025", status=ACTIVE
2. Employee creates PerformanceReview: writes selfAssessment
3. System creates PerformanceReviewGoals linking review to completed goals
4. Manager adds managerFeedback and rating
5. Employee acknowledges review
```

---

This system tracks the complete employee performance lifecycle from goal setting to final reviews! Each entity plays a specific role in this process. Let me know if you want me to explain any specific part in more detail!