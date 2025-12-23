package com.calendar.dto;

import com.calendar.model.AttendeeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeeDTO {
    private Long id;
    private String username;  //
    private String name;
    private AttendeeStatus status;
}