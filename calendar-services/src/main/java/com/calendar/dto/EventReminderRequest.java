package com.calendar.dto;

import lombok.Data;
import model.ReminderType;


@Data
public class EventReminderRequest {
    private ReminderType type = ReminderType.POPUP;
    private Integer minutes = 15;
}
