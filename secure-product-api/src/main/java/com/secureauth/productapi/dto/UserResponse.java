package com.secureauth.productapi.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String phone;
    private boolean active;
    private String role;
    private Set<String> departments;
    private Set<String> functions;
    private Set<String> operations;
    private String profileImage;

    // New fields
    private String location;
    private String area;
    private String plant;
}