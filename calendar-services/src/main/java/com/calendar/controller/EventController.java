package com.calendar.controller;

import com.calendar.dto.CreateEventRequest;
import com.calendar.dto.EventDTO;
import com.calendar.dto.EventStatsDTO;
import com.calendar.dto.RespondEventRequest;
import com.calendar.model.EventCategory;
import com.calendar.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private String getUserEmail(String userEmailHeader) {
        if (userEmailHeader == null || userEmailHeader.trim().isEmpty()) {
            throw new RuntimeException("User email is required");
        }
        return userEmailHeader.trim();
    }

    private Long getUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            return 1L; // Default for testing
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return 1L;
        }
    }

    // ==========================================
    // HEALTH CHECK
    // ==========================================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Calendar API",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ==========================================
    // EVENT CRUD OPERATIONS
    // ==========================================

    /**
     * Get all user events (owned + attending)
     * Frontend endpoint: GET /calendar/events
     */
    @GetMapping
    public ResponseEntity<?> getUserEvents(
            @RequestHeader(value = "User-Email", required = false) String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("📥 Fetching all events for user: {}", userEmail);

            List<EventDTO> events = eventService.getUserEvents(userEmail);
            log.info("✅ Found {} events for user: {}", events.size(), userEmail);

            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            log.warn("⚠️ Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching user events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get events created by user
     * Frontend endpoint: GET /calendar/events/owned
     */
    @GetMapping("/owned")
    public ResponseEntity<?> getEventsCreatedByUser(
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("👑 Fetching events created by user: {}", userEmail);

            List<EventDTO> events = eventService.getEventsCreatedByUser(userEmail);
            log.info("✅ Found {} created events", events.size());

            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            log.warn("⚠️ Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching created events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get events user is attending
     * Frontend endpoint: GET /calendar/events/attending
     */
    @GetMapping("/attending")
    public ResponseEntity<?> getEventsUserAttending(
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("✓ Fetching events user is attending: {}", userEmail);

            List<EventDTO> events = eventService.getEventsUserAttending(userEmail);
            log.info("✅ Found {} attending events", events.size());

            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            log.warn("⚠️ Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching attending events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get single event by ID
     * Frontend endpoint: GET /calendar/events/:id
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(
            @PathVariable Long id,
            @RequestHeader(value = "User-Email", required = false) String userEmailHeader) {
        try {
            log.info("🔍 Fetching event with ID: {}", id);

            if (userEmailHeader != null && !userEmailHeader.trim().isEmpty()) {
                String userEmail = getUserEmail(userEmailHeader);
                EventDTO event = eventService.getEventByIdWithUserContext(id, userEmail);
                return ResponseEntity.ok(event);
            } else {
                EventDTO event = eventService.getEventById(id);
                return ResponseEntity.ok(event);
            }
        } catch (RuntimeException e) {
            log.warn("⚠️ Event not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Create new event
     * Frontend endpoint: POST /calendar/events
     */
    @PostMapping
    public ResponseEntity<?> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader(value = "User-ID", required = false) String userIdHeader,
            @RequestHeader(value = "User-Email", required = false) String userEmailHeader) {
        try {
            Long userId = getUserId(userIdHeader);

            // Determine user email from header or request body
            String userEmail;
            if (userEmailHeader != null && !userEmailHeader.trim().isEmpty()) {
                userEmail = userEmailHeader.trim();
            } else if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                userEmail = request.getUsername().trim();
            } else {
                throw new RuntimeException("User email is required");
            }
            
            EventDTO event = eventService.createEvent(request, userId, userEmail);
            log.info("✅ Event created successfully with ID: {}", event.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(event);

        } catch (RuntimeException e) {
            log.error("⚠️ Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error creating event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Update existing event
     * Frontend endpoint: PUT /calendar/events/:id
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader(value = "User-ID", required = false) String userIdHeader,
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            Long userId = getUserId(userIdHeader);
            String userEmail = getUserEmail(userEmailHeader);

            log.info("✏️ Updating event ID: {} by user: {}", id, userEmail);

            EventDTO updated = eventService.updateEvent(id, request, userId, userEmail);
            log.info("✅ Event updated successfully");

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            log.warn("⚠️ Update failed: {}", e.getMessage());

            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } catch (Exception e) {
            log.error("❌ Error updating event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Delete event (including recurring events)
     * Frontend endpoint: DELETE /calendar/events/:id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long id,
            @RequestHeader(value = "User-ID", required = false) String userIdHeader,
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            Long userId = getUserId(userIdHeader);
            String userEmail = getUserEmail(userEmailHeader);

            log.info("🗑️ Deleting event ID: {} by user: {}", id, userEmail);

            eventService.deleteEvent(id, userId, userEmail);
            log.info("✅ Event deleted successfully");

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("⚠️ Delete failed: {}", e.getMessage());

            if (e.getMessage().contains("permission") || e.getMessage().contains("creator")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } catch (Exception e) {
            log.error("❌ Error deleting event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // ==========================================
    // ACCEPT/DECLINE FUNCTIONALITY
    // ==========================================

    /**
     * Accept or decline an event invitation
     * Frontend endpoint: POST /calendar/events/:id/respond
     * Body: { "action": "accept" | "decline" }
     */
    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respondToEvent(
            @PathVariable Long id,
            @Valid @RequestBody RespondEventRequest request,
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            String action = request.getAction().toLowerCase();

            log.info("📬 User {} responding to event {}: {}", userEmail, id, action);

            if (!action.equals("accept") && !action.equals("decline")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Action must be 'accept' or 'decline'"));
            }

            EventDTO event = eventService.respondToEvent(id, userEmail, action);
            log.info("✅ Response recorded successfully");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Event " + action + "ed successfully",
                    "event", event
            ));

        } catch (RuntimeException e) {
            log.warn("⚠️ Response failed: {}", e.getMessage());

            if (e.getMessage().contains("not invited") || e.getMessage().contains("not an attendee")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } catch (Exception e) {
            log.error("❌ Error responding to event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // ==========================================
    // SEARCH & FILTER
    // ==========================================

    /**
     * Search user events
     * Frontend endpoint: GET /calendar/events/search?q=query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUserEvents(
            @RequestHeader(value = "User-Email") String userEmailHeader,
            @RequestParam String q) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("🔍 Searching events for user: {} with query: {}", userEmail, q);

            List<EventDTO> events = eventService.searchUserEvents(userEmail, q);
            log.info("✅ Found {} matching events", events.size());

            return ResponseEntity.ok(events);

        } catch (RuntimeException e) {
            log.warn("⚠️ Search failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error searching events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get events by category
     * Frontend endpoint: GET /calendar/events/category/:category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getUserEventsByCategory(
            @PathVariable String category,
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            EventCategory eventCategory = EventCategory.fromValue(category);

            log.info("📂 Fetching {} events for user: {}", category, userEmail);

            List<EventDTO> events = eventService.getUserEventsByCategory(userEmail, eventCategory);
            log.info("✅ Found {} events in category", events.size());

            return ResponseEntity.ok(events);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Invalid category: {}", category);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid category: " + category));
        } catch (RuntimeException e) {
            log.warn("⚠️ Request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching events by category", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // ==========================================
    // STATISTICS
    // ==========================================

    /**
     * Get user-specific event statistics
     * Frontend endpoint: GET /calendar/events/user/stats
     */
    @GetMapping("/user/stats")
    public ResponseEntity<?> getUserEventStats(
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("📊 Fetching event statistics for user: {}", userEmail);

            EventStatsDTO stats = eventService.getUserEventStats(userEmail);
            log.info("✅ Statistics calculated successfully");

            return ResponseEntity.ok(stats);

        } catch (RuntimeException e) {
            log.warn("⚠️ Stats request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching user stats", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // ==========================================
    // ADDITIONAL UTILITY ENDPOINTS
    // ==========================================

    /**
     * Get upcoming events (next 7 days)
     * Frontend endpoint: GET /calendar/events/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents(
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("📅 Fetching upcoming events for user: {}", userEmail);

            List<EventDTO> events = eventService.getUserUpcomingEvents(userEmail);
            log.info("✅ Found {} upcoming events", events.size());

            return ResponseEntity.ok(events);

        } catch (RuntimeException e) {
            log.warn("⚠️ Request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching upcoming events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get today's events
     * Frontend endpoint: GET /calendar/events/today
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayEvents(
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("📆 Fetching today's events for user: {}", userEmail);

            List<EventDTO> events = eventService.getUserTodayEvents(userEmail);
            log.info("✅ Found {} events today", events.size());

            return ResponseEntity.ok(events);

        } catch (RuntimeException e) {
            log.warn("⚠️ Request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching today's events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get events by date range
     * Frontend endpoint: GET /calendar/events/date-range?startDate=...&endDate=...
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("📅 Fetching events between {} and {} for user: {}",
                    startDate, endDate, userEmail);

            List<EventDTO> events = eventService.getUserEventsByDateRange(
                    userEmail, startDate, endDate);
            log.info("✅ Found {} events in date range", events.size());

            return ResponseEntity.ok(events);

        } catch (RuntimeException e) {
            log.warn("⚠️ Request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching events by date range", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get expired events
     * Frontend endpoint: GET /calendar/events/expired
     */
    @GetMapping("/expired")
    public ResponseEntity<?> getExpiredEvents(
            @RequestHeader(value = "User-Email") String userEmailHeader) {
        try {
            String userEmail = getUserEmail(userEmailHeader);
            log.info("⏰ Fetching expired events for user: {}", userEmail);

            List<EventDTO> events = eventService.getExpiredEvents(userEmail);
            log.info("✅ Found {} expired events", events.size());

            return ResponseEntity.ok(events);

        } catch (RuntimeException e) {
            log.warn("⚠️ Request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error fetching expired events", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }
}