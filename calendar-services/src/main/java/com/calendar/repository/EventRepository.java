package com.calendar.repository;

import com.calendar.model.Event;
import com.calendar.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // ==========================================
    // ADMIN/GLOBAL QUERIES (no user filter)
    // ==========================================

    // Events by date range
    @Query("SELECT e FROM Event e WHERE e.start >= :startDate AND e.end <= :endDate ORDER BY e.start")
    List<Event> findEventsByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // Events by category
    List<Event> findByCategoryOrderByStartAsc(EventCategory category);

    // Search events
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY e.start")
    List<Event> searchEvents(@Param("searchText") String searchText);

    // Upcoming events
    @Query("SELECT e FROM Event e WHERE e.start >= :now AND e.start <= :nextWeek ORDER BY e.start")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now,
                                   @Param("nextWeek") LocalDateTime nextWeek);

    // Today's events
    @Query("SELECT e FROM Event e WHERE DATE(e.start) = DATE(:today) ORDER BY e.start")
    List<Event> findTodayEvents(@Param("today") LocalDateTime today);

    // Count by category
    Long countByCategory(EventCategory category);

    // Pending reminders
    @Query("SELECT DISTINCT e FROM Event e JOIN e.reminders r WHERE r.isSent = false " +
            "AND e.start <= :reminderTime ORDER BY e.start")
    List<Event> findEventsWithPendingReminders(@Param("reminderTime") LocalDateTime reminderTime);

    // ==========================================
    // USER-SPECIFIC QUERIES (FIXED - using email)
    // ==========================================

    /**
     * Find all events related to a user (created by them OR attending)
     * FIXED: Now uses e.createdByEmail instead of e.createdBy
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
            "WHERE e.createdByEmail = :userEmail OR a.username = :userEmail ORDER BY e.start")
    List<Event> findUserRelatedEvents(@Param("userEmail") String userEmail);

    /**
     * Find events user is attending (not created by them)
     */
    @Query("SELECT DISTINCT e FROM Event e JOIN e.attendees a " +
            "WHERE a.username = :userEmail ORDER BY e.start ASC")
    List<Event> findEventsUserAttending(@Param("userEmail") String userEmail);

    /**
     * Find user events by date range
     * FIXED: Now uses e.createdByEmail
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
            "WHERE (e.createdByEmail = :userEmail OR a.username = :userEmail) " +
            "AND e.start >= :startDate AND e.end <= :endDate ORDER BY e.start")
    List<Event> findUserEventsByDateRange(@Param("userEmail") String userEmail,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find user events by category
     * FIXED: Now uses e.createdByEmail
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
            "WHERE (e.createdByEmail = :userEmail OR a.username = :userEmail) " +
            "AND e.category = :category ORDER BY e.start")
    List<Event> findUserEventsByCategory(@Param("userEmail") String userEmail,
                                         @Param("category") EventCategory category);

    /**
     * Search user events
     * FIXED: Now uses e.createdByEmail
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
            "WHERE (e.createdByEmail = :userEmail OR a.username = :userEmail) " +
            "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :searchText, '%'))) ORDER BY e.start")
    List<Event> searchUserEvents(@Param("userEmail") String userEmail,
                                 @Param("searchText") String searchText);

    /**
     * Find upcoming user events
     * FIXED: Now uses e.createdByEmail
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
            "WHERE (e.createdByEmail = :userEmail OR a.username = :userEmail) " +
            "AND e.start >= :now AND e.start <= :nextWeek ORDER BY e.start")
    List<Event> findUserUpcomingEvents(@Param("userEmail") String userEmail,
                                       @Param("now") LocalDateTime now,
                                       @Param("nextWeek") LocalDateTime nextWeek);

    /**
     * Find today's user events
     * FIXED: Now uses e.createdByEmail
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a " +
            "WHERE (e.createdByEmail = :userEmail OR a.username = :userEmail) " +
            "AND DATE(e.start) = DATE(:today) ORDER BY e.start")
    List<Event> findUserTodayEvents(@Param("userEmail") String userEmail,
                                    @Param("today") LocalDateTime today);

    /**
     * Find events created by a specific user
     * NEW: Query specifically for events created by user
     */
    @Query("SELECT e FROM Event e WHERE e.createdByEmail = :userEmail ORDER BY e.start")
    List<Event> findByCreatedByEmailOrderByStartAsc(@Param("userEmail") String userEmail);
}