```java
package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.entity.Notification;
import com.project.performanceTrack.enums.NotificationStatus;
import com.project.performanceTrack.exception.ResourceNotFoundException;
import com.project.performanceTrack.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NOTIFICATION CONTROLLER
 * Handles REST API endpoints for notification management in PerformanceTrack system.
 * Provides functionality to view and manage user notifications.
 */
@RestController  // Combines @Controller + @ResponseBody - all methods return JSON automatically
@RequestMapping("/api/v1/notifications")  // Base URL: all endpoints start with /api/v1/notifications
public class NotificationController {
    
    // Inject NotificationRepository for database operations
    // Spring automatically creates and provides the implementation at runtime
    @Autowired
    private NotificationRepository notifRepo;
    
    /**
     * ENDPOINT: GET ALL NOTIFICATIONS FOR CURRENT USER
     * URL: GET /api/v1/notifications OR GET /api/v1/notifications?status=READ
     * 
     * Retrieves notifications for the authenticated user, optionally filtered by status.
     * User identity comes from JWT token, extracted by authentication filter.
     */
    @GetMapping  // Maps HTTP GET requests to this method
    public ApiResponse<List<Notification>> getNotifications(
            HttpServletRequest httpReq,  // Contains userId extracted from JWT token
            @RequestParam(required = false) String status) {  // Optional query param: ?status=READ or ?status=UNREAD
        
        // Extract userId from request attributes (set by JWT authentication filter)
        // This ensures users can only access their own notifications
        Integer userId = (Integer) httpReq.getAttribute("userId");
        
        // Declare variable to store query results
        List<Notification> notifs;
        
        // Check if status filter was provided in query parameters
        if (status != null) {
            // Convert string parameter to enum (e.g., "READ" -> NotificationStatus.READ)
            // .toUpperCase() handles case-insensitive input (read, Read, READ all work)
            NotificationStatus notifStatus = NotificationStatus.valueOf(status.toUpperCase());
            
            // Fetch notifications filtered by both userId and status
            // Returns only notifications matching the specified status (READ or UNREAD)
            // Results ordered by creation date, newest first
            notifs = notifRepo.findByUser_UserIdAndStatusOrderByCreatedDateDesc(userId, notifStatus);
        } else {
            // No status filter provided - fetch all notifications for this user
            // Results ordered by creation date, newest first
            notifs = notifRepo.findByUser_UserIdOrderByCreatedDateDesc(userId);
        }
        
        // Wrap result in standardized ApiResponse format and return
        // Returns JSON: {"success": true, "message": "...", "data": [...]}
        return ApiResponse.success("Notifications retrieved", notifs);
    }
    
    /**
     * ENDPOINT: MARK SINGLE NOTIFICATION AS READ
     * URL: PUT /api/v1/notifications/{notifId}
     * 
     * Marks a specific notification as READ and records the timestamp.
     * Used when user clicks on or views a notification.
     */
    @PutMapping("/{notifId}")  // Maps HTTP PUT to /api/v1/notifications/{notifId}
    public ApiResponse<Notification> markAsRead(
            @PathVariable Integer notifId) {  // Extracts {notifId} from URL path
        
        // Fetch the notification from database by ID
        // findById() returns Optional<Notification> (may or may not contain a value)
        // .orElseThrow() extracts the Notification if found, or throws exception if not
        // This prevents NullPointerException and provides clear error handling
        Notification notif = notifRepo.findById(notifId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        // Update notification status to READ
        notif.setStatus(NotificationStatus.READ);
        
        // Record the exact time when notification was read
        notif.setReadDate(LocalDateTime.now());
        
        // Persist changes to database
        // save() performs UPDATE because notification already has an ID
        // Returns the updated entity
        Notification updated = notifRepo.save(notif);
        
        // Return success response with updated notification data
        return ApiResponse.success("Notification marked as read", updated);
    }
    
    /**
     * ENDPOINT: MARK ALL NOTIFICATIONS AS READ
     * URL: PUT /api/v1/notifications/mark-all-read
     * 
     * Bulk operation to mark all UNREAD notifications as READ for current user.
     * Provides "Mark all as read" feature commonly found in notification systems.
     */
    @PutMapping("/mark-all-read")  // More specific path - Spring matches this before /{notifId}
    public ApiResponse<Void> markAllAsRead(HttpServletRequest httpReq) {  // Void return - no data payload
        
        // Extract userId from JWT token stored in request attributes
        Integer userId = (Integer) httpReq.getAttribute("userId");
        
        // Fetch all UNREAD notifications for this user
        // Only retrieve unread ones - no need to update notifications already marked as read
        // Results ordered by creation date (though order doesn't matter for bulk update)
        List<Notification> notifs = notifRepo
                .findByUser_UserIdAndStatusOrderByCreatedDateDesc(userId, NotificationStatus.UNREAD);
        
        // Loop through each notification and update in memory
        // forEach() applies the lambda expression to each element
        // Lambda: n -> { ... } means "for each notification n, execute this code block"
        notifs.forEach(n -> {
            n.setStatus(NotificationStatus.READ);  // Change status from UNREAD to READ
            n.setReadDate(LocalDateTime.now());     // Record current timestamp
        });
        
        // Batch save all updated notifications to database
        // saveAll() is more efficient than calling save() in a loop
        // Reduces database round trips by batching multiple UPDATE statements
        notifRepo.saveAll(notifs);
        
        // Return success response with no data payload
        // Client just needs confirmation - can refresh notification list if needed
        return ApiResponse.success("All notifications marked as read");
    }
}
```

---

```java
package com.project.performanceTrack.repository;

import com.project.performanceTrack.entity.Notification;
import com.project.performanceTrack.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NOTIFICATION REPOSITORY
 * Data Access Layer for Notification entity.
 * 
 * Spring Data JPA automatically implements this interface at runtime.
 * No need to write SQL or implementation classes - method names define queries.
 */
@Repository  // Marks this as a Spring Data repository component
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // JpaRepository<Notification, Integer>
    //               ^            ^
    //               |            └─ Primary Key type (Integer notificationId)
    //               └────────────── Entity type being managed
    
    // Inherited methods from JpaRepository (no need to define):
    // - save(Notification n)        : Insert or update
    // - findById(Integer id)        : Find by primary key
    // - findAll()                   : Get all notifications
    // - deleteById(Integer id)      : Delete by primary key
    // - count()                     : Count total records
    
    /**
     * CUSTOM QUERY METHOD 1: Find all notifications for a specific user
     * 
     * METHOD NAME BREAKDOWN:
     * findBy                       - Indicates SELECT query
     * User_UserId                  - Navigate from Notification.user to User.userId
     * OrderBy                      - SQL ORDER BY clause
     * CreatedDate                  - Field to sort by
     * Desc                         - Descending order (newest first)
     * 
     * HOW IT WORKS:
     * Notification entity has: @ManyToOne User user;
     * User entity has: @Id Integer userId;
     * 
     * "User_UserId" means:
     * 1. Access the "user" field in Notification
     * 2. Navigate to the related User entity (underscore indicates relationship traversal)
     * 3. Access the "userId" field in User
     * 
     * GENERATED SQL:
     * SELECT n.* FROM notification n
     * INNER JOIN user u ON n.user_id = u.user_id
     * WHERE u.user_id = ?
     * ORDER BY n.created_date DESC
     * 
     * RETURN: List of all notifications for the given user, newest first
     */
    List<Notification> findByUser_UserIdOrderByCreatedDateDesc(Integer userId);
    
    /**
     * CUSTOM QUERY METHOD 2: Find notifications filtered by user AND status
     * 
     * METHOD NAME BREAKDOWN:
     * findBy                       - Indicates SELECT query
     * User_UserId                  - Navigate to User.userId (as explained above)
     * And                          - SQL AND operator to combine conditions
     * Status                       - Filter by notification status field
     * OrderBy                      - SQL ORDER BY clause
     * CreatedDate                  - Field to sort by
     * Desc                         - Descending order (newest first)
     * 
     * GENERATED SQL:
     * SELECT n.* FROM notification n
     * INNER JOIN user u ON n.user_id = u.user_id
     * WHERE u.user_id = ? AND n.status = ?
     * ORDER BY n.created_date DESC
     * 
     * PARAMETERS:
     * userId - The user whose notifications to fetch
     * status - The NotificationStatus enum value (READ or UNREAD)
     * 
     * RETURN: List of notifications matching both userId and status, newest first
     */
    List<Notification> findByUser_UserIdAndStatusOrderByCreatedDateDesc(
            Integer userId, 
            NotificationStatus status);
}
```

**KEY CONCEPTS EXPLAINED:**

1. **Repository Pattern**: Abstracts database operations into a dedicated layer
2. **Spring Data JPA Magic**: Automatically implements methods based on naming conventions
3. **Method Name Query Derivation**: Method names like `findByUser_UserId` are parsed to generate SQL
4. **Relationship Navigation**: Underscore (`_`) in method names traverses entity relationships
5. **No SQL Required**: Spring generates optimized SQL queries automatically
6. **Type Safety**: Return types and parameters are strongly typed (compile-time safety)
