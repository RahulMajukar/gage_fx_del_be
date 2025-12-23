package com.calendar.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class EventRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Event type is required")
    private String type;
    
    private String department;
    
    private String line;
    
    private String plantName;
    
    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;
    
    private String description;
    
    private String status;
}
