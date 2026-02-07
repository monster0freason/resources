# SSE and Scheduled Tasks Implementation Guide

Here's my honest assessment, and I'll address SSE separately since it changes the picture.

## Scheduled Tasks -- What's Worth It

### Keep (genuinely useful)

| Task | Why |
|------|-----|
| @EnableScheduling + basic setup | 2 lines, enables everything |
| Pending Approval Reminders (Daily 9 AM) | Managers forget. This is a real nudge. |
| Review Cycle Ending Reminders (Daily 10 AM) | Employees need deadline warnings. |
| Pending Completion Reminders (Weekly Mon 9 AM) | Same as approval reminders. |

### Skip

| Task | Why |
|------|-----|
| SchedulerConfig with thread pool of 5 | You have 3 tasks that run at different times. They'll never run concurrently. Default single thread is fine. |
| Old Notification Cleanup (Monthly) | Your project won't live long enough to accumulate 90 days of notifications. |
| Old Audit Log Archival (Quarterly) | Same. You won't have a year of audit logs. |
| Inactive User Cleanup (Yearly) | This is a production concern for a live product. Not a freshers project. |
| DB Health Check (Every 5 min) | Spring Boot Actuator already does this. You have the dependency. Hit /actuator/health and it checks DB automatically. |

That cuts 3 hours down to ~1 hour.

## Now About SSE -- This Is Where It Gets Interesting

SSE (Server-Sent Events) and scheduled tasks actually solve different parts of the same problem:

**Scheduled Task:**  "Hey, there are pending approvals" → creates a Notification in DB  
**SSE:**             "A notification was just created"   → pushes it to the user's browser in real-time

Right now your flow is:

1. Something happens (goal created, review submitted)
2. NotificationService.sendNotification() saves to DB
3. Frontend polls GET /api/v1/notifications every X seconds  ← wasteful
4. User eventually sees it

With SSE, it becomes:

1. Something happens
2. NotificationService.sendNotification() saves to DB
3. NotificationService ALSO pushes via SSE to connected user  ← instant
4. User sees it immediately

### Where SSE makes sense in your app

| Use Case | Makes Sense? |
|----------|--------------|
| Real-time notification delivery | Yes -- this is THE use case |
| Goal status changes | No -- happens rarely, not time-critical |
| Review cycle updates | No -- same reason |
| Scheduled task results | No -- they create notifications, which SSE already handles |

So SSE should be used in one place only: when a notification is created, push it to the user if they're connected.

## What the Implementation Would Look Like

4 things total:

1. NEW FILE:  SseEmitterService.java      -- manages SSE connections
2. NEW FILE:  NotificationScheduler.java   -- the 3 scheduled reminder tasks
3. CHANGE:    NotificationController.java  -- add SSE stream endpoint
4. CHANGE:    NotificationService.java     -- push via SSE when saving notification
5. CHANGE:    PerformanceTrackApplication.java -- add @EnableScheduling

---

## 1. CHANGE: PerformanceTrackApplication.java -- add one annotation

NOW:

```java
package com.project.performanceTrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class PerformanceTrackApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(PerformanceTrackApplication.class, args);
    }
}
```

AFTER:

```java
package com.project.performanceTrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;   // <-- new
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling                                                     // <-- new
public class PerformanceTrackApplication {
    // ... rest stays exactly the same
}
```

What this means: Tells Spring to look for @Scheduled methods and run them automatically. Without this annotation, any @Scheduled method would be silently ignored. One line, no config class needed.

## 2. NEW FILE: GoalRepository.java -- add 2 query methods

NOW:

```java
@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    List<Goal> findByAssignedToUser_UserId(Integer userId);
    List<Goal> findByAssignedManager_UserId(Integer managerId);
    List<Goal> findByStatus(GoalStatus status);
    List<Goal> findByAssignedToUser_UserIdAndStatus(Integer userId, GoalStatus status);
    List<Goal> findByAssignedManager_UserIdAndStatus(Integer managerId, GoalStatus status);
}
```

AFTER:

```java
@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {

    // Existing
    List<Goal> findByAssignedToUser_UserId(Integer userId);
    List<Goal> findByAssignedManager_UserId(Integer managerId);
    List<Goal> findByStatus(GoalStatus status);
    List<Goal> findByAssignedToUser_UserIdAndStatus(Integer userId, GoalStatus status);
    List<Goal> findByAssignedManager_UserIdAndStatus(Integer managerId, GoalStatus status);

    // New - needed by scheduler to find stale pending goals
    List<Goal> findByStatusAndCreatedDateBefore(GoalStatus status, LocalDateTime before);

    // New - needed by scheduler to find stale pending completions
    List<Goal> findByStatusAndCompletionSubmittedDateBefore(GoalStatus status, LocalDateTime before);
}
```

Also needs `import java.time.LocalDateTime;` at the top.

What this means: The scheduler needs to find goals that have been sitting in PENDING status for over 2 days, and goals in PENDING_COMPLETION_APPROVAL for over 3 days. These two methods let Spring Data auto-generate that query (WHERE status = ? AND created_date < ?).

## 3. NEW FILE: SseEmitterService.java

This is the SSE connection manager. It doesn't exist yet.

```java
// NEW FILE: src/main/java/com/project/performanceTrack/service/SseEmitterService.java

package com.project.performanceTrack.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    // Map of userId -> their active SSE connection
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Called when frontend connects to GET /api/v1/notifications/stream
    public SseEmitter createEmitter(Integer userId) {
        // 30 minute timeout (then frontend reconnects)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Clean up when connection ends
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        emitters.put(userId, emitter);
        log.debug("SSE connection opened for userId: {}", userId);
        return emitter;
    }

    // Called by NotificationService whenever a notification is saved
    public void sendToUser(Integer userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
                log.debug("SSE connection lost for userId: {}", userId);
            }
        }
    }
}
```

What this means in simple terms:

Think of SSE like a phone call that stays open:

- Frontend calls GET /api/v1/notifications/stream -- this "picks up the phone"
- createEmitter() creates an open connection and stores it in a map: {userId=5 -> connection}
- Later, when someone creates a goal and NotificationService.sendNotification() runs for userId 5, it also calls sendToUser(5, notification)
- sendToUser looks up userId 5 in the map, finds the open connection, and pushes the notification through
- Frontend receives it instantly without polling
- If user closes the tab or connection times out after 30 min, the cleanup callbacks (onCompletion, onTimeout) remove the entry from the map

The ConcurrentHashMap handles thread safety. Only one SSE connection per user (if they open a new tab, the old connection gets replaced).

## 4. CHANGE: NotificationService.java -- push via SSE after saving

NOW:

```java
@Service
@RequiredArgsConstructor
public class    NotificationService {

    private final NotificationRepository notifRepo;

    public void sendNotification(User user, NotificationType type, String message,
                                 String entityType, Integer entityId,
                                 String priority, boolean actionReq) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setType(type);
        notif.setMessage(message);
        notif.setRelatedEntityType(entityType);
        notif.setRelatedEntityId(entityId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setPriority(priority != null ? priority : "NORMAL");
        notif.setActionRequired(actionReq);
        notifRepo.save(notif);
    }

    // ... rest stays the same
}
```

AFTER:

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final SseEmitterService sseEmitterService;                // <-- new

    public void sendNotification(User user, NotificationType type, String message,
                                 String entityType, Integer entityId,
                                 String priority, boolean actionReq) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setType(type);
        notif.setMessage(message);
        notif.setRelatedEntityType(entityType);
        notif.setRelatedEntityId(entityId);
        notif.setStatus(NotificationStatus.UNREAD);
        notif.setPriority(priority != null ? priority : "NORMAL");
        notif.setActionRequired(actionReq);
        Notification saved = notifRepo.save(notif);                   // <-- capture return

        // Push to user in real-time if they're connected
        sseEmitterService.sendToUser(user.getUserId(), saved);        // <-- new

    }

    // ... rest stays exactly the same
}
```

What changed: 3 lines. Inject SseEmitterService, capture the saved notification, push it. Every place in your app that calls sendNotification() (GoalService, PerformanceReviewService, UserService, etc.) now automatically pushes real-time -- zero changes needed in those files.

## 5. CHANGE: NotificationController.java -- add SSE stream endpoint

NOW:

```java
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<Notification>> getNotifications(HttpServletRequest httpReq,
                                                            @RequestParam(required = false) String status) {
        Integer userId = (Integer) httpReq.getAttribute("userId");
        List<Notification> notifications = notificationService.getNotifications(userId, status);
        return ApiResponse.success("Notifications retrieved", notifications);
    }

    @PutMapping("/{notifId}")
    public ApiResponse<Notification> markAsRead(@PathVariable Integer notifId) {
        Notification updated = notificationService.markAsRead(notifId);
        return ApiResponse.success("Notification marked as read", updated);
    }

    @PutMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead(HttpServletRequest httpReq) {
        Integer userId = (Integer) httpReq.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("All notifications marked as read");
    }
}
```

AFTER:

```java
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;                // <-- new

    // NEW - SSE stream endpoint
    @GetMapping("/stream")
    public SseEmitter stream(HttpServletRequest httpReq) {
        Integer userId = (Integer) httpReq.getAttribute("userId");
        return sseEmitterService.createEmitter(userId);
    }

    @GetMapping
    public ApiResponse<List<Notification>> getNotifications(HttpServletRequest httpReq,
                                                            @RequestParam(required = false) String status) {
        // ... stays exactly the same
    }

    @PutMapping("/{notifId}")
    public ApiResponse<Notification> markAsRead(@PathVariable Integer notifId) {
        // ... stays exactly the same
    }

    @PutMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead(HttpServletRequest httpReq) {
        // ... stays exactly the same
    }
}
```

Also needs `import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;` and `import com.project.performanceTrack.service.SseEmitterService;`.

What this means: The frontend calls GET /api/v1/notifications/stream once when the page loads. This returns an SseEmitter which keeps the HTTP connection open. Spring handles the SSE protocol automatically.

Important: /stream must be above / in the file because Spring matches @GetMapping("/stream") before @GetMapping -- order matters for route matching.

Frontend usage would be:

```javascript
const eventSource = new EventSource('/api/v1/notifications/stream', {
    headers: { 'Authorization': 'Bearer ' + token }
});
eventSource.addEventListener('notification', (event) => {
    const notification = JSON.parse(event.data);
    // Show toast, update bell icon, etc.
});
```

## 6. NEW FILE: NotificationScheduler.java

This is the scheduled tasks file. Doesn't exist yet.

```java
// NEW FILE: src/main/java/com/project/performanceTrack/scheduler/NotificationScheduler.java

package com.project.performanceTrack.scheduler;

import com.project.performanceTrack.entity.Goal;
import com.project.performanceTrack.entity.ReviewCycle;
import com.project.performanceTrack.entity.User;
import com.project.performanceTrack.enums.GoalStatus;
import com.project.performanceTrack.enums.NotificationType;
import com.project.performanceTrack.enums.ReviewCycleStatus;
import com.project.performanceTrack.repository.GoalRepository;
import com.project.performanceTrack.repository.ReviewCycleRepository;
import com.project.performanceTrack.repository.UserRepository;
import com.project.performanceTrack.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final GoalRepository goalRepo;
    private final ReviewCycleRepository cycleRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    private static final Set<Long> REMINDER_DAYS = Set.of(30L, 15L, 7L, 3L);

    // Task 1: Remind managers about goals sitting in PENDING for 2+ days
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    public void sendPendingApprovalReminders() {
        log.info("Running: pending approval reminders");

        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        List<Goal> staleGoals = goalRepo.findByStatusAndCreatedDateBefore(
                GoalStatus.PENDING, twoDaysAgo);

        // Group by manager: {manager -> count of stale goals}
        Map<User, Long> countsByManager = staleGoals.stream()
                .collect(Collectors.groupingBy(
                        Goal::getAssignedManager, Collectors.counting()));

        countsByManager.forEach((manager, count) -> {
            notificationService.sendNotification(
                    manager,
                    NotificationType.REVIEW_REMINDER,
                    "You have " + count + " goal(s) pending approval for over 2 days",
                    "Goal", null, "HIGH", true);
        });

        log.info("Completed: pending approval reminders. Notified {} managers", countsByManager.size());
    }

    // Task 2: Remind employees when review cycle is ending soon
    @Scheduled(cron = "0 0 10 * * *") // Daily at 10 AM
    public void sendReviewCycleEndingReminders() {
        log.info("Running: review cycle ending reminders");

        cycleRepo.findFirstByStatusOrderByStartDateDesc(ReviewCycleStatus.ACTIVE)
                .ifPresent(cycle -> {
                    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), cycle.getEndDate());

                    if (REMINDER_DAYS.contains(daysLeft)) {
                        List<User> allUsers = userRepo.findAll();

                        allUsers.forEach(user -> {
                            notificationService.sendNotification(
                                    user,
                                    NotificationType.REVIEW_REMINDER,
                                    "Review cycle '" + cycle.getTitle() + "' ends in "
                                            + daysLeft + " days. Complete your goals.",
                                    "ReviewCycle", cycle.getCycleId(),
                                    daysLeft <= 7 ? "HIGH" : "NORMAL", false);
                        });

                        log.info("Completed: review cycle reminders. {} days left, notified {} users",
                                daysLeft, allUsers.size());
                    } else {
                        log.info("Completed: review cycle reminders. {} days left, no reminder needed", daysLeft);
                    }
                });
    }

    // Task 3: Remind managers about completions waiting for approval for 3+ days
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    public void sendPendingCompletionReminders() {
        log.info("Running: pending completion reminders");

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Goal> staleCompletions = goalRepo.findByStatusAndCompletionSubmittedDateBefore(
                GoalStatus.PENDING_COMPLETION_APPROVAL, threeDaysAgo);

        Map<User, Long> countsByManager = staleCompletions.stream()
                .collect(Collectors.groupingBy(
                        Goal::getAssignedManager, Collectors.counting()));

        countsByManager.forEach((manager, count) -> {
            notificationService.sendNotification(
                    manager,
                    NotificationType.REVIEW_REMINDER,
                    "You have " + count + " goal(s) pending completion approval for over 3 days",
                    "Goal", null, "HIGH", true);
        });

        log.info("Completed: pending completion reminders. Notified {} managers", countsByManager.size());
    }
}
```

What each task does in plain English:

**Task 1 (Daily 9 AM):** "Find all goals stuck in PENDING for over 2 days. Group them by manager. Send each manager one notification saying 'You have X goals waiting for your approval.'" This matters because if a manager has 10 pending goals, they get ONE notification saying "10 goals pending", not 10 separate notifications.

**Task 2 (Daily 10 AM):** "Find the active review cycle. Check how many days until it ends. If it's exactly 30, 15, 7, or 3 days away, notify everyone." The REMINDER_DAYS set makes this clean -- on all other days, this task runs but does nothing.

**Task 3 (Every Monday 9 AM):** Same as Task 1, but for goals in PENDING_COMPLETION_APPROVAL status (employee submitted completion, manager hasn't reviewed). Only runs weekly since it's less urgent.

Why all 3 tasks call notificationService.sendNotification(): Because we added SSE to that method. So if a manager happens to be online at 9 AM when the scheduler runs, they see the reminder pop up instantly. If they're not online, it's in the DB for when they next load the page.

## Summary of all changes

| Type | File | What |
|------|------|------|
| Changed (1 line) | PerformanceTrackApplication.java | Add @EnableScheduling |
| Changed (2 methods) | GoalRepository.java | Add findByStatusAndCreatedDateBefore, findByStatusAndCompletionSubmittedDateBefore |
| Changed (3 lines) | NotificationService.java | Inject SseEmitterService, push after save |
| Changed (1 method + 1 field) | NotificationController.java | Inject SseEmitterService, add GET /stream endpoint |
| New file | SseEmitterService.java | SSE connection manager (~40 lines) |
| New file | NotificationScheduler.java | 3 scheduled tasks (~90 lines) |

No new dependencies. @EnableScheduling and SseEmitter are both built into Spring Boot already. No Quartz, no external scheduler library.

The key insight: SSE + scheduled tasks work together. The scheduler creates notifications, and SSE delivers them instantly. Every existing sendNotification() call across your entire app (in GoalService, PerformanceReviewService, etc.) automatically gets real-time push -- because the SSE push happens inside NotificationService, which is the single point all notifications flow through.
