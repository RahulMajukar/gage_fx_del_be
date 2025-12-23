//package com.calendar.service.impl;
//
//import com.calendar.dto.EventRequest;
//import com.calendar.dto.EventResponse;
//import com.calendar.model.Event;
//import com.calendar.service.EventService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class EventServiceImpl implements EventService {
//
//    @Autowired
//    private EventRepository eventRepository;
//
//    @Override
//    public EventResponse createEvent(EventRequest eventRequest) {
//        Event event = new Event();
//        event.setTitle(eventRequest.getTitle());
//        event.setType(eventRequest.getType());
//        event.setDepartment(eventRequest.getDepartment());
//        event.setLine(eventRequest.getLine());
//        event.setPlantName(eventRequest.getPlantName());
//        event.setEventDate(eventRequest.getEventDate());
//        event.setDescription(eventRequest.getDescription());
//        event.setStatus(eventRequest.getStatus() != null ? eventRequest.getStatus() : "Active");
//
//        Event savedEvent = eventRepository.save(event);
//        return convertToResponse(savedEvent);
//    }
//
//    @Override
//    public EventResponse getEventById(Long id) {
//        Event event = eventRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
//        return convertToResponse(event);
//    }
//
//    @Override
//    public List<EventResponse> getAllEvents() {
//        List<Event> events = eventRepository.findAll();
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public EventResponse updateEvent(Long id, EventRequest eventRequest) {
//        Event event = eventRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
//
//        event.setTitle(eventRequest.getTitle());
//        event.setType(eventRequest.getType());
//        event.setDepartment(eventRequest.getDepartment());
//        event.setLine(eventRequest.getLine());
//        event.setPlantName(eventRequest.getPlantName());
//        event.setEventDate(eventRequest.getEventDate());
//        event.setDescription(eventRequest.getDescription());
//        if (eventRequest.getStatus() != null) {
//            event.setStatus(eventRequest.getStatus());
//        }
//
//        Event updatedEvent = eventRepository.save(event);
//        return convertToResponse(updatedEvent);
//    }
//
//    @Override
//    public void deleteEvent(Long id) {
//        if (!eventRepository.existsById(id)) {
//            throw new RuntimeException("Event not found with id: " + id);
//        }
//        eventRepository.deleteById(id);
//    }
//
//    @Override
//    public List<EventResponse> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
//        List<Event> events = eventRepository.findEventsByDateRange(startDate, endDate);
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<EventResponse> getEventsByDate(LocalDateTime date) {
//        List<Event> events = eventRepository.findEventsByDate(date);
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<EventResponse> getEventsByType(String type) {
//        List<Event> events = eventRepository.findByType(type);
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<EventResponse> getEventsByDepartment(String department) {
//        List<Event> events = eventRepository.findByDepartment(department);
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<EventResponse> getEventsByPlantName(String plantName) {
//        List<Event> events = eventRepository.findByPlantName(plantName);
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<EventResponse> getEventsByStatus(String status) {
//        List<Event> events = eventRepository.findByStatus(status);
//        return events.stream()
//            .map(this::convertToResponse)
//            .collect(Collectors.toList());
//    }
//
//    private EventResponse convertToResponse(Event event) {
//        EventResponse response = new EventResponse();
//        response.setId(event.getId());
//        response.setTitle(event.getTitle());
//        response.setType(event.getType());
//        response.setDepartment(event.getDepartment());
//        response.setLine(event.getLine());
//        response.setPlantName(event.getPlantName());
//        response.setEventDate(event.getEventDate());
//        response.setDescription(event.getDescription());
//        response.setStatus(event.getStatus());
//        response.setCreatedAt(event.getCreatedAt());
//        response.setUpdatedAt(event.getUpdatedAt());
//        return response;
//    }
//}
