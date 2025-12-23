package com.calendar.repository;

import com.calendar.model.EventReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventReminderRepository extends JpaRepository<EventReminder, Long> {

    /**
     * Find all reminders for an event
     */
    List<EventReminder> findByEventId(Long eventId);

    /**
     * Delete all reminders for a given event
     */
    @Modifying
    @Query("DELETE FROM EventReminder r WHERE r.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}