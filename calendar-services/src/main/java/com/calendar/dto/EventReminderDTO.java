package com.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.ReminderType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventReminderDTO {
    private Long id;
    private ReminderType type;
    private Integer minutes;
    private Boolean isSent;
}
