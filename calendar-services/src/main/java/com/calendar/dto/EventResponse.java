package com.calendar.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventResponse {
    
    private Long id;
    private String title;
    private String type;
    private String department;
    private String line;
    private String plantName;
    private LocalDateTime eventDate;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
