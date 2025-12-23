package com.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for accepting or declining event invitations
 * Used by: POST /api/calendar/events/{id}/respond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondEventRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "accept|decline", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Action must be 'accept' or 'decline'")
    private String action; // "accept" or "decline"
}