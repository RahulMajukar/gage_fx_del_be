package com.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private String username;
    private Set<String> roles;
    private Set<String> departments;
    private Set<String> functions;
    private Set<String> operations;

    // Default constructor
    public JwtResponseDTO() {}
}
