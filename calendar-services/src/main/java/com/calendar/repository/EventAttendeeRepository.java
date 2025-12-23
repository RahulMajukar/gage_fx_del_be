package com.calendar.repository;

import com.calendar.model.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

    /**
     * Find all attendees for an event
     */
    List<EventAttendee> findByEventId(Long eventId);

    /**
     * Check if a user is an attendee of a given event
     */
    boolean existsByEventIdAndUsername(Long eventId, String username);

    /**
     * Delete all attendees for a given event
     */
    @Modifying
    @Query("DELETE FROM EventAttendee a WHERE a.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}