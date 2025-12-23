package com.secureauth.productapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UserCreateRequest {
    // Username for login (not necessarily an email)
    @NotBlank
    private String username;

    // Separate email id for communication / password delivery
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String countryCode;
    private String phone;

    private Long roleId; // single role for now
    private Set<Long> departmentIds;
    private Set<Long> functionIds;
    private Set<Long> operationIds;

    // Optional base64-encoded profile image string (e.g., data URL or raw base64)
    private String profileImage;

    // Admin password handling: if true, admin provides password; otherwise system emails a generated password
    private Boolean adminSetsPassword;

    // If adminSetsPassword == true, backend uses this password
    private String password;

    // New fields for Location, Plant, Area (sent as enum name strings, e.g., "LOCATION_A")
    private String location;
    private String area;
    private String plant;
}