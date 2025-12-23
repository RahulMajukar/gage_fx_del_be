package com.secureauth.productapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String profileImage;
    private Set<String> roles;
    private Set<String> departments;
    private Set<String> functions;
    private Set<String> operations;
    private String email;
    // Default constructor
    public JwtResponse() {}
}
