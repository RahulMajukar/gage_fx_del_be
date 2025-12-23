package com.calendar.service;

import com.calendar.dto.*;
import com.calendar.model.*;
import com.calendar.repository.EventAttendeeRepository;
import com.calendar.repository.EventReminderRepository;
import com.calendar.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;
    private final EventReminderRepository reminderRepository;

    // ==========================================
    // GET EVENTS
    // ==========================================

    /**
     * Get all events for a user (owned + attending)
     */
    public List<EventDTO> getUserEvents(String userEmail) {
        log.info("📥 Fetching events for user: {}", userEmail);
        return eventRepository.findUserRelatedEvents(userEmail).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    /**
     * Get events created by user
     */
    public List<EventDTO> getEventsCreatedByUser(String userEmail) {
        log.info("👑 Fetching events created by user: {}", userEmail);
        return eventRepository.findByCreatedByEmailOrderByStartAsc(userEmail).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    /**
     * Get events user is attending
     */
    public List<EventDTO> getEventsUserAttending(String userEmail) {
        log.info("✓ Fetching events user {} is attending", userEmail);
        return eventRepository.findEventsUserAttending(userEmail).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    /**
     * Get single event by ID
     */
    public EventDTO getEventById(Long id) {
        log.info("🔍 Fetching event with id: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return convertToDTO(event);
    }

    /**
     * Get event by ID with user context
     */
    public EventDTO getEventByIdWithUserContext(Long id, String userEmail) {
        log.info("🔍 Fetching event with id: {} for user: {}", id, userEmail);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return convertToDTOWithUserContext(event, userEmail);
    }

    // ==========================================
    // FILTER & SEARCH
    // ==========================================

    public List<EventDTO> getUserEventsByDateRange(String userEmail, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("📅 Fetching events between {} and {} for user: {}", startDate, endDate, userEmail);
        return eventRepository.findUserEventsByDateRange(userEmail, startDate, endDate).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    public List<EventDTO> getUserEventsByCategory(String userEmail, EventCategory category) {
        log.info("📂 Fetching {} events for user: {}", category, userEmail);
        return eventRepository.findUserEventsByCategory(userEmail, category).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    public List<EventDTO> searchUserEvents(String userEmail, String searchText) {
        log.info("🔍 Searching events for user: {} with text: {}", userEmail, searchText);
        return eventRepository.searchUserEvents(userEmail, searchText).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    // ==========================================
    // UPCOMING & TODAY
    // ==========================================

    public List<EventDTO> getUserUpcomingEvents(String userEmail) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        log.info("📅 Fetching upcoming events for user: {}", userEmail);
        return eventRepository.findUserUpcomingEvents(userEmail, now, nextWeek).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    public List<EventDTO> getUserTodayEvents(String userEmail) {
        LocalDateTime today = LocalDateTime.now();
        log.info("📆 Fetching today's events for user: {}", userEmail);
        return eventRepository.findUserTodayEvents(userEmail, today).stream()
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    /**
     * Get expired events for a user
     */
    public List<EventDTO> getExpiredEvents(String userEmail) {
        LocalDateTime now = LocalDateTime.now();
        log.info("⏰ Fetching expired events for user: {}", userEmail);
        return eventRepository.findUserRelatedEvents(userEmail).stream()
                .filter(event -> event.getEnd().isBefore(now))
                .map(event -> convertToDTOWithUserContext(event, userEmail))
                .collect(Collectors.toList());
    }

    // ==========================================
    // CREATE EVENT
    // ==========================================

    public EventDTO createEvent(CreateEventRequest request, Long userId, String userEmail) {
        log.info("➕ Creating new event: {} by user: {}", request.getTitle(), userEmail);

        // Validate dates
        if (request.getEnd().isBefore(request.getStart())) {
            throw new RuntimeException("End time must be after start time");
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStart(request.getStart());
        event.setEnd(request.getEnd());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory() != null ? request.getCategory() : EventCategory.WORK);
        event.setPriority(request.getPriority() != null ? request.getPriority() : EventPriority.MEDIUM);
        event.setIsAllDay(request.getIsAllDay() != null ? request.getIsAllDay() : false);
        event.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        event.setCreatedBy(userId);
        event.setCreatedByEmail(userEmail);

        // Map attendees
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            List<EventAttendee> attendees = request.getAttendees().stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .map(email -> {
                        EventAttendee attendee = new EventAttendee();
                        attendee.setUsername(email.trim());
                        // Set creator as ACCEPTED, others as PENDING
                        attendee.setStatus(email.trim().equals(userEmail) ? AttendeeStatus.ACCEPTED : AttendeeStatus.PENDING);
                        attendee.setEvent(event);
                        return attendee;
                    })
                    .collect(Collectors.toList());
            event.setAttendees(attendees);
        }

        // Map reminders
        if (request.getReminders() != null && !request.getReminders().isEmpty()) {
            List<EventReminder> reminders = request.getReminders().stream()
                    .map(reminderReq -> {
                        EventReminder reminder = new EventReminder();
                        reminder.setType(reminderReq.getType());
                        reminder.setMinutes(reminderReq.getMinutes());
                        reminder.setIsSent(false);
                        reminder.setEvent(event);
                        return reminder;
                    })
                    .collect(Collectors.toList());
            event.setReminders(reminders);
        }

        Event savedEvent = eventRepository.save(event);
        log.info("✅ Event created with ID: {}", savedEvent.getId());

        return getEventByIdWithUserContext(savedEvent.getId(), userEmail);
    }

    // ==========================================
    // UPDATE EVENT
    // ==========================================

    public EventDTO updateEvent(Long id, CreateEventRequest request, Long userId, String userEmail) {
        log.info("✏️ Updating event with id: {} by user: {}", id, userEmail);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Check permissions - only owner can update
        boolean isOwner = event.getCreatedByEmail() != null &&
                event.getCreatedByEmail().equals(userEmail);

        if (!isOwner) {
            throw new RuntimeException("Only the event creator can update this event");
        }

        // Validate dates
        if (request.getEnd().isBefore(request.getStart())) {
            throw new RuntimeException("End time must be after start time");
        }

        // Update basic fields
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStart(request.getStart());
        event.setEnd(request.getEnd());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory() != null ? request.getCategory() : EventCategory.WORK);
        event.setPriority(request.getPriority() != null ? request.getPriority() : EventPriority.MEDIUM);
        event.setIsAllDay(request.getIsAllDay() != null ? request.getIsAllDay() : false);
        event.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);

        // Update attendees
        attendeeRepository.deleteByEventId(id);
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            List<EventAttendee> attendees = request.getAttendees().stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .map(email -> {
                        EventAttendee attendee = new EventAttendee();
                        attendee.setUsername(email.trim());
                        attendee.setStatus(AttendeeStatus.PENDING);
                        attendee.setEvent(event);
                        return attendee;
                    })
                    .collect(Collectors.toList());
            attendeeRepository.saveAll(attendees);
        }

        // Update reminders
        reminderRepository.deleteByEventId(id);
        if (request.getReminders() != null && !request.getReminders().isEmpty()) {
            List<EventReminder> reminders = request.getReminders().stream()
                    .map(reminderReq -> {
                        EventReminder reminder = new EventReminder();
                        reminder.setType(reminderReq.getType());
                        reminder.setMinutes(reminderReq.getMinutes());
                        reminder.setIsSent(false);
                        reminder.setEvent(event);
                        return reminder;
                    })
                    .collect(Collectors.toList());
            reminderRepository.saveAll(reminders);
        }

        eventRepository.save(event);
        log.info("✅ Event updated successfully");

        return getEventByIdWithUserContext(id, userEmail);
    }

    // ==========================================
    // DELETE EVENT (including recurring events)
    // ==========================================

    public void deleteEvent(Long id, Long userId, String userEmail) {
        log.info("🗑️ Deleting event with id: {} by user: {}", id, userEmail);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Only owner can delete (including recurring events)
        boolean isOwner = event.getCreatedByEmail() != null &&
                event.getCreatedByEmail().equals(userEmail);

        if (!isOwner) {
            throw new RuntimeException("Only the event creator can delete this event");
        }

        // Log if it's a recurring event
        if (event.getIsRecurring()) {
            log.info("🔄 Deleting recurring event with ID: {}", id);
        }

        eventRepository.deleteById(id);
        log.info("✅ Event deleted successfully");
    }

    // ==========================================
    // ACCEPT/DECLINE FUNCTIONALITY
    // ==========================================

    /**
     * Respond to an event invitation (accept or decline)
     */
    public EventDTO respondToEvent(Long eventId, String userEmail, String action) {
        log.info("📬 User {} responding to event {}: {}", userEmail, eventId, action);

        // Find the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Find the attendee record
        EventAttendee attendee = event.getAttendees().stream()
                .filter(a -> userEmail.equals(a.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You are not invited to this event"));

        // Update status
        if ("accept".equalsIgnoreCase(action)) {
            attendee.setStatus(AttendeeStatus.ACCEPTED);
            log.info("✅ Event accepted by {}", userEmail);
        } else if ("decline".equalsIgnoreCase(action)) {
            attendee.setStatus(AttendeeStatus.DECLINED);
            log.info("❌ Event declined by {}", userEmail);
        } else {
            throw new RuntimeException("Invalid action. Use 'accept' or 'decline'");
        }

        // Save the updated attendee
        attendeeRepository.save(attendee);

        // Return updated event with user context
        return getEventByIdWithUserContext(eventId, userEmail);
    }

    // ==========================================
    // STATISTICS
    // ==========================================

    public EventStatsDTO getUserEventStats(String userEmail) {
        log.info("📊 Calculating event statistics for user: {}", userEmail);

        List<Event> userEvents = eventRepository.findUserRelatedEvents(userEmail);
        List<Event> createdEvents = userEvents.stream()
                .filter(event -> userEmail.equals(event.getCreatedByEmail()))
                .collect(Collectors.toList());
        List<Event> attendingEvents = userEvents.stream()
                .filter(event -> !userEmail.equals(event.getCreatedByEmail()))
                .collect(Collectors.toList());

        EventStatsDTO stats = new EventStatsDTO();
        stats.setTotal((long) userEvents.size());

        // By category
        Map<String, Long> byCategory = new HashMap<>();
        byCategory.put("work", userEvents.stream().filter(e -> e.getCategory() == EventCategory.WORK).count());
        byCategory.put("personal", userEvents.stream().filter(e -> e.getCategory() == EventCategory.PERSONAL).count());
        byCategory.put("health", userEvents.stream().filter(e -> e.getCategory() == EventCategory.HEALTH).count());
        byCategory.put("education", userEvents.stream().filter(e -> e.getCategory() == EventCategory.EDUCATION).count());
        stats.setByCategory(byCategory);

        // By ownership
        Map<String, Long> byOwnership = new HashMap<>();
        byOwnership.put("owned", (long) createdEvents.size());
        byOwnership.put("attending", (long) attendingEvents.size());
        stats.setByOwnership(byOwnership);

        // Time-based
        stats.setUpcomingEvents((long) getUserUpcomingEvents(userEmail).size());
        stats.setTodayEvents((long) getUserTodayEvents(userEmail).size());

        // By status (for attendee events only)
        Map<String, Long> byStatus = new HashMap<>();
        long accepted = attendingEvents.stream()
                .flatMap(event -> event.getAttendees().stream())
                .filter(a -> userEmail.equals(a.getUsername()))
                .filter(a -> a.getStatus() == AttendeeStatus.ACCEPTED)
                .count();
        long pending = attendingEvents.stream()
                .flatMap(event -> event.getAttendees().stream())
                .filter(a -> userEmail.equals(a.getUsername()))
                .filter(a -> a.getStatus() == AttendeeStatus.PENDING)
                .count();
        long declined = attendingEvents.stream()
                .flatMap(event -> event.getAttendees().stream())
                .filter(a -> userEmail.equals(a.getUsername()))
                .filter(a -> a.getStatus() == AttendeeStatus.DECLINED)
                .count();

        byStatus.put("accepted", accepted);
        byStatus.put("pending", pending);
        byStatus.put("rejected", declined);
        stats.setByStatus(byStatus);

        return stats;
    }

    // ==========================================
    // DTO CONVERSION
    // ==========================================

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setLocation(event.getLocation());
        dto.setCategory(event.getCategory());
        dto.setPriority(event.getPriority());
        dto.setIsAllDay(event.getIsAllDay());
        dto.setIsRecurring(event.getIsRecurring());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        // Convert attendees
        if (event.getAttendees() != null) {
            List<EventAttendeeDTO> attendeeDTOs = event.getAttendees().stream()
                    .map(attendee -> {
                        EventAttendeeDTO attendeeDTO = new EventAttendeeDTO();
                        attendeeDTO.setId(attendee.getId());
                        attendeeDTO.setUsername(attendee.getUsername());
                        attendeeDTO.setName(attendee.getName());
                        attendeeDTO.setStatus(attendee.getStatus());
                        return attendeeDTO;
                    })
                    .collect(Collectors.toList());
            dto.setAttendees(attendeeDTOs);
        } else {
            dto.setAttendees(new ArrayList<>());
        }

        // Convert reminders
        if (event.getReminders() != null) {
            List<EventReminderDTO> reminderDTOs = event.getReminders().stream()
                    .map(reminder -> {
                        EventReminderDTO reminderDTO = new EventReminderDTO();
                        reminderDTO.setId(reminder.getId());
                        reminderDTO.setType(reminder.getType());
                        reminderDTO.setMinutes(reminder.getMinutes());
                        reminderDTO.setIsSent(reminder.getIsSent());
                        return reminderDTO;
                    })
                    .collect(Collectors.toList());
            dto.setReminders(reminderDTOs);
        } else {
            dto.setReminders(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Convert to DTO with user context (adds relationship info)
     * CRITICAL for frontend color coding and permissions
     */
    private EventDTO convertToDTOWithUserContext(Event event, String userEmail) {
        EventDTO dto = convertToDTO(event);

        // Determine user's relationship to the event
        boolean isOwner = event.getCreatedByEmail() != null &&
                event.getCreatedByEmail().equals(userEmail);

        boolean isAttendee = event.getAttendees() != null &&
                event.getAttendees().stream()
                        .anyMatch(attendee -> userEmail.equals(attendee.getUsername()));

        // Set relationship flags
        dto.setIsOwner(isOwner);
        dto.setIsAttendee(isAttendee);

        // Determine if event is expired
        boolean isExpired = event.getEnd().isBefore(LocalDateTime.now());

        // Set userRelationship and acceptanceStatus for frontend styling
        if (isOwner) {
            dto.setUserRelationship("owner");
            if (isExpired) {
                dto.setAcceptanceStatus("expired");
            } else {
                dto.setAcceptanceStatus(null); // Owners don't have acceptance status unless expired
            }
        } else if (isAttendee) {
            dto.setUserRelationship("attendee");

            // Get user's acceptance status
            event.getAttendees().stream()
                    .filter(attendee -> userEmail.equals(attendee.getUsername()))
                    .findFirst()
                    .ifPresent(attendee -> {
                        String status = attendee.getStatus().getValue();
                        if (isExpired) {
                            dto.setAcceptanceStatus("expired");
                        } else {
                            dto.setAcceptanceStatus(status);
                        }
                    });
        } else {
            dto.setUserRelationship("none");
            dto.setAcceptanceStatus(null);
        }

        return dto;
    }
}